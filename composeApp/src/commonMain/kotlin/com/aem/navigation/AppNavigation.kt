package com.aem.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.aem.blocks.DrawerNavContent
import com.aem.data.DefaultEdsConfig
import com.aem.data.EdsConfig
import com.aem.data.LocalPageCache
import com.aem.data.NavData
import com.aem.data.PageCache
import com.aem.data.rememberPageCache
import com.aem.network.EdsApiService
import com.aem.screens.EdsPageScreen
import kotlinx.coroutines.launch

/**
 * Main navigation host using Navigation 3 with NavDisplay.
 * Includes a global ModalNavigationDrawer with dynamically fetched nav content
 * from the EDS site's nav.plain.html.
 *
 * @param edsConfig The EDS site configuration
 * @param onExternalLink Callback for handling external links (opens browser)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    edsConfig: EdsConfig = DefaultEdsConfig,
    onExternalLink: (String) -> Unit = {}
) {
    val backStack = remember { mutableStateListOf<Any>(Home) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val pageCache = rememberPageCache()
    val apiService = remember { EdsApiService() }

    // Nav data state
    var navData by remember { mutableStateOf<NavData?>(null) }
    var navLoading by remember { mutableStateOf(true) }
    var navError by remember { mutableStateOf<String?>(null) }

    // Fetch nav data on first composition
    LaunchedEffect(edsConfig) {
        navLoading = true
        navError = null
        val result = apiService.fetchNav(edsConfig)
        result.fold(
            onSuccess = {
                navData = it
                navLoading = false
            },
            onFailure = {
                navError = it.message
                navLoading = false
            }
        )
    }

    CompositionLocalProvider(LocalPageCache provides pageCache) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    DrawerNavContent(
                        navData = navData,
                        isLoading = navLoading,
                        errorMessage = navError,
                        onNavigate = { path ->
                            scope.launch {
                                drawerState.close()
                                handleLinkClick(
                                    url = path,
                                    edsConfig = edsConfig,
                                    backStack = backStack,
                                    onExternalLink = onExternalLink
                                )
                            }
                        },
                        onClose = {
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        ) {
            AppNavigationContent(
                backStack = backStack,
                edsConfig = edsConfig,
                apiService = apiService,
                brandTitle = navData?.brand?.title,
                onExternalLink = onExternalLink,
                onMenuClick = {
                    scope.launch {
                        drawerState.open()
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppNavigationContent(
    backStack: SnapshotStateList<Any>,
    edsConfig: EdsConfig,
    apiService: EdsApiService,
    brandTitle: String?,
    onExternalLink: (String) -> Unit,
    onMenuClick: () -> Unit
) {
    val currentRoute = backStack.lastOrNull()
    val title = brandTitle ?: "AEM App"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    when (currentRoute) {
                        is Home -> {
                            IconButton(onClick = onMenuClick) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = "Menu"
                                )
                            }
                        }

                        is PageDetail -> {
                            IconButton(onClick = { backStack.removeLastOrNull() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }

                        else -> {
                            IconButton(onClick = onMenuClick) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = "Menu"
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Show menu button on detail pages
                    if (currentRoute is PageDetail) {
                        IconButton(onClick = onMenuClick) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        val onNavigate: (String) -> Unit = { url ->
            handleLinkClick(
                url = url,
                edsConfig = edsConfig,
                backStack = backStack,
                onExternalLink = onExternalLink
            )
        }

        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider = { route ->
                when (route) {
                    is Home -> NavEntry(route) {
                        EdsPageScreen(
                            cacheKey = PageCache.HOME_KEY,
                            edsConfig = edsConfig,
                            onNavigate = onNavigate,
                            paddingValues = paddingValues,
                            fetchPage = { apiService.fetchHomePage(edsConfig) }
                        )
                    }

                    is PageDetail -> NavEntry(route) {
                        EdsPageScreen(
                            cacheKey = route.path,
                            edsConfig = edsConfig,
                            onNavigate = onNavigate,
                            paddingValues = paddingValues,
                            fetchPage = { apiService.fetchPage(edsConfig, route.path) }
                        )
                    }

                    else -> NavEntry(Unit) {
                        EdsPageScreen(
                            cacheKey = PageCache.HOME_KEY,
                            edsConfig = edsConfig,
                            onNavigate = onNavigate,
                            paddingValues = paddingValues,
                            fetchPage = { apiService.fetchHomePage(edsConfig) }
                        )
                    }
                }
            }
        )
    }
}

/**
 * Handle a link click, determining whether to navigate internally or externally.
 */
private fun handleLinkClick(
    url: String,
    edsConfig: EdsConfig,
    backStack: SnapshotStateList<Any>,
    onExternalLink: (String) -> Unit
) {
    when {
        // Skip anchor links for now
        LinkHandler.isAnchorLink(url) -> {
            // TODO: Implement anchor scrolling
        }

        // Handle special protocols (mailto, tel) externally
        LinkHandler.isSpecialProtocol(url) -> {
            onExternalLink(url)
        }

        // Internal navigation
        LinkHandler.shouldNavigateInternally(url, edsConfig) -> {
            val path = LinkHandler.extractPath(url, edsConfig)
            if (path.isEmpty() || path == "index" || path == edsConfig.homePath) {
                // Navigate to home if not already there
                if (backStack.lastOrNull() !is Home) {
                    backStack.add(Home)
                }
            } else {
                backStack.add(PageDetail(path))
            }
        }

        // External link - open in browser
        else -> {
            onExternalLink(url)
        }
    }
}
