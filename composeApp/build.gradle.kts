import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { browser() }

    android {
        namespace = "com.aem"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        androidResources.enable = true
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    jvm()

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            // Ktor - Android engine
            implementation(libs.ktor.client.okhttp)

            // Koin - Android
            implementation(libs.koin.android)

            // Media3 ExoPlayer (Video)
            implementation(libs.androidx.media3.exoplayer)
            implementation(libs.androidx.media3.ui)
        }

        commonMain.dependencies {
            // Compose Multiplatform
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.animation)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.ui.tooling.preview)

            // Lifecycle
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)

            // Material 3 Adaptive (Window Size Classes)
            implementation(libs.compose.material3.adaptive)

            // Material Icons Core
            implementation(libs.compose.material.icons.core)

            // Navigation 3 (KMP-compatible)
            implementation(libs.navigation3.ui)
            implementation(libs.lifecycle.viewmodel.navigation3)
            implementation(libs.navigationevent.compose)

            // Ktor - HTTP Client
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            // Ksoup - HTML Parsing
            implementation(libs.ksoup)

            // Coil - Image Loading
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)

            // Koin - Dependency Injection
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // Serialization & Utilities
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlin.datetime)
            implementation(libs.kotlinx.coroutines.core)


        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        iosMain.dependencies {
            // Ktor - iOS engine
            implementation(libs.ktor.client.darwin)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)

            // Ktor - JVM engine
            implementation(libs.ktor.client.okhttp)
        }

        wasmJsMain.dependencies {
            // Ktor - WASM/JS engine
            implementation(libs.ktor.client.js)
        }
    }
}

// UI Tooling for Compose previews (AGP 9.0 with android.kmp.library plugin)
dependencies {
    androidRuntimeClasspath(libs.compose.ui.tooling)
}
