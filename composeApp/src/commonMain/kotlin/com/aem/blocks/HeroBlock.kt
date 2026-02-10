package com.aem.blocks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.aem.data.ContentParser
import com.aem.data.LocalEdsConfig
import com.aem.data.SectionElement
import com.aem.theme.Spacing

/**
 * Hero block variant based on EDS variations.
 */
enum class HeroVariant {
    Default,
    Small,
    Large,
    Centered
}

/**
 * Renders a hero block with image, title, subtitle, and optional CTA.
 * Extracts content from the block's HTML structure using CSS selectors.
 */
@Composable
fun HeroBlock(
    block: SectionElement.Block,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val edsConfig = LocalEdsConfig.current
    val variant = determineVariant(block.name, block.variants)
    val imageHeight = when (variant) {
        HeroVariant.Small -> 200.dp
        HeroVariant.Large -> 450.dp
        else -> 320.dp
    }

    // Extract content from the first row
    val firstRow = block.rows.firstOrNull()
    val imageColumn = firstRow?.columns?.firstOrNull()
    val textColumn = if ((firstRow?.columns?.size ?: 0) > 1) {
        firstRow?.columns?.get(1)
    } else {
        firstRow?.columns?.firstOrNull()
    }

    // Extract image URL
    val imageUrl = imageColumn?.element?.let { col ->
        ContentParser.extractImageUrl(col)?.let { edsConfig.resolveUrl(it) }
    }

    // Extract title from heading elements
    val title = textColumn?.element?.let { col ->
        col.selectFirst("h1, h2, h3")?.text()
    }

    // Extract subtitle from paragraph elements (skip ones with links)
    val subtitle = textColumn?.element?.let { col ->
        col.select("p").firstOrNull { p ->
            p.selectFirst("a") == null && p.selectFirst("picture") == null && p.text().isNotBlank()
        }?.text()
    }

    // Extract CTA link
    val cta = textColumn?.element?.let { col ->
        ContentParser.extractFirstLink(col)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(imageHeight)
    ) {
        // Background image
        imageUrl?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }

        // Gradient overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f)
                        ),
                        startY = 100f
                    )
                )
        )

        // Content overlay
        Column(
            modifier = Modifier
                .align(
                    when (variant) {
                        HeroVariant.Centered -> Alignment.Center
                        else -> Alignment.BottomStart
                    }
                )
                .padding(Spacing.lg),
            horizontalAlignment = when (variant) {
                HeroVariant.Centered -> Alignment.CenterHorizontally
                else -> Alignment.Start
            }
        ) {
            title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
            }

            subtitle?.let {
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            cta?.let { (href, text) ->
                Spacer(modifier = Modifier.height(Spacing.md))
                Button(
                    onClick = { onLinkClick(href) }
                ) {
                    Text(text.ifBlank { "Learn More" })
                }
            }
        }
    }
}

private fun determineVariant(name: String, variants: List<String>): HeroVariant {
    val all = (listOf(name) + variants).joinToString(" ").lowercase()
    return when {
        all.contains("small") -> HeroVariant.Small
        all.contains("large") -> HeroVariant.Large
        all.contains("centered") || all.contains("center") -> HeroVariant.Centered
        else -> HeroVariant.Default
    }
}
