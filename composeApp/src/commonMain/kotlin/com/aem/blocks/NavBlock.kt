package com.aem.blocks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aem.data.NavData
import com.aem.data.NavItem
import com.aem.data.NavSection
import com.aem.theme.Spacing

/**
 * Drawer navigation content that renders parsed NavData from nav.plain.html.
 *
 * Matches the EDS mobile navigation style:
 * - Header row with close (X) button and brand title
 * - Bold section/category titles
 * - Regular-weight clickable link items under each section
 *
 * @param navData The parsed navigation data (null while loading)
 * @param isLoading Whether the nav data is still being fetched
 * @param errorMessage Error message if nav fetch failed (null on success)
 * @param onNavigate Callback when a nav link is clicked (receives href)
 * @param onClose Callback to close the drawer
 * @param modifier Optional modifier
 */
@Composable
fun DrawerNavContent(
    navData: NavData?,
    isLoading: Boolean,
    errorMessage: String?,
    onNavigate: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header: Close button + Brand title
        DrawerHeader(
            brandTitle = navData?.brand?.title,
            onClose = onClose
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.xl),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            }

            errorMessage != null -> {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(
                        horizontal = Spacing.lg,
                        vertical = Spacing.md
                    )
                )
            }

            navData != null -> {
                NavSections(
                    sections = navData.sections,
                    onNavigate = onNavigate
                )
            }
        }
    }
}

/**
 * Drawer header with close (X) button and brand title.
 * Matches EDS mobile nav: X button on left, bold title next to it.
 */
@Composable
private fun DrawerHeader(
    brandTitle: String?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = Spacing.xs, end = Spacing.md, top = Spacing.sm, bottom = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close navigation",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = brandTitle ?: "",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Renders the list of navigation sections with their items.
 */
@Composable
private fun NavSections(
    sections: List<NavSection>,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(top = Spacing.sm)) {
        for (section in sections) {
            NavSectionItem(
                section = section,
                onNavigate = onNavigate
            )
        }
    }
}

/**
 * A single navigation section: bold title + list of clickable items.
 * Matches the EDS mobile nav style.
 */
@Composable
private fun NavSectionItem(
    section: NavSection,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Section title - bold, non-clickable
        Text(
            text = section.title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
        )

        // Section items - regular weight, clickable
        for (item in section.items) {
            NavLinkItem(
                item = item,
                onNavigate = onNavigate
            )
        }

        Spacer(modifier = Modifier.height(Spacing.xs))
    }
}

/**
 * A single clickable navigation link item.
 */
@Composable
private fun NavLinkItem(
    item: NavItem,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = item.title,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onNavigate(item.href) }
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
    )
}
