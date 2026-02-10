package com.aem.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.aem.data.DefaultEdsConfig
import com.aem.data.EdsConfig
import com.aem.data.LocalPageCache
import com.aem.data.PageCache
import com.aem.data.rememberPageCache
import com.aem.network.EdsApiService
import com.aem.screens.EdsPageScreen
import kotlinx.coroutines.launch

/**
 * Main navigation host using Navigation 3 with NavDisplay.
 * Includes a global ModalNavigationDrawer accessible from all screens.
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

    CompositionLocalProvider(LocalPageCache provides pageCache) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    DrawerContent(
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
                        }
                    )
                }
            }
        ) {
            AppNavigationContent(
                backStack = backStack,
                edsConfig = edsConfig,
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
    onExternalLink: (String) -> Unit,
    onMenuClick: () -> Unit
) {
    val currentRoute = backStack.lastOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AEM App",
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
        val apiService = remember { EdsApiService() }
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
 * Drawer content with navigation items.
 */
@Composable
private fun DrawerContent(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp)
    ) {
        // Drawer Header
        Text(
            text = "AEM App",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp)
        )

        HorizontalDivider()

        Spacer(modifier = Modifier.height(8.dp))

        // Navigation Items
        NavigationDrawerItem(
            label = { Text("Home") },
            selected = false,
            onClick = { onNavigate("/") },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            label = { Text("About") },
            selected = false,
            onClick = { onNavigate("/about") },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            label = { Text("Contact") },
            selected = false,
            onClick = { onNavigate("/contact") },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
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
