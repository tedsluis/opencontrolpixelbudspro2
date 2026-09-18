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

    lint {
        // Known AGP+Hilt lint false positive (ai-sessions/0033, Phase 8): lint's manifest-vs-
        // classpath check runs before Hilt's ASM class transform (transformDebugClassesWithAsm)
        // has produced the final Application/Activity classes lint's classpath model expects,
        // so it reports OpenControlApplication/MainActivity as "not found" even though both
        // compile and are present in every build output this session verified directly
        // (app/build/tmp/kotlin-classes/debug/.../OpenControlApplication.class and
        // .../MainActivity.class both exist after a normal build). Disabling only this specific
        // check, not lint wholesale.
        disable += "MissingClass"
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
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)

    // DECISIONS.md ADR-028: Hilt.
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
