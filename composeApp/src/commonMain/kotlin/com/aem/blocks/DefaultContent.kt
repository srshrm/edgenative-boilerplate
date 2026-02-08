package com.aem.blocks

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
import com.aem.data.ContentParser
import com.aem.data.EdsSection
import com.aem.data.LocalEdsConfig
import com.aem.data.SectionElement
import com.aem.theme.Spacing
import com.fleeksoft.ksoup.nodes.Element

/**
 * Renders a complete EDS section, dispatching each element to
 * either the BlockRenderer or ElementRenderer.
 */
@Composable
fun SectionRenderer(
    section: EdsSection,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm)
    ) {
        section.elements.forEach { element ->
            when (element) {
                is SectionElement.Block -> {
                    BlockRenderer(
                        block = element,
                        onLinkClick = onLinkClick
                    )
                }

                is SectionElement.DefaultContent -> {
                    ElementRenderer(
                        element = element.element,
                        onLinkClick = onLinkClick,
                        modifier = Modifier.padding(horizontal = Spacing.md)
                    )
                }
            }
            Spacer(modifier = Modifier.height(Spacing.xs))
        }
    }
}

/**
 * Renders a single HTML element based on its tag name.
 * Handles headings, paragraphs, images, lists, links, and other content.
 */
@Composable
fun ElementRenderer(
    element: Element,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val edsConfig = LocalEdsConfig.current

    when (element.tagName().lowercase()) {
        "h1" -> {
            Text(
                text = element.text(),
                style = MaterialTheme.typography.headlineLarge,
                modifier = modifier.padding(bottom = Spacing.sm)
            )
        }

        "h2" -> {
            Text(
                text = element.text(),
                style = MaterialTheme.typography.headlineMedium,
                modifier = modifier.padding(bottom = Spacing.sm)
            )
        }

        "h3" -> {
            Text(
                text = element.text(),
                style = MaterialTheme.typography.headlineSmall,
                modifier = modifier.padding(bottom = Spacing.sm)
            )
        }

        "h4" -> {
            Text(
                text = element.text(),
                style = MaterialTheme.typography.titleLarge,
                modifier = modifier.padding(bottom = Spacing.sm)
            )
        }

        "h5" -> {
            Text(
                text = element.text(),
                style = MaterialTheme.typography.titleMedium,
                modifier = modifier.padding(bottom = Spacing.sm)
            )
        }

        "h6" -> {
            Text(
                text = element.text(),
                style = MaterialTheme.typography.titleSmall,
                modifier = modifier.padding(bottom = Spacing.sm)
            )
        }

        "p" -> {
            ParagraphRenderer(
                element = element,
                onLinkClick = onLinkClick,
                modifier = modifier
            )
        }

        "picture" -> {
            val imgSrc = ContentParser.extractImageUrl(element)
            val resolvedUrl = edsConfig.resolveUrl(imgSrc)
            resolvedUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = element.selectFirst("img")?.attr("alt"),
                    contentScale = ContentScale.FillWidth,
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(bottom = Spacing.sm)
                )
            }
        }

        "img" -> {
            val src = element.attr("src").takeIf { it.isNotBlank() }
            val resolvedUrl = edsConfig.resolveUrl(src)
            resolvedUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = element.attr("alt"),
                    contentScale = ContentScale.FillWidth,
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(bottom = Spacing.sm)
                )
            }
        }

        "ul" -> {
            Column(modifier = modifier.padding(vertical = Spacing.xs)) {
                element.children().filter { it.tagName() == "li" }.forEach { li ->
                    Row(modifier = Modifier.padding(vertical = Spacing.xs)) {
                        Text("\u2022 ", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(
                            text = li.text(),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        "ol" -> {
            Column(modifier = modifier.padding(vertical = Spacing.xs)) {
                element.children().filter { it.tagName() == "li" }.forEachIndexed { index, li ->
                    Row(modifier = Modifier.padding(vertical = Spacing.xs)) {
                        Text("${index + 1}. ", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(
                            text = li.text(),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        "a" -> {
            val href = element.attr("href")
            TextButton(
                onClick = { if (href.isNotBlank()) onLinkClick(href) }
            ) {
                Text(element.text())
            }
        }

        "div" -> {
            // Render children of plain divs
            Column(modifier = modifier) {
                element.children().forEach { child ->
                    ElementRenderer(
                        element = child,
                        onLinkClick = onLinkClick
                    )
                }
            }
        }

        else -> {
            // Fallback: render text content if present
            val text = element.text()
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

/**
 * Renders a <p> element, handling mixed inline content including
 * text, <strong>, <em>, <a>, <br>, and embedded <picture>/<img>.
 */
@Composable
fun ParagraphRenderer(
    element: Element,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val edsConfig = LocalEdsConfig.current

    // Check if the paragraph contains an image (picture or img)
    val picture = element.selectFirst("picture")
    val img = element.selectFirst("img")

    if (picture != null || (img != null && element.children().size == 1)) {
        // Image-only paragraph
        val imgSrc = if (picture != null) {
            ContentParser.extractImageUrl(picture)
        } else {
            img?.attr("src")?.takeIf { it.isNotBlank() }
        }
        val resolvedUrl = edsConfig.resolveUrl(imgSrc)
        resolvedUrl?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = (picture?.selectFirst("img") ?: img)?.attr("alt"),
                contentScale = ContentScale.FillWidth,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.sm)
            )
        }
        return
    }

    // Check if paragraph is just a link
    val link = element.selectFirst("a")
    if (link != null && element.text() == link.text()) {
        TextButton(
            onClick = { link.attr("href").takeIf { it.isNotBlank() }?.let(onLinkClick) }
        ) {
            Text(link.text())
        }
        return
    }

    // Regular text paragraph
    val text = element.text()
    if (text.isNotBlank()) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = modifier.padding(bottom = Spacing.sm)
        )
    }
}
