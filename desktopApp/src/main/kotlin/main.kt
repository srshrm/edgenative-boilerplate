import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.adobe.aem_kmp_boilerplate.App
import com.mmk.kmpnotifier.extensions.composeDesktopResourcesPath
import com.mmk.kmpnotifier.notification.NotifierManager
import com.mmk.kmpnotifier.notification.configuration.NotificationPlatformConfiguration
import java.io.File

fun main() = application {
    // Initialize KMPNotifier for Desktop
    // Note: Place ic_notification.png in resources/common folder
    // See: https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Native_distributions_and_local_execution/README.md#packaging-resources
    NotifierManager.initialize(
        NotificationPlatformConfiguration.Desktop(
            showPushNotification = true,
            notificationIconPath = composeDesktopResourcesPath() + File.separator + "ic_notification.png"
        )
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "AEMKMPBoilerplate",
    ) {
        App()
    }
}
