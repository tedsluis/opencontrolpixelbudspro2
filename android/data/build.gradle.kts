plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

// Converted from a plain `kotlin.jvm` module to an Android library module this
// session (ai-sessions/0033, Phase 5) — ARCHITECTURE.md §2's documented
// dependency direction (`:data` depends on `:hardware` to consume
// `BudsTransport`) requires both modules to share the same Kotlin platform
// type ("androidJvm"); Gradle's variant-aware dependency resolution refuses
// to let a pure `kotlin.jvm` module depend on a `com.android.library` one
// (attribute mismatch: `org.jetbrains.kotlin.platform.type` "jvm" vs.
// "androidJvm") — confirmed by actually trying it and reading Gradle's own
// variant-matching error before making this change, not assumed upfront.
// Every codec class in this module remains pure Kotlin with zero Android
// framework imports; only the Gradle plugin/dependency-resolution mechanism
// changed, not the code's own platform requirements.
android {
    namespace = "io.github.tedsluis.opencontrolpixelbuds.data"
    compileSdk = 34

    defaultConfig {
        // DECISIONS.md ADR-029: minimum supported Android API is 34, matching compile/target SDK.
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
    implementation(project(":hardware")) // ARCHITECTURE.md §2: :data consumes :hardware's BudsTransport.
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    // AGENTS.md §10: persists the Debug Mode toggle (ARCHITECTURE.md §7/§12) — this project's
    // already-decided local-persistence mechanism (ARCHITECTURE.md §2/§9, "Encrypted DataStore").
    implementation(libs.datastore.preferences)

    testImplementation(testFixtures(project(":hardware"))) // FakeBudsTransport (0044 APP-10)
    testImplementation(libs.junit5.jupiter.api)
    testImplementation(libs.junit5.jupiter.params)
    testRuntimeOnly(libs.junit5.jupiter.engine)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
