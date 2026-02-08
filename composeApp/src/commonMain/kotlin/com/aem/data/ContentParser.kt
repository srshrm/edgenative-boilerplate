package com.aem.data

import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element

/**
 * Parses EDS plain HTML documents into structured [EdsPage] models.
 *
 * The plain HTML format (.plain.html) has a consistent structure:
 * - Top-level <div> elements represent sections
 * - <div class="blockname"> elements represent named blocks (columns, cards, hero, etc.)
 * - <div class="section-metadata"> contains key-value pairs for section styling
 * - Other elements (h1-h6, p, picture, ul, ol, etc.) are default content
 */
object ContentParser {

    /**
     * Parse a ksoup Document into an EdsPage.
     *
     * @param doc The parsed HTML document
     * @param baseUrl The base URL of the EDS site for resolving relative URLs
     * @return The structured EdsPage
     */
    fun parseDocument(doc: Document, baseUrl: String): EdsPage {
        val title = doc.title().takeIf { it.isNotBlank() }

        // Top-level divs in the body are sections
        val body = doc.body()
        val sectionDivs = body.children().filter { it.tagName() == "div" }

        val sections = sectionDivs
            .map { parseSection(it) }
            .filter { it.elements.isNotEmpty() }

        return EdsPage(title = title, sections = sections)
    }

    /**
     * Parse a top-level div into an EdsSection.
     *
     * @param sectionDiv The section's root div element
     * @return The parsed EdsSection with style and content elements
     */
    fun parseSection(sectionDiv: Element): EdsSection {
        val metadata = extractSectionMetadata(sectionDiv)
        val elements = mutableListOf<SectionElement>()

        for (child in sectionDiv.children()) {
            when {
                // Skip section-metadata divs (already extracted into metadata map)
                child.tagName() == "div" && child.hasClass("section-metadata") -> {
                    // Already processed
                }

                // Named block: a div with CSS class(es)
                child.tagName() == "div" && child.className().isNotBlank() -> {
                    val classes = child.classNames().toList()
                    val name = classes.first()
                    val variants = classes.drop(1)
                    val rows = parseBlockRows(child)
                    elements.add(
                        SectionElement.Block(
                            name = name,
                            variants = variants,
                            rows = rows
                        )
                    )
                }

                // Default content: headings, paragraphs, images, lists, etc.
                else -> {
                    elements.add(SectionElement.DefaultContent(element = child))
                }
            }
        }

        return EdsSection(metadata = metadata, elements = elements)
    }

    /**
     * Parse block rows from a block div.
     * Direct child <div> elements of the block are rows.
     * Each row's child <div> elements are columns.
     *
     * @param blockDiv The block div element
     * @return List of BlockRow containing BlockColumns
     */
    fun parseBlockRows(blockDiv: Element): List<BlockRow> {
        return blockDiv.children()
            .filter { it.tagName() == "div" }
            .map { rowDiv ->
                val columns = rowDiv.children()
                    .filter { it.tagName() == "div" }
                    .map { colDiv -> BlockColumn(element = colDiv) }

                // If the row has no div children, treat the row itself as a single column
                if (columns.isEmpty()) {
                    BlockRow(columns = listOf(BlockColumn(element = rowDiv)))
                } else {
                    BlockRow(columns = columns)
                }
            }
    }

    /**
     * Extract all key-value pairs from the section-metadata block.
     *
     * Section metadata is structured as:
     * <div class="section-metadata">
     *   <div>
     *     <div>Style</div>
     *     <div>highlight</div>
     *   </div>
     *   <div>
     *     <div>mobile-alignment</div>
     *     <div>center</div>
     *   </div>
     *   <div>
     *     <div><em>teaser-target-id</em></div>  <!-- keys can be wrapped in <em> -->
     *     <div>homepage-carousel</div>
     *   </div>
     * </div>
     *
     * Keys are lowercased for consistent lookup.
     * Duplicate keys have their values joined with ", ".
     *
     * @param sectionDiv The section div to search for metadata
     * @return Map of all metadata key-value pairs, empty if none found
     */
    fun extractSectionMetadata(sectionDiv: Element): Map<String, String> {
        val metadataDiv = sectionDiv.children()
            .firstOrNull { it.tagName() == "div" && it.hasClass("section-metadata") }
            ?: return emptyMap()

        val metadata = mutableMapOf<String, String>()

        for (row in metadataDiv.children()) {
            if (row.tagName() != "div") continue
            val cells = row.children().filter { it.tagName() == "div" }
            if (cells.size >= 2) {
                val key = cells[0].text().trim().lowercase()
                val value = cells[1].text().trim()
                if (key.isNotBlank()) {
                    // Handle duplicate keys by joining values with ", "
                    val existing = metadata[key]
                    metadata[key] = if (existing != null) "$existing, $value" else value
                }
            }
        }

        return metadata
    }

    /**
     * Extract the best image URL from an element.
     * Looks for <img> tags inside <picture> elements, falling back to direct <img> tags.
     *
     * @param element The element to search within
     * @return The image source URL, or null if not found
     */
    fun extractImageUrl(element: Element): String? {
        val img = element.selectFirst("img") ?: return null
        return img.attr("src").takeIf { it.isNotBlank() }
    }

    /**
     * Extract plain text content from an element, stripping all HTML tags.
     *
     * @param element The element to extract text from
     * @return The plain text content
     */
    fun extractText(element: Element): String {
        return element.text()
    }

    /**
     * Extract all links from an element.
     *
     * @param element The element to search within
     * @return List of pairs (href, text) for each link found
     */
    fun extractLinks(element: Element): List<Pair<String, String>> {
        return element.select("a").map { link ->
            link.attr("href") to link.text()
        }
    }

    /**
     * Extract the first link from an element.
     *
     * @param element The element to search within
     * @return Pair of (href, text), or null if no link found
     */
    fun extractFirstLink(element: Element): Pair<String, String>? {
        val link = element.selectFirst("a") ?: return null
        val href = link.attr("href").takeIf { it.isNotBlank() } ?: return null
        return href to link.text()
    }
}
