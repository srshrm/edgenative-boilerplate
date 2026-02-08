package com.aem.network

import com.aem.data.ContentParser
import com.aem.data.EdsConfig
import com.aem.data.EdsPage
import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

/**
 * Service for fetching EDS page content via plain HTML.
 *
 * Fetches the `.plain.html` variant of an EDS page and parses it
 * into structured [EdsPage] models using ksoup.
 *
 * @param httpClient The HTTP client to use for requests
 */
class EdsApiService(
    private val httpClient: HttpClient = createPlatformHttpClient()
) {
    /**
     * Fetch an EDS page by parsing its plain HTML content.
     *
     * @param config The EDS site configuration
     * @param path The relative page path (e.g., "products/item1" or "" for home)
     * @return Result containing the parsed EdsPage or an error
     */
    suspend fun fetchPage(config: EdsConfig, path: String): Result<EdsPage> {
        return try {
            val url = config.getPlainHtmlUrl(path)
            val response: HttpResponse = httpClient.get(url)

            if (response.status.isSuccess()) {
                val html = response.bodyAsText()
                val doc = Ksoup.parse(html = html, baseUri = config.getPageUrl(path))
                val page = ContentParser.parseDocument(doc, config.siteUrl)
                Result.success(page)
            } else {
                Result.failure(
                    EdsApiException(
                        "Failed to fetch page: HTTP ${response.status.value}",
                        response.status.value
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(
                EdsApiException("Network error: ${e.message}", cause = e)
            )
        }
    }

    /**
     * Fetch the home page for the given EDS configuration.
     * Uses the configured homePath from the EdsConfig.
     *
     * @param config The EDS site configuration
     * @return Result containing the parsed EdsPage or an error
     */
    suspend fun fetchHomePage(config: EdsConfig): Result<EdsPage> {
        return fetchPage(config, config.homePath)
    }

    /**
     * Close the HTTP client and release resources.
     */
    fun close() {
        httpClient.close()
    }
}

/**
 * Exception thrown when EDS API calls fail.
 */
class EdsApiException(
    message: String,
    val statusCode: Int? = null,
    cause: Throwable? = null
) : Exception(message, cause)
