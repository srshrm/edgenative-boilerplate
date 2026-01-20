package com.adobe.aem_kmp_boilerplate

import android.app.Application
import android.content.Context
import com.adobe.aem_kmp_boilerplate.app.R
import com.mmk.kmpnotifier.notification.NotifierManager
import com.mmk.kmpnotifier.notification.configuration.NotificationPlatformConfiguration

/**
 * Android Application class to provide application context and initialize services.
 */
class AndroidApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize AppContextProvider for shared module
        AppContextProvider.initialize(this)

        // Initialize KMPNotifier for Android
        NotifierManager.initialize(
            configuration = NotificationPlatformConfiguration.Android(
                notificationIconResId = R.drawable.ic_launcher_foreground,
                showPushNotification = true
            )
        )
    }

    companion object {
        private lateinit var instance: AndroidApp

        val applicationContext: Context
            get() = instance.applicationContext
    }
}
