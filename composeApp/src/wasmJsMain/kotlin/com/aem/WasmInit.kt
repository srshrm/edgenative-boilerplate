package com.aem

import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.aem.network.createPlatformHttpClient

/**
 * Initialize WASM-specific platform services.
 * Configures Coil image loader to use our CORS proxy-aware HTTP client,
 * so images from AEM EDS also go through the dev server proxy.
 *
 * Must be called before any Compose rendering starts.
 */
fun initWasmImageLoader() {
    SingletonImageLoader.setSafe { context ->
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory(httpClient = createPlatformHttpClient()))
            }
            .build()
    }
}
