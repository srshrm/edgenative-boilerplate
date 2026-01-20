import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(project(":composeApp"))
    implementation(libs.compose.desktop.currentOs)
    implementation(libs.compose.ui)
    implementation(libs.kotlinx.coroutines.swing)
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "AEMKMPBoilerplate"
            packageVersion = "1.0.0"
        }
    }
}
