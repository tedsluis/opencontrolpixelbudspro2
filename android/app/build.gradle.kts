plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
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

android {
    namespace = "io.github.tedsluis.opencontrolpixelbuds"
    compileSdk = 34

    defaultConfig {
        applicationId = "io.github.tedsluis.opencontrolpixelbuds"
        // DECISIONS.md ADR-029: minimum supported Android API is 34, matching compile/target SDK.
        minSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0-dev"
        buildConfigField("String", "GIT_COMMIT", "\"$gitCommit\"")
        buildConfigField("String", "GIT_COMMIT_DATE", "\"$gitCommitDate\"")
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
