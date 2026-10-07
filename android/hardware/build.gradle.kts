plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.github.tedsluis.opencontrolpixelbuds.hardware"
    // DECISIONS.md ADR-029 and its Update of 2026-10-07 (`ai-sessions/0078`): compile against API 37 — required by the AndroidX/Compose releases of
    // the toolchain upgrade; compile-time only (minSdk and targetSdk stay 34).
    compileSdk = 37

    defaultConfig {
        // Minimum supported Android API: 34 (Android 14), matching the target SDK (compileSdk 37 since the ADR-029 Update of 2026-10-07)
        // (DECISIONS.md ADR-029) — well above the 26 CompanionDeviceManager itself needs
        // (ARCHITECTURE.md §9, DECISIONS.md ADR-005).
        minSdk = 34
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    // FakeBudsTransport lives in test fixtures, not the production source set (0044 finding APP-10): shared by this
    // module's and :data's unit tests, never packaged into the app.
    testFixtures {
        enable = true
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":domain"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.core.ktx)
    // javax.inject.Inject only — this module has no other Hilt/Dagger dependency,
    // so it stays usable from a manual-DI consumer too (DECISIONS.md ADR-028 only
    // decided Hilt for :app's own composition root, not a hard dependency here).
    implementation(libs.javax.inject)

    testFixturesImplementation(project(":domain"))
    testFixturesImplementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit5.jupiter.api)
    testRuntimeOnly(libs.junit5.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher) // Gradle 9: the launcher is no longer added by itself (`ai-sessions/0078`)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
