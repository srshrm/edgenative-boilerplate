package com.aem.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.aem.blocks.SectionRenderer
import com.aem.data.EdsConfig
import com.aem.data.EdsPage
import com.aem.data.LocalEdsConfig
import com.aem.data.LocalPageCache
import com.aem.theme.Spacing
import kotlinx.coroutines.launch

/**
 * Shared EDS page screen that handles fetching, caching, pull-to-refresh,
 * and rendering for any EDS page.
 *
 * @param cacheKey Key used to store/retrieve this page from the in-memory cache
 * @param edsConfig The EDS site configuration
 * @param onNavigate Callback for link navigation
 * @param paddingValues Padding values from parent Scaffold
 * @param fetchPage Suspend function that fetches the page content
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EdsPageScreen(
    cacheKey: String,
    edsConfig: EdsConfig,
    onNavigate: (String) -> Unit,
    paddingValues: PaddingValues = PaddingValues(),
    fetchPage: suspend () -> Result<EdsPage>
) {
    val pageCache = LocalPageCache.current
    val cachedPage = pageCache.get(cacheKey)

    var pageData by remember { mutableStateOf(cachedPage) }
    var isLoading by remember { mutableStateOf(cachedPage == null) }
    var isRefreshing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    suspend fun loadPage(forceRefresh: Boolean = false) {
        if (!forceRefresh && pageCache.contains(cacheKey)) {
            pageData = pageCache.get(cacheKey)
            isLoading = false
            return
        }

        if (!forceRefresh) {
            isLoading = true
        }
        error = null

        fetchPage()
            .onSuccess { page ->
                pageCache.put(cacheKey, page)
                pageData = page
                isLoading = false
                isRefreshing = false
            }
            .onFailure { e ->
                error = userFriendlyError(e)
                isLoading = false
                isRefreshing = false
            }
    }

    LaunchedEffect(cacheKey, edsConfig) {
        loadPage()
    }

    CompositionLocalProvider(LocalEdsConfig provides edsConfig) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                pageCache.remove(cacheKey)
                coroutineScope.launch { loadPage(forceRefresh = true) }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> LoadingContent()

                error != null -> ErrorContent(
                    message = error!!,
                    onRetry = {
                        isLoading = true
                        error = null
                        coroutineScope.launch { loadPage(forceRefresh = true) }
                    }
                )

                pageData != null -> PageContent(
                    page = pageData!!,
                    onLinkClick = onNavigate
                )
            }
        }
    }
}

@Composable
internal fun PageContent(
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
        page.sections.forEach { section ->
            SectionRenderer(
                section = section,
                onLinkClick = onLinkClick
            )
        }
    }
}

@Composable
internal fun LoadingContent(
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
internal fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Spacing.sm)
            )
            Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = Spacing.lg)
            ) {
                Text("Retry")
            }
        }
    }
}

/**
 * Maps raw exception to a user-friendly error message.
 */
private fun userFriendlyError(e: Throwable): String {
    val msg = e.message.orEmpty().lowercase()
    return when {
        "timeout" in msg -> "The request timed out. Please check your connection and try again."
        "unknown host" in msg || "unresolvedaddress" in msg ->
            "Could not reach the server. Please check your internet connection."

        "connect" in msg -> "Unable to connect. Please try again later."
        "404" in msg -> "Page not found."
        "500" in msg || "503" in msg -> "The server is temporarily unavailable. Please try again later."
        else -> "Failed to load the page. Please try again."
    }
}
