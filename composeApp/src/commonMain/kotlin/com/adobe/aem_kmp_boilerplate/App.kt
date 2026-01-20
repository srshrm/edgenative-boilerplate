package com.adobe.aem_kmp_boilerplate

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.adobe.aem_kmp_boilerplate.data.DefaultEdsConfig
import com.adobe.aem_kmp_boilerplate.data.EdsConfig
import com.adobe.aem_kmp_boilerplate.navigation.AppNavigation
import com.adobe.aem_kmp_boilerplate.theme.AemAppTheme
import com.adobe.aem_kmp_boilerplate.utils.openUrl

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
    AemAppTheme {
        AppNavigation(
            edsConfig = edsConfig,
            onExternalLink = { url ->
                openUrl(url)
            }
        )
    }
}
