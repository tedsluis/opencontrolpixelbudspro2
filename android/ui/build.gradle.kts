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
}
