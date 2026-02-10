package com.aem.blocks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aem.data.SectionElement
import com.aem.theme.Spacing

/**
 * Generic block renderer for unrecognized block types.
 * Renders all content in a simple vertical layout within a card.
 */
@Composable
fun GenericBlock(
    block: SectionElement.Block,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.md),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md)
        ) {
            // Block name as header (for debugging/development)
            if (block.name.isNotBlank()) {
                Text(
                    text = block.name.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = Spacing.sm)
                )
            }

            // Render all content rows
            block.rows.forEach { row ->
                row.columns.forEach { column ->
                    column.element.children().forEach { child ->
                        ElementRenderer(
                            element = child,
                            onLinkClick = onLinkClick
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.sm))
            }
        }
    }
}
