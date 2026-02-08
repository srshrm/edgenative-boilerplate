package com.aem.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.URLProtocol
import kotlinx.browser.window

/**
 * Ktor client plugin that routes external (cross-origin) requests through
 * the local webpack dev server CORS proxy at /cors-proxy?url=<target>.
 *
 * This avoids browser CORS restrictions when fetching content from AEM EDS
 * endpoints during local preview.
 */
private val CorsProxyPlugin = createClientPlugin("CorsProxy") {
    onRequest { request, _ ->
        val currentHost = window.location.hostname
        // Only proxy requests going to a different host (i.e., external)
        if (request.url.host.isNotEmpty() && request.url.host != currentHost) {
            val targetUrl = request.url.buildString()
            request.url.protocol = URLProtocol.HTTP
            request.url.host = currentHost
            request.url.port = window.location.port.toIntOrNull() ?: 80
            request.url.pathSegments = listOf("cors-proxy")
            request.url.parameters.clear()
            request.url.parameters.append("url", targetUrl)
        }
    }
}

actual fun createPlatformHttpClient(): HttpClient {
    return HttpClient(Js) {
        install(CorsProxyPlugin)
    }
}
