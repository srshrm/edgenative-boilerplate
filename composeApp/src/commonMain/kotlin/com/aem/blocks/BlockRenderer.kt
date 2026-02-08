package com.aem.blocks

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aem.data.LocalEdsConfig
import com.aem.data.SectionElement

/**
 * Renders an EDS block based on its name.
 * Dispatches to the appropriate block composable by matching the CSS class name.
 *
 * @param block The block to render (identified by CSS class from plain HTML)
 * @param onLinkClick Callback for link clicks
 * @param modifier Modifier for the block
 */
@Composable
fun BlockRenderer(
    block: SectionElement.Block,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val blockName = block.name.lowercase()

    when {
        // Skip footer and navigation blocks
        blockName in SKIP_BLOCKS -> {
            // Skip rendering
        }

        // Hero block
        blockName.contains("hero") -> {
            HeroBlock(
                block = block,
                onLinkClick = onLinkClick,
                modifier = modifier
            )
        }

        // Columns block
        blockName.contains("columns") -> {
            ColumnsBlock(
                block = block,
                onLinkClick = onLinkClick,
                modifier = modifier
            )
        }

        // Cards block
        blockName.contains("cards") || blockName.contains("card") -> {
            CardsBlock(
                block = block,
                onLinkClick = onLinkClick,
                modifier = modifier
            )
        }

        // Fragment block - fetches and renders content from another URL
        blockName == "fragment" -> {
            val edsConfig = LocalEdsConfig.current
            FragmentBlock(
                block = block,
                edsConfig = edsConfig,
                onLinkClick = onLinkClick,
                modifier = modifier
            )
        }

        // Default fallback - render as generic block
        else -> {
            GenericBlock(
                block = block,
                onLinkClick = onLinkClick,
                modifier = modifier
            )
        }
    }
}

/**
 * Blocks that should be skipped during rendering.
 * header/nav block is handled separately by the screen's top app bar.
 */
val SKIP_BLOCKS: Set<String> = setOf("footer", "navigation", "header", "nav")
