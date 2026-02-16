package com.aem.utils

/**
 * Platform-specific URL opener.
 * Opens URLs in the system's default browser.
 */
expect fun openUrl(url: String)

/**
 * Whether pull-to-refresh gesture is supported on this platform.
 * True on touch-based platforms (Android, iOS), false on pointer/mouse
 * platforms (wasmJs, desktop) where the gesture doesn't work reliably.
 */
expect val supportsPullToRefresh: Boolean

/**
 * Clean and normalize a URL path.
 */
fun normalizePath(path: String): String {
    return path
        .trimStart('/')
        .trimEnd('/')
        .removeSuffix(".html")
        .ifEmpty { "index" }
}

/**
 * Check if a path represents the home/index page.
 */
fun isHomePath(path: String): Boolean {
    val normalized = normalizePath(path)
    return normalized.isEmpty() || normalized == "index" || normalized == "/"
}

