package com.aem.data

/**
 * Default EDS configuration for the AEM Boilerplate site.
 */
val DefaultEdsConfig = EdsConfig(
    siteUrl = "https://main--aem-boilerplate--adobe.aem.live",
    homePath = ""
)

/**
 * Configuration for an EDS (Edge Delivery Services) site.
 * Used to construct URLs for fetching page plain HTML content.
 *
 * @param siteUrl The base URL of the EDS site (e.g., "https://main--aem-boilerplate--adobe.aem.live")
 * @param homePath The relative path to use as the home page (e.g., "emea/en/products" or "" for site root)
 */
data class EdsConfig(
    val siteUrl: String = DefaultEdsConfig.siteUrl,
    val homePath: String = DefaultEdsConfig.homePath
) {
    /**
     * Host name for the EDS site (used for link handling).
     */
    val siteHost: String
        get() = siteUrl
            .removePrefix("https://")
            .removePrefix("http://")
            .substringBefore("/")

    /**
     * Construct the plain HTML URL for a specific page path.
     *
     * URL construction rules:
     * - Host URL or path ending with '/' -> append "index.plain.html"
     * - Otherwise -> append ".plain.html"
     *
     * Examples:
     * - "https://site.com" -> "https://site.com/index.plain.html"
     * - "https://site.com/page" -> "https://site.com/page.plain.html"
     *
     * @param path The relative page path (e.g., "products/item1" or "" for home)
     * @return The full plain HTML URL
     */
    fun getPlainHtmlUrl(path: String): String {
        val pageUrl = getPageUrl(path)
        return when {
            pageUrl == siteUrl -> "$siteUrl/index.plain.html"
            pageUrl.endsWith("/") -> "${pageUrl}index.plain.html"
            else -> "$pageUrl.plain.html"
        }
    }

    /**
     * Construct the live site URL for a specific page path.
     *
     * @param path The relative page path
     * @return The full live site URL
     */
    fun getPageUrl(path: String): String {
        val cleanPath = path.trimStart('/')
        return if (cleanPath.isEmpty()) siteUrl else "$siteUrl/$cleanPath"
    }

    /**
     * Resolve a relative URL to an absolute URL.
     * Handles URLs starting with "./", "/", or already absolute URLs.
     *
     * @param relativeUrl The relative URL (e.g., "./media_xxx.jpg")
     * @return The absolute URL
     */
    fun resolveUrl(relativeUrl: String?): String? {
        if (relativeUrl == null) return null

        return when {
            // Already absolute URL
            relativeUrl.startsWith("http://") || relativeUrl.startsWith("https://") -> relativeUrl
            // Relative to current directory
            relativeUrl.startsWith("./") -> "$siteUrl/${relativeUrl.removePrefix("./")}"
            // Relative to root
            relativeUrl.startsWith("/") -> "$siteUrl$relativeUrl"
            // Assume relative to root
            else -> "$siteUrl/$relativeUrl"
        }
    }
}
