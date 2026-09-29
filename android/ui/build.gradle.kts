plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "io.github.tedsluis.opencontrolpixelbuds.ui"
    compileSdk = 34

    defaultConfig {
        // DECISIONS.md ADR-029: minimum supported Android API is 34, matching compile/target SDK.
        minSdk = 34
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
    }

    // `ai-sessions/0057`: Compose UI tests run on the JVM under Robolectric (they need the merged resources/manifest).
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":domain"))

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.core)
    // Not referenced by this module's code, but NOT dead (0044 finding APP-12, checked 2026-09-24): navigation-compose pulls
    // lifecycle-viewmodel-compose 2.6.2 transitively; this direct dependency aligns it with the rest of lifecycle 2.8.6.
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.navigation.compose) // ARCHITECTURE.md §2.4: navigation structure.
    debugImplementation(libs.compose.ui.tooling)

    // `ai-sessions/0057` (the maintainer's choice at the checkpoint, AGENTS.md §10): Compose UI tests on the JVM. Test-only — nothing here reaches the APK.
    // `ui-test-junit4` drives composables; `ui-test-manifest` supplies the empty activity the test rule starts (as `testImplementation`, not the documented
    // `debugImplementation`, so it never enters the debug APK's manifest); Robolectric runs the tests without a device; JUnit 4 is the runner Robolectric
    // needs. None of them brings a network, analytics or ads dependency (`./gradlew :ui:dependencies`, checked in the 0057 RESULT).
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.junit4)
    testImplementation(libs.robolectric)
    testImplementation(libs.compose.ui.test.manifest)
}
