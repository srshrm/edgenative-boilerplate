package com.adobe.aem_kmp_boilerplate.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.adobe.aem_kmp_boilerplate.blocks.SectionRenderer
import com.adobe.aem_kmp_boilerplate.data.DefaultEdsConfig
import com.adobe.aem_kmp_boilerplate.data.EdsConfig
import com.adobe.aem_kmp_boilerplate.data.EdsPage
import com.adobe.aem_kmp_boilerplate.data.LocalEdsConfig
import com.adobe.aem_kmp_boilerplate.data.titleText
import com.adobe.aem_kmp_boilerplate.network.EdsApiService
import com.adobe.aem_kmp_boilerplate.theme.Spacing

/**
 * Page detail screen that displays any EDS page by path.
 * Reusable for navigating to different pages within the site.
 *
 * @param path The relative page path to display
 * @param edsConfig The EDS site configuration
 * @param onNavigate Callback for link navigation
 * @param onBack Callback for back navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageDetailScreen(
    path: String,
    edsConfig: EdsConfig = DefaultEdsConfig,
    onNavigate: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    var pageData by remember { mutableStateOf<EdsPage?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val apiService = remember { EdsApiService() }

    LaunchedEffect(path, edsConfig) {
        isLoading = true
        error = null

        apiService.fetchPage(edsConfig, path)
            .onSuccess { page ->
                pageData = page
                isLoading = false
            }
            .onFailure { e ->
                error = e.message ?: "Failed to load page"
                isLoading = false
            }
    }

    CompositionLocalProvider(LocalEdsConfig provides edsConfig) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = pageData?.metadata?.titleText ?: formatPathAsTitle(path),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { paddingValues ->
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
        page.content.forEach { sectionContainer ->
            SectionRenderer(
                contentGroups = sectionContainer.section,
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
