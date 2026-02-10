package com.aem.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember

/**
 * Simple in-memory cache for EDS pages to avoid re-fetching on back navigation.
 * Pages are cached by their path key.
 */
class PageCache {
    private val cache = mutableMapOf<String, EdsPage>()

    fun get(key: String): EdsPage? = cache[key]

    fun put(key: String, page: EdsPage) {
        cache[key] = page
    }

    fun remove(key: String) {
        cache.remove(key)
    }

    fun contains(key: String): Boolean = cache.containsKey(key)

    fun clear() {
        cache.clear()
    }

    companion object {
        const val HOME_KEY = "__home__"
    }
}

/**
 * CompositionLocal for providing PageCache down the composition tree.
 */
val LocalPageCache = compositionLocalOf { PageCache() }

/**
 * Remember a PageCache instance at the composition root.
 */
@Composable
fun rememberPageCache(): PageCache = remember { PageCache() }
