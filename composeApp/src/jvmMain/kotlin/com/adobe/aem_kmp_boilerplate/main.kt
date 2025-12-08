package com.adobe.aem_kmp_boilerplate

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "AEMKMPBoilerplate",
    ) {
        App()
    }
}