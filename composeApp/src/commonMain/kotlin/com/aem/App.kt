package com.aem

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.aem.data.DefaultEdsConfig
import com.aem.data.EdsConfig
import com.aem.navigation.AppNavigation
import com.aem.theme.AppTheme
import com.aem.utils.openUrl

/**
 * Main application entry point.
 * Sets up the theme, navigation, and EDS configuration.
 *
 * @param edsConfig The EDS site configuration. Defaults to AEM Boilerplate.
 */
@Composable
@Preview
fun App(
    edsConfig: EdsConfig = DefaultEdsConfig
) {
    AppTheme {
        AppNavigation(
            edsConfig = edsConfig,
            onExternalLink = { url ->
                openUrl(url)
            }
        )
    }
}
