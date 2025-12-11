package com.adobe.aem_kmp_boilerplate.blocks

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
import com.adobe.aem_kmp_boilerplate.data.BlockRow
import com.adobe.aem_kmp_boilerplate.data.ContentParser
import com.adobe.aem_kmp_boilerplate.data.LocalEdsConfig
import com.adobe.aem_kmp_boilerplate.theme.Spacing

/**
 * Renders a cards block as a grid of clickable cards.
 * Uses FlowRow instead of LazyVerticalGrid to avoid nested scrolling issues.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CardsBlock(
    rows: List<BlockRow>,
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
        rows.forEach { row ->
            Box(
                modifier = Modifier.widthIn(min = 160.dp, max = 280.dp)
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
    val imageColumn = row.firstColumn
    val textColumn = row.secondColumn ?: row.firstColumn

    val imageUrl = edsConfig.resolveUrl(imageColumn?.firstImage?.src)
    val link = textColumn?.firstLink ?: imageColumn?.firstLink

    // Extract title (usually the first text with strong formatting)
    val title = textColumn?.items?.firstOrNull()?.let { node ->
        if (node.isParagraph) {
            // Check for nested strong text
            ContentParser.parseContentNodes(node.content).find { it.type == "strong" }?.text
                ?: ContentParser.extractPlainText(node.content)
        } else {
            node.text ?: ContentParser.extractPlainText(node.content)
        }
    }

    // Extract description (usually the second paragraph)
    val description = textColumn?.items?.drop(1)?.firstOrNull()?.let { node ->
        node.text ?: ContentParser.extractPlainText(node.content)
    }

    Card(
        onClick = { link?.href?.let(onLinkClick) },
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
