package com.aem.blocks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aem.data.ContentParser
import com.aem.data.EdsConfig
import com.aem.data.EdsPage
import com.aem.data.SectionElement
import com.aem.network.EdsApiService

/**
 * Renders an EDS fragment block by fetching its content at runtime.
 *
 * Fragment blocks contain a link to another page/fragment that should be
 * loaded and rendered inline. The fragment's plain HTML is fetched and
 * parsed using the same pipeline as regular pages.
 *
 * @param block The fragment block containing the link to the fragment page
 * @param edsConfig The EDS configuration for URL resolution
 * @param onLinkClick Callback for link clicks within fragment content
 * @param modifier Modifier for the fragment container
 */
@Composable
fun FragmentBlock(
    block: SectionElement.Block,
    edsConfig: EdsConfig,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Extract fragment href from the block content
    val fragmentHref = remember(block) {
        extractFragmentHref(block)
    }

    if (fragmentHref == null) {
        // No href found - skip rendering
        return
    }

    // State for fragment content
    var fragmentPage by remember { mutableStateOf<EdsPage?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val apiService = remember { EdsApiService() }

    // Fetch fragment content
    LaunchedEffect(fragmentHref, edsConfig) {
        isLoading = true
        error = null

        // Resolve the fragment path
        val fragmentPath = resolveFragmentPath(fragmentHref, edsConfig)

        apiService.fetchPage(edsConfig, fragmentPath)
            .onSuccess { page ->
                fragmentPage = page
                isLoading = false
            }
            .onFailure { e ->
                error = e.message
                isLoading = false
            }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            }

            error != null -> {
                Text(
                    text = "Fragment unavailable",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp)
                )
            }

            fragmentPage != null -> {
                // Render fragment content
                fragmentPage!!.sections.forEach { section ->
                    SectionRenderer(
                        section = section,
                        onLinkClick = onLinkClick
                    )
                }
            }
        }
    }
}

/**
 * Extract the fragment href from the block's content.
 * Searches through rows/columns for the first link.
 */
private fun extractFragmentHref(block: SectionElement.Block): String? {
    for (row in block.rows) {
        for (column in row.columns) {
            val link = ContentParser.extractFirstLink(column.element)
            if (link != null) return link.first
        }
    }
    return null
}

/**
 * Resolve fragment path for fetching.
 * Handles both relative paths (/path/to/fragment) and absolute URLs.
 * Returns the path portion suitable for EdsApiService.fetchPage().
 */
private fun resolveFragmentPath(href: String, edsConfig: EdsConfig): String {
    return when {
        // Absolute URL - extract path
        href.startsWith("http://") || href.startsWith("https://") -> {
            try {
                val path = href.substringAfter("://").substringAfter("/", "")
                path
            } catch (_: Exception) {
                href.trimStart('/')
            }
        }
        // Relative path - trim leading slash
        href.startsWith("/") -> href.trimStart('/')
        // Other - use as-is
        else -> href
    }
}
