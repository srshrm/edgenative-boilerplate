package com.aem.blocks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aem.data.BlockColumn
import com.aem.data.SectionElement
import com.aem.theme.Spacing

/**
 * Renders a columns block with multiple columns of content.
 * Each row contains columns that are rendered side by side.
 */
@Composable
fun ColumnsBlock(
    block: SectionElement.Block,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.md)
    ) {
        block.rows.forEach { row ->
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

/**
 * Renders a single column by iterating its child elements.
 */
@Composable
private fun ColumnItem(
    column: BlockColumn,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        column.element.children().forEach { child ->
            ElementRenderer(
                element = child,
                onLinkClick = onLinkClick
            )
        }
    }
}
