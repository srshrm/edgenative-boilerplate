package com.adobe.aem_kmp_boilerplate.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adobe.aem_kmp_boilerplate.blocks.SectionRenderer
import com.adobe.aem_kmp_boilerplate.data.DefaultEdsConfig
import com.adobe.aem_kmp_boilerplate.data.EdsConfig
import com.adobe.aem_kmp_boilerplate.data.EdsPage
import com.adobe.aem_kmp_boilerplate.data.LocalEdsConfig
import com.adobe.aem_kmp_boilerplate.data.titleText
import com.adobe.aem_kmp_boilerplate.network.EdsApiService
import com.adobe.aem_kmp_boilerplate.theme.Spacing
import kotlinx.coroutines.launch

/**
 * Home screen that displays the EDS site's home page.
 *
 * @param edsConfig The EDS site configuration
 * @param onNavigate Callback for link navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    edsConfig: EdsConfig = DefaultEdsConfig,
    onNavigate: (String) -> Unit = {}
) {
    var pageData by remember { mutableStateOf<EdsPage?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val apiService = remember { EdsApiService() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(edsConfig) {
        isLoading = true
        error = null

        apiService.fetchHomePage(edsConfig)
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
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    DrawerContent(
                        onNavigate = { path ->
                            scope.launch {
                                drawerState.close()
                                onNavigate(path)
                            }
                        }
                    )
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = pageData?.metadata?.titleText ?: "AEM App",
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = "Menu"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
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
            androidx.compose.material3.TextButton(
                onClick = onRetry,
                modifier = Modifier.padding(top = Spacing.md)
            ) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun DrawerContent(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
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
