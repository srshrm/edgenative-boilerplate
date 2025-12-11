package com.adobe.aem_kmp_boilerplate.notification

import com.mmk.kmpnotifier.notification.NotificationImage
import com.mmk.kmpnotifier.notification.Notifier
import com.mmk.kmpnotifier.notification.NotifierManager
import com.mmk.kmpnotifier.notification.PayloadData
import kotlin.random.Random

/**
 * Common notification service for showing local notifications across all platforms.
 * Uses KMPNotifier library under the hood.
 *
 * Based on: https://github.com/mirzemehdi/KMPNotifier
 */
object NotificationService {

    private val notifier: Notifier
        get() = NotifierManager.getLocalNotifier()

    /**
     * Show a local notification with title and body.
     *
     * @param id Unique notification ID (auto-generated if not provided)
     * @param title The notification title
     * @param body The notification body/message
     * @param payloadData Custom payload data for handling notification taps
     * @param imageUrl Optional image URL to display in notification (Android & iOS only)
     * @return The notification ID
     */
    fun showNotification(
        id: Int = Random.nextInt(0, Int.MAX_VALUE),
        title: String,
        body: String,
        payloadData: Map<String, String> = emptyMap(),
        imageUrl: String? = null
    ): Int {
        notifier.notify {
            this.id = id
            this.title = title
            this.body = body
            this.payloadData = payloadData
            imageUrl?.let {
                this.image = NotificationImage.Url(it)
            }
        }
        return id
    }

    /**
     * Show a local notification with a URL payload for deep linking.
     *
     * @param id Unique notification ID (auto-generated if not provided)
     * @param title The notification title
     * @param body The notification body/message
     * @param url URL to open when notification is tapped
     * @param imageUrl Optional image URL to display in notification
     * @return The notification ID
     */
    fun showNotificationWithUrl(
        id: Int = Random.nextInt(0, Int.MAX_VALUE),
        title: String,
        body: String,
        url: String,
        imageUrl: String? = null
    ): Int {
        notifier.notify {
            this.id = id
            this.title = title
            this.body = body
            this.payloadData = mapOf(Notifier.KEY_URL to url)
            imageUrl?.let {
                this.image = NotificationImage.Url(it)
            }
        }
        return id
    }

    /**
     * Remove a specific notification by ID.
     *
     * @param id The notification ID to remove
     */
    fun removeNotification(id: Int) {
        notifier.remove(id)
    }

    /**
     * Remove all notifications.
     */
    fun removeAllNotifications() {
        notifier.removeAll()
    }

    /**
     * Set a listener for notification events.
     *
     * @param onNewToken Called when a new FCM/APNs token is received
     * @param onPushNotificationWithPayload Called when a push notification is received with payload
     * @param onNotificationClicked Called when a notification is clicked
     * @param onPayloadData Called when payload data is received
     */
    fun setNotificationListener(
        onNewToken: ((String) -> Unit)? = null,
        onPushNotificationWithPayload: ((title: String?, body: String?, data: PayloadData) -> Unit)? = null,
        onNotificationClicked: ((data: PayloadData) -> Unit)? = null,
        onPayloadData: ((data: PayloadData) -> Unit)? = null
    ) {
        NotifierManager.addListener(object : NotifierManager.Listener {
            override fun onNewToken(token: String) {
                onNewToken?.invoke(token)
            }

            override fun onPushNotificationWithPayloadData(
                title: String?,
                body: String?,
                data: PayloadData
            ) {
                super.onPushNotificationWithPayloadData(title, body, data)
                onPushNotificationWithPayload?.invoke(title, body, data)
            }

            override fun onNotificationClicked(data: PayloadData) {
                super.onNotificationClicked(data)
                onNotificationClicked?.invoke(data)
            }

            override fun onPayloadData(data: PayloadData) {
                super.onPayloadData(data)
                onPayloadData?.invoke(data)
            }
        })
    }

    /**
     * Get the current push notification token (FCM token on Android, APNs token on iOS).
     * Returns null if not available yet.
     */
    suspend fun getPushToken(): String? {
        return NotifierManager.getPushNotifier().getToken()
    }

    /**
     * Delete the user's push notification token.
     * Useful when user logs out.
     */
    suspend fun deleteToken() {
        NotifierManager.getPushNotifier().deleteMyToken()
    }

    /**
     * Subscribe to a push notification topic.
     *
     * @param topic The topic name to subscribe to
     */
    suspend fun subscribeToTopic(topic: String) {
        NotifierManager.getPushNotifier().subscribeToTopic(topic)
    }

    /**
     * Unsubscribe from a push notification topic.
     *
     * @param topic The topic name to unsubscribe from
     */
    suspend fun unsubscribeFromTopic(topic: String) {
        NotifierManager.getPushNotifier().unSubscribeFromTopic(topic)
    }

    /**
     * Enable internal logging for debugging.
     */
    fun enableLogging() {
        NotifierManager.setLogger { message ->
            println("[KMPNotifier] $message")
        }
    }
}
