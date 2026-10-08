plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

/*
 * `ai-sessions/0062` F-5 (the maintainer's choice, chat 2026-10-01): the build identity shown on the Info tab, computed **locally** from this checkout at
 * configuration time — never from the network. `git` is run through Gradle's `providers.exec` (configuration-cache friendly); without git, outside a
 * repository or on any error the value is "unknown" and the build still succeeds (offline and in CI). "-dirty" = tracked files differ from the commit (untracked
 * files, e.g. `android/.kotlin/`, do not count). The commit's own date, not a build time: two builds of one commit stay identical (reproducible).
 */
fun gitOutput(vararg args: String): String? = try {
    providers.exec {
        commandLine("git", *args)
        isIgnoreExitValue = true
    }.standardOutput.asText.get().trim().takeIf { it.isNotEmpty() }
} catch (e: Exception) {
    null
}

val gitCommit: String = gitOutput("rev-parse", "--short", "HEAD")?.let { hash ->
    if (gitOutput("status", "--porcelain", "--untracked-files=no") != null) "$hash-dirty" else hash
} ?: "unknown"
val gitCommitDate: String = gitOutput("log", "-1", "--format=%cs") ?: "unknown"

/*
 * `ai-sessions/0065` (the maintainer's choice, chat 2026-10-02): the release signing values come from outside the repository — Gradle properties (the
 * maintainer's `~/.gradle/gradle.properties`, chmod 600) or, as a fallback, environment variables of the same name. The keystore file itself lives outside the
 * repository too. Without all four values the release APK is not built at all (`packageRelease` fails with the message below): never unsigned, never signed with
 * the debug key — a differently signed APK cannot update an installed one (developer.android.com, "Sign your app"). Debug builds, tests and lint need none
 * of this. `RELEASING.md` has the steps.
 */
val releaseSigningKeys = listOf("OPENCONTROL_STORE_FILE", "OPENCONTROL_KEY_ALIAS", "OPENCONTROL_STORE_PASSWORD", "OPENCONTROL_KEY_PASSWORD")
val releaseSigning: Map<String, String> = releaseSigningKeys.mapNotNull { key ->
    (providers.gradleProperty(key).orNull ?: providers.environmentVariable(key).orNull)?.takeIf { it.isNotBlank() }?.let { key to it }
}.toMap()
val missingReleaseSigning: List<String> = releaseSigningKeys.filterNot { it in releaseSigning }

android {
    namespace = "io.github.tedsluis.opencontrolpixelbuds"
    // DECISIONS.md ADR-029 and its Update of 2026-10-07 (`ai-sessions/0078`): compile against API 37 — required by the AndroidX/Compose releases of
    // the toolchain upgrade; compile-time only (minSdk and targetSdk stay 34).
    compileSdk = 37

    defaultConfig {
        applicationId = "io.github.tedsluis.opencontrolpixelbuds"
        // DECISIONS.md ADR-029: minimum supported Android API is 34, matching the target SDK (compileSdk 37 since its 2026-10-07 Update).
        minSdk = 34
        targetSdk = 34
        // `ai-sessions/0065` (chat 2026-10-02): versionCode = major * 10000 + minor * 100 + patch (1.0.0 → 10000, 1.0.1 → 10001, 1.1.0 → 10100); a release
        // candidate before 1.0.0 ("1.0.0-rc.1") takes 9901; 1.1.1 → 10101. It must grow with every release: Android refuses to install a lower one over a higher one.
        // Release candidates exist only for X.0.0 (`ai-sessions/0069`, `scripts/release.sh`): for any other version the candidate's code would not be
        // above the previous release. 1.0.1 = the hotfix of `ai-sessions/0069` (the maintainer's choice in chat 2026-10-03); 1.1.1 = the toolchain of `ai-sessions/0078`
        // (`ai-sessions/0079`, the maintainer's choice in chat 2026-10-08).
        versionCode = 10101
        versionName = "1.1.1"
        buildConfigField("String", "GIT_COMMIT", "\"$gitCommit\"")
        buildConfigField("String", "GIT_COMMIT_DATE", "\"$gitCommitDate\"")
    }

    signingConfigs {
        if (missingReleaseSigning.isEmpty()) {
            create("release") {
                storeFile = file(releaseSigning.getValue("OPENCONTROL_STORE_FILE"))
                storePassword = releaseSigning.getValue("OPENCONTROL_STORE_PASSWORD")
                keyAlias = releaseSigning.getValue("OPENCONTROL_KEY_ALIAS")
                keyPassword = releaseSigning.getValue("OPENCONTROL_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            if (missingReleaseSigning.isEmpty()) signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        buildConfig = true // F-5 (the Info tab's build identity) and F-8 (StrictMode only when BuildConfig.DEBUG)
    }

    // Correction (post-`ai-sessions/0033`): this block previously disabled lint's `MissingClass`
    // check here, reasoning it was an AGP+Hilt tooling false positive because the compiled
    // .class files existed on disk. That reasoning was wrong — lint was correctly reporting a
    // real bug (a mismatch between this module's `namespace` and OpenControlApplication/
    // MainActivity's actual Kotlin package, which made AndroidManifest.xml's relative
    // ".ClassName" references resolve to nonexistent fully-qualified names), confirmed by a
    // crash on real hardware. Fixed at the source (AndroidManifest.xml now uses fully-qualified
    // class names) instead of suppressing the check that had already caught it — no lint
    // disables needed.
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":hardware"))
    implementation(project(":ui"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)

    // DECISIONS.md ADR-028: Hilt.
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}

// The clear failure of `ai-sessions/0065`: checked when the release APK is packaged, not at configuration, so debug builds, tests and lint run without keys.
val missingSigningForTask = missingReleaseSigning
tasks.matching { it.name == "packageRelease" }.configureEach {
    doFirst {
        if (missingSigningForTask.isNotEmpty()) {
            throw GradleException(
                "Release signing is not configured: missing ${missingSigningForTask.joinToString()}. Set them in ~/.gradle/gradle.properties " +
                    "(or as environment variables) — see RELEASING.md. The release APK is never built unsigned or with the debug key.",
            )
        }
    }
}
