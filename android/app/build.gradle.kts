plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

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
    implementation(project(":data"))
    implementation(project(":hardware"))
    implementation(project(":ui"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)

    // DECISIONS.md ADR-028: Hilt.
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
