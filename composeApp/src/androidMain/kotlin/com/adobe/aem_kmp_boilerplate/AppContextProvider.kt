package com.adobe.aem_kmp_boilerplate

import android.content.Context

/**
 * Provides application context for the shared module.
 * Must be initialized by the Android app before use.
 */
object AppContextProvider {
    private var _context: Context? = null

    val applicationContext: Context
        get() = _context ?: throw IllegalStateException(
            "AppContextProvider not initialized. Call initialize() in your Application class."
        )

    fun initialize(context: Context) {
        _context = context.applicationContext
    }
}
