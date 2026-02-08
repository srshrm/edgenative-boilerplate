import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.aem.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "AEMKMPBoilerplate",
    ) {
        App()
    }
}
