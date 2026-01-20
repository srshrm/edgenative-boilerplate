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

    android {
        namespace = "com.adobe.aem_kmp_boilerplate"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        androidResources.enable = true
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }

    jvm()

    listOf(
        iosArm64(),
        iosX64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            // Export KMPNotifier for iOS Swift access
            export(libs.kmpnotifier)
        }
    }

    sourceSets {
        androidMain.dependencies {
            // Ktor - Android engine
            implementation(libs.ktor.client.okhttp)

            // Koin - Android
            implementation(libs.koin.android)
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

            // DataStore Preferences
            implementation(libs.datastore.preferences)
            implementation(libs.datastore)

            // KMPNotifier (Push & Local Notifications)
            api(libs.kmpnotifier)
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
    }
}

// UI Tooling for Compose previews (AGP 9.0 with android.kmp.library plugin)
dependencies {
    androidRuntimeClasspath(libs.compose.ui.tooling)
}
