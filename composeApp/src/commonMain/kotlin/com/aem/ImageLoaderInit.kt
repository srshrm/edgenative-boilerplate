package com.aem

import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.aem.network.createPlatformHttpClient

/**
 * Initialize the Coil image loader with our platform HTTP client.
 * Configures Coil to use Ktor so images route through the same
 * HTTP client (including any CORS proxy on WASM).
 *
 * Must be called before any Compose rendering starts.
 */
fun initImageLoader() {
    SingletonImageLoader.setSafe { context ->
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory(httpClient = createPlatformHttpClient()))
            }
            .build()
    }
}
