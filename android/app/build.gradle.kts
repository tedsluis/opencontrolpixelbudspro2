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
