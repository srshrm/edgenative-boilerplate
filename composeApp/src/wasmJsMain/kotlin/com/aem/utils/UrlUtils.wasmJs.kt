package com.aem.utils

import kotlinx.browser.window

actual fun openUrl(url: String) {
    window.open(url, "_blank")
}
