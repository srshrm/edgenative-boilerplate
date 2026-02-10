package com.aem.navigation

import com.aem.data.EdsConfig

/**
 * Handles link clicks and determines whether to navigate internally or externally.
 */
object LinkHandler {

    /**
     * Determine if a URL should be handled with internal navigation.
     *
     * @param url The clicked URL
     * @param config The current EDS site configuration
     * @return true if the link should navigate within the app, false for external browser
     */
    fun shouldNavigateInternally(url: String, config: EdsConfig): Boolean {
        // Empty or blank URLs are internal
        if (url.isBlank()) return true

        // Relative URLs are always internal
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return true
        }

        // Check if the URL belongs to the current EDS site
        return try {
            val host = extractHost(url)
            host == config.siteHost || host == null
        } catch (e: Exception) {
            // If we can't parse the URL, treat as internal
            true
        }
    }

    /**
     * Extract the relative path from a URL for internal navigation.
     *
     * @param url The full URL or relative path
     * @param config The current EDS site configuration
     * @return The relative path suitable for PageDetail navigation
     */
    fun extractPath(url: String, config: EdsConfig): String {
        // Already a relative path
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return url.trimStart('/')
        }

        // Extract path from full URL
        return try {
            val withoutProtocol = url
                .removePrefix("http://")
                .removePrefix("https://")

            val pathStart = withoutProtocol.indexOf('/')
            if (pathStart >= 0) {
                withoutProtocol.substring(pathStart + 1)
                    .removeSuffix(".html")
                    .trimEnd('/')
            } else {
                ""
            }
        } catch (e: Exception) {
            url.trimStart('/')
        }
    }

    /**
     * Extract the host from a URL.
     */
    private fun extractHost(url: String): String? {
        val withoutProtocol = url
            .removePrefix("http://")
            .removePrefix("https://")

        val hostEnd = withoutProtocol.indexOfFirst { it == '/' || it == '?' || it == '#' }
        return if (hostEnd >= 0) {
            withoutProtocol.substring(0, hostEnd)
        } else {
            withoutProtocol
        }.takeIf { it.isNotBlank() }
    }

    /**
     * Check if a URL is an anchor/hash link on the current page.
     */
    fun isAnchorLink(url: String): Boolean {
        return url.startsWith("#")
    }

    /**
     * Check if a URL is a mailto or tel link.
     */
    fun isSpecialProtocol(url: String): Boolean {
        return url.startsWith("mailto:") ||
                url.startsWith("tel:") ||
                url.startsWith("sms:")
    }
}

