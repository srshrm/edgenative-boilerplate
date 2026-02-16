package com.aem.utils

import android.content.Intent
import androidx.core.net.toUri
import com.aem.AppContextProvider

actual fun openUrl(url: String) {
    val context = AppContextProvider.applicationContext
    try {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Log or handle error
        e.printStackTrace()
    }
}

actual val supportsPullToRefresh: Boolean = true

