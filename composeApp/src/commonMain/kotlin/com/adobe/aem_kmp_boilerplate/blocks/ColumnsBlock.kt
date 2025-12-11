package com.adobe.aem_kmp_boilerplate.blocks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.adobe.aem_kmp_boilerplate.data.BlockColumn
import com.adobe.aem_kmp_boilerplate.data.BlockRow
import com.adobe.aem_kmp_boilerplate.data.ContentNode
import com.adobe.aem_kmp_boilerplate.data.ContentParser
import com.adobe.aem_kmp_boilerplate.data.LocalEdsConfig
import com.adobe.aem_kmp_boilerplate.theme.Spacing

/**
 * Renders a columns block with multiple columns of content.
 */
@Composable
fun ColumnsBlock(
    rows: List<BlockRow>,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.md)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                row.columns.forEach { column ->
                    ColumnItem(
                        column = column,
                        onLinkClick = onLinkClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(Spacing.md))
        }
    }
}

@Composable
private fun ColumnItem(
    column: BlockColumn,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        column.items.forEach { item ->
            ContentNodeRenderer(
                node = item,
                onLinkClick = onLinkClick
            )
        }
    }
}

@Composable
fun ContentNodeRenderer(
    node: ContentNode,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val edsConfig = LocalEdsConfig.current

    when {
        node.isImage -> {
            edsConfig.resolveUrl(node.src)?.let { resolvedUrl ->
                AsyncImage(
                    model = resolvedUrl,
                    contentDescription = node.alt,
                    contentScale = ContentScale.FillWidth,
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(bottom = Spacing.sm)
                )
            }
        }

        node.isHeading -> {
            val style = when (node.level) {
                1 -> MaterialTheme.typography.headlineLarge
                2 -> MaterialTheme.typography.headlineMedium
                3 -> MaterialTheme.typography.headlineSmall
                4 -> MaterialTheme.typography.titleLarge
                5 -> MaterialTheme.typography.titleMedium
                6 -> MaterialTheme.typography.titleSmall
                else -> MaterialTheme.typography.headlineMedium
            }
            val text = node.text ?: ContentParser.extractPlainText(node.content)
            if (text.isNotBlank()) {
                Text(
                    text = text,
                    style = style,
                    modifier = modifier.padding(bottom = Spacing.sm)
                )
            }
        }

        node.isParagraph -> {
            val text = node.text ?: ContentParser.extractPlainText(node.content)
            if (text.isNotBlank()) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = modifier.padding(bottom = Spacing.sm)
                )
            }
            // Also check for nested links
            val link = ContentParser.findFirstLink(ContentParser.parseContentNodes(node.content))
            link?.let {
                TextButton(
                    onClick = { it.href?.let(onLinkClick) }
                ) {
                    Text(it.text ?: it.displayText ?: "")
                }
            }
        }

        node.isLink -> {
            TextButton(
                onClick = { node.href?.let(onLinkClick) }
            ) {
                Text(node.text ?: node.displayText ?: "")
            }
        }

        node.isList -> {
            Column(modifier = modifier.padding(vertical = Spacing.xs)) {
                node.items?.forEach { listItem ->
                    Row(modifier = Modifier.padding(vertical = Spacing.xs)) {
                        Text("• ", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(
                            text = listItem.text ?: "",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        node.isStyledText -> {
            val text = node.text ?: ContentParser.extractPlainText(node.content)
            if (text.isNotBlank()) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = modifier.padding(bottom = Spacing.sm)
                )
            }
        }

        else -> {
            val text = node.text ?: ContentParser.extractPlainText(node.content)
            if (text.isNotBlank()) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = modifier
                )
            }
        }
    }
}
