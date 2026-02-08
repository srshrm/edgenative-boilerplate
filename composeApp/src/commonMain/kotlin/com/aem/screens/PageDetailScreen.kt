package com.aem.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.aem.blocks.SectionRenderer
import com.aem.data.DefaultEdsConfig
import com.aem.data.EdsConfig
import com.aem.data.EdsPage
import com.aem.data.LocalEdsConfig
import com.aem.data.LocalPageCache
import com.aem.network.EdsApiService
import com.aem.theme.Spacing

/**
 * Page detail screen that displays any EDS page by path.
 * Reusable for navigating to different pages within the site.
 *
 * @param path The relative page path to display
 * @param edsConfig The EDS site configuration
 * @param onNavigate Callback for link navigation
 * @param paddingValues Padding values from parent Scaffold
 */
@Composable
fun PageDetailScreen(
    path: String,
    edsConfig: EdsConfig = DefaultEdsConfig,
    onNavigate: (String) -> Unit = {},
    paddingValues: PaddingValues = PaddingValues()
) {
    val pageCache = LocalPageCache.current
    val cachedPage = pageCache.get(path)

    var pageData by remember { mutableStateOf(cachedPage) }
    var isLoading by remember { mutableStateOf(cachedPage == null) }
    var error by remember { mutableStateOf<String?>(null) }

    val apiService = remember { EdsApiService() }

    LaunchedEffect(path, edsConfig) {
        // Skip fetch if already cached
        if (pageCache.contains(path)) {
            pageData = pageCache.get(path)
            isLoading = false
            return@LaunchedEffect
        }

        isLoading = true
        error = null

        apiService.fetchPage(edsConfig, path)
            .onSuccess { page ->
                pageCache.put(path, page)
                pageData = page
                isLoading = false
            }
            .onFailure { e ->
                error = e.message ?: "Failed to load page"
                isLoading = false
            }
    }

    CompositionLocalProvider(LocalEdsConfig provides edsConfig) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    LoadingContent()
                }

                error != null -> {
                    ErrorContent(
                        message = error!!,
                        onRetry = {
                            isLoading = true
                            error = null
                        }
                    )
                }

                pageData != null -> {
                    PageContent(
                        page = pageData!!,
                        onLinkClick = onNavigate
                    )
                }
            }
        }
    }
}

@Composable
private fun PageContent(
    page: EdsPage,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Render all sections
        page.sections.forEach { section ->
            SectionRenderer(
                section = section,
                onLinkClick = onLinkClick
            )
        }
    }
}

@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Error",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.sm)
            )
            TextButton(
                onClick = onRetry,
                modifier = Modifier.padding(top = Spacing.md)
            ) {
                Text("Retry")
            }
        }
    }
}

/**
 * Format a path as a title for display.
 */
private fun formatPathAsTitle(path: String): String {
    return path
        .split("/")
        .lastOrNull()
        ?.replace("-", " ")
        ?.replace("_", " ")
        ?.split(" ")
        ?.joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercaseChar() }
        }
        ?: "Page"
}
