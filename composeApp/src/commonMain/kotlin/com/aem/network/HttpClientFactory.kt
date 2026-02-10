package com.aem.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Factory for creating platform-specific HTTP clients.
 * The actual engine is provided by platform-specific implementations.
 */
expect fun createPlatformHttpClient(): HttpClient

/**
 * JSON configuration used for parsing EDS responses.
 */
val edsJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
    encodeDefaults = true
}

/**
 * Create a configured HTTP client for EDS API calls.
 */
fun createEdsHttpClient(): HttpClient {
    return createPlatformHttpClient().config {
        install(ContentNegotiation) {
            json(edsJson)
        }
    }
}

