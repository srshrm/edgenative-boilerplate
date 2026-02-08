package com.aem.blocks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.aem.data.BlockRow
import com.aem.data.ContentParser
import com.aem.data.LocalEdsConfig
import com.aem.data.SectionElement
import com.aem.theme.Spacing

/**
 * Renders a cards block as a grid of clickable cards.
 * Uses FlowRow instead of LazyVerticalGrid to avoid nested scrolling issues.
 *
 * Each row in the block represents a card item. Cards typically have:
 * - First column: image
 * - Second column: title (in <strong>) and description text
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CardsBlock(
    block: SectionElement.Block,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        block.rows.forEach { row ->
            Box(
                modifier = Modifier.widthIn(min = 160.dp, max = 320.dp)
            ) {
                CardItem(
                    row = row,
                    onLinkClick = onLinkClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun CardItem(
    row: BlockRow,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val edsConfig = LocalEdsConfig.current

    // Cards typically have image in first column, text in second
    val imageColumn = row.columns.firstOrNull()
    val textColumn = if (row.columns.size > 1) row.columns[1] else row.columns.firstOrNull()

    // Extract image URL
    val imageUrl = imageColumn?.element?.let { col ->
        ContentParser.extractImageUrl(col)?.let { edsConfig.resolveUrl(it) }
    }

    // Extract link for card click
    val link = textColumn?.element?.let { ContentParser.extractFirstLink(it) }
        ?: imageColumn?.element?.let { ContentParser.extractFirstLink(it) }

    // Extract title (typically in <p><strong>Title</strong></p>)
    val title = textColumn?.element?.let { col ->
        col.selectFirst("p strong")?.text()
            ?: col.selectFirst("p")?.text()
    }

    // Extract description (second <p> element)
    val description = textColumn?.element?.let { col ->
        val paragraphs = col.select("p")
        if (paragraphs.size > 1) paragraphs[1].text() else null
    }

    Card(
        onClick = { link?.first?.let(onLinkClick) },
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            imageUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                )
            }

            Column(
                modifier = Modifier.padding(Spacing.md)
            ) {
                title?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                }
            }
        }
    }
}
