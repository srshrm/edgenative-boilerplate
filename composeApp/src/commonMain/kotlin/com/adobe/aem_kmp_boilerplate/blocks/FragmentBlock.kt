package com.adobe.aem_kmp_boilerplate.blocks

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
import com.adobe.aem_kmp_boilerplate.data.BlockRow
import com.adobe.aem_kmp_boilerplate.data.ContentParser
import com.adobe.aem_kmp_boilerplate.data.EdsConfig
import com.adobe.aem_kmp_boilerplate.data.EdsPage
import com.adobe.aem_kmp_boilerplate.network.EdsApiService

/**
 * Renders an EDS fragment block by fetching its content at runtime.
 *
 * Fragment blocks contain a link to another page/fragment that should be
 * loaded and rendered inline. The fragment JSON is fetched via the same
 * JSON conversion service used for regular pages.
 *
 * @param rows The parsed block content containing the fragment link
 * @param edsConfig The EDS configuration for URL resolution
 * @param onLinkClick Callback for link clicks within fragment content
 * @param modifier Modifier for the fragment container
 */
@Composable
fun FragmentBlock(
    rows: List<BlockRow>,
    edsConfig: EdsConfig,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Extract fragment href from the nested content structure
    val fragmentHref = remember(rows) {
        extractFragmentHref(rows)
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

        // Resolve the fragment path (handle relative URLs)
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
                // Show loading indicator
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
                // Show error (or silently skip in production)
                // For production, you might want to just return and skip rendering
                Text(
                    text = "Fragment unavailable",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp)
                )
            }

            fragmentPage != null -> {
                // Render fragment content
                fragmentPage!!.content.forEach { sectionContainer ->
                    SectionRenderer(
                        contentGroups = sectionContainer.section,
                        onLinkClick = onLinkClick
                    )
                }
            }
        }
    }
}

/**
 * Extract the fragment href from the block's nested content structure.
 *
 * Fragment blocks have a structure like:
 * content: [ [ [ { type: "link", href: "/path/to/fragment", text: "..." } ] ] ]
 */
private fun extractFragmentHref(rows: List<BlockRow>): String? {
    // Navigate the nested structure to find the first link
    for (row in rows) {
        for (column in row.columns) {
            for (item in column.items) {
                // Check if this item is a link
                if (item.isLink && !item.href.isNullOrBlank()) {
                    return item.href
                }
                // Check nested content for links
                val nestedNodes = ContentParser.parseContentNodes(item.content)
                val nestedLink = nestedNodes.find { it.isLink && !it.href.isNullOrBlank() }
                if (nestedLink != null) {
                    return nestedLink.href
                }
            }
        }
    }
    return null
}

/**
 * Resolve fragment path for fetching.
 *
 * Handles both relative paths (/path/to/fragment) and absolute URLs.
 * Returns the path portion suitable for EdsApiService.fetchPage().
 */
private fun resolveFragmentPath(href: String, edsConfig: EdsConfig): String {
    return when {
        // Absolute URL - extract path
        href.startsWith("http://") || href.startsWith("https://") -> {
            try {
                // Extract path from URL
                val path = href.substringAfter("://")
                    .substringAfter("/", "")
                path
            } catch (e: Exception) {
                href.trimStart('/')
            }
        }
        // Relative path - use as-is (trim leading slash for consistency)
        href.startsWith("/") -> href.trimStart('/')
        // Other - use as-is
        else -> href
    }
}
