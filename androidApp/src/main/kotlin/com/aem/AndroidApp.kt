package com.aem

import android.app.Application
import android.content.Context

/**
 * Android Application class to provide application context and initialize services.
 */
class AndroidApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize AppContextProvider for shared module
        AppContextProvider.initialize(this)
    }

    companion object {
        private lateinit var instance: AndroidApp

        val applicationContext: Context
            get() = instance.applicationContext
    }
}
