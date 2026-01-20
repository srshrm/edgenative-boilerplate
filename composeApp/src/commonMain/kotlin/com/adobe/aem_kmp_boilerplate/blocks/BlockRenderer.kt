package com.adobe.aem_kmp_boilerplate.blocks

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adobe.aem_kmp_boilerplate.data.ContentNode
import com.adobe.aem_kmp_boilerplate.data.ContentParser
import com.adobe.aem_kmp_boilerplate.data.LocalEdsConfig

/**
 * Renders an EDS block based on its name.
 * Dispatches to the appropriate block composable.
 *
 * @param block The block ContentNode to render (must have type="block")
 * @param onLinkClick Callback for link clicks
 * @param modifier Modifier for the block
 */
@Composable
fun BlockRenderer(
    block: ContentNode,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!block.isBlock) return

    val blockName = block.name?.lowercase() ?: return
    val blockContent = ContentParser.parseBlockContent(block.content)

    when {
        // Skip footer and navigation blocks
        blockName == "footer" || blockName == "navigation" -> {
            // Skip rendering
        }

        // Hero block
        blockName.contains("hero") -> {
            HeroBlock(
                blockName = blockName,
                rows = blockContent,
                onLinkClick = onLinkClick,
                modifier = modifier
            )
        }

        // Columns block
        blockName.contains("columns") -> {
            ColumnsBlock(
                rows = blockContent,
                onLinkClick = onLinkClick,
                modifier = modifier
            )
        }

        // Cards block
        blockName.contains("cards") || blockName.contains("card") -> {
            CardsBlock(
                rows = blockContent,
                onLinkClick = onLinkClick,
                modifier = modifier
            )
        }

        // Header - convert to navigation info (handled separately)
        blockName == "header" || blockName == "nav" -> {
            // Header is typically handled by the screen's top app bar
            // Skip direct rendering here
        }

        // Fragment block - fetches and renders content from another URL
        blockName == "fragment" -> {
            val edsConfig = LocalEdsConfig.current
            FragmentBlock(
                rows = blockContent,
                edsConfig = edsConfig,
                onLinkClick = onLinkClick,
                modifier = modifier
            )
        }

        // Default fallback - render as generic block
        else -> {
            GenericBlock(
                blockName = blockName,
                rows = blockContent,
                onLinkClick = onLinkClick,
                modifier = modifier
            )
        }
    }
}

/**
 * Blocks that should be skipped during rendering.
 */
val SKIP_BLOCKS = setOf("footer", "navigation")

/**
 * Check if a block should be rendered.
 */
fun shouldRenderBlock(block: ContentNode): Boolean {
    if (!block.isBlock) return false
    return block.name?.lowercase() !in SKIP_BLOCKS
}
