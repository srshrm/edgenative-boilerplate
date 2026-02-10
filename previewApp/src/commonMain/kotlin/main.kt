import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.window.ComposeViewport
import com.aem.App
import com.aem.initWasmImageLoader
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement

/**
 * Web entry point for AEM KMP Preview - WasmJS target.
 * Renders the App() composable inside a Pixel 10-style phone frame.
 *
 * Pixel 10: 1080×2424, 422 PPI → viewport ≈ 412dp wide.
 * Our CSS frame is 375px. Compose internally uses (375 × devicePixelRatio)
 * canvas pixels with default density = devicePixelRatio, giving 375dp viewport.
 * We adjust density so the viewport becomes 412dp to match the real device.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // Configure Coil image loader to use CORS proxy-aware HTTP client
    initWasmImageLoader()

    // Hide the loading spinner once Compose is ready
    (document.getElementById("loading") as? HTMLElement)?.style?.display = "none"

    val phoneScreen = document.getElementById("phone-screen")
        ?: error("Element #phone-screen not found in index.html")

    // Match Pixel 10 viewport: 412dp
    // Compose canvas = 375 CSS px × devicePixelRatio internal pixels
    // density = (375 × dpr) / 412 → gives exactly 412dp viewport width
    val dpr = window.devicePixelRatio.toFloat()
    val frameWidthCss = 375f
    val pixel10ViewportDp = 412f
    val matchedDensity = Density(
        density = (frameWidthCss * dpr) / pixel10ViewportDp,
        fontScale = 1f
    )

    ComposeViewport(phoneScreen) {
        CompositionLocalProvider(LocalDensity provides matchedDensity) {
            App()
        }
    }
}
