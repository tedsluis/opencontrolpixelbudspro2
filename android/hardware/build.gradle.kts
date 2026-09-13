plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "io.github.tedsluis.opencontrolpixelbuds.hardware"
    compileSdk = 34

    defaultConfig {
        // Minimum supported Android API: 34 (Android 14), matching compile/target SDK
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
    implementation("javax.inject:javax.inject:1")

    testImplementation(libs.junit5.jupiter.api)
    testRuntimeOnly(libs.junit5.jupiter.engine)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
