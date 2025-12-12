package com.adobe.aem_kmp_boilerplate.data

/**
 * Configuration for an EDS (Edge Delivery Services) site.
 * Used to construct URLs for fetching page JSON content.
 *
 * @param siteUrl The base URL of the EDS site (e.g., "https://main--aem-boilerplate--adobe.aem.live")
 * @param homePath The relative path to use as the home page (e.g., "emea/en/products" or "" for site root)
 * @param jsonServiceUrl The JSON conversion service URL (default: "https://mhast-html-to-json.aemrnd.workers.dev")
 */
data class EdsConfig(
    val siteUrl: String,
    val homePath: String = "",
    val jsonServiceUrl: String = DEFAULT_JSON_SERVICE_URL
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
     * Construct the JSON URL for a specific page path using the new query parameter approach.
     *
     * @param path The relative page path (e.g., "products/item1" or "" for home)
     * @return The full JSON conversion service URL with url and head query parameters
     */
    fun getJsonUrl(path: String): String {
        val pageUrl = getPageUrl(path)
        return "$jsonServiceUrl?url=$pageUrl&head=false"
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

    companion object {
        /**
         * Default JSON conversion service URL.
         * Can be overridden by providing a custom jsonServiceUrl in the constructor.
         */
        const val DEFAULT_JSON_SERVICE_URL = "https://mhast-html-to-json.aemrnd.workers.dev"
    }
}

/**
 * Default EDS configuration for the AEM Boilerplate site.
 */
val DefaultEdsConfig = EdsConfig(
    siteUrl = "https://main--aem-boilerplate--adobe.aem.live"
)

