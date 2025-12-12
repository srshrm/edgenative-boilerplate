package com.adobe.aem_kmp_boilerplate.network

import com.adobe.aem_kmp_boilerplate.data.EdsConfig
import com.adobe.aem_kmp_boilerplate.data.EdsPage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

/**
 * Service for fetching EDS page content via the JSON conversion service.
 *
 * @param httpClient The HTTP client to use for requests
 */
class EdsApiService(
    private val httpClient: HttpClient = createEdsHttpClient()
) {
    /**
     * Fetch an EDS page as structured JSON.
     *
     * @param config The EDS site configuration
     * @param path The relative page path (e.g., "products/item1" or "" for home)
     * @return Result containing the parsed EdsPage or an error
     */
    suspend fun fetchPage(config: EdsConfig, path: String): Result<EdsPage> {
        return try {
            val url = config.getJsonUrl(path)
            val response: HttpResponse = httpClient.get(url)

            if (response.status.isSuccess()) {
                val page: EdsPage = response.body()
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

