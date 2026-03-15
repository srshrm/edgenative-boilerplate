import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.aem.App
import com.aem.initImageLoader
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

/**
 * Web entry point for EdgeNative Preview — WasmJS target.
 * Renders the App() composable full-page into document.body.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initImageLoader()

    (document.getElementById("loading") as? HTMLElement)?.style?.display = "none"

    ComposeViewport {
        App()
    }
}
