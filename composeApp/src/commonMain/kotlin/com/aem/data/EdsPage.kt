package com.aem.data

import com.fleeksoft.ksoup.nodes.Element

/**
 * Represents a parsed EDS page from plain HTML content.
 *
 * @param title The page title extracted from the HTML document
 * @param sections The list of content sections parsed from top-level divs
 */
data class EdsPage(
    val title: String? = null,
    val sections: List<EdsSection> = emptyList()
)

/**
 * Represents a section within an EDS page.
 * Each section corresponds to a top-level <div> in the plain HTML.
 *
 * @param metadata All key-value pairs from the section-metadata block.
 *   Keys are lowercased for consistent lookup. Duplicate keys have values joined with ", ".
 *   Common keys include: style, type, mobile-alignment etc.
 * @param elements The list of content elements within this section
 */
data class EdsSection(
    val metadata: Map<String, String> = emptyMap(),
    val elements: List<SectionElement> = emptyList()
) {
    /** Convenience accessor for the "style" metadata value. */
    val style: String? get() = metadata["style"]
}

/**
 * Represents an element within a section.
 * Can be either a named block (identified by CSS class) or default content.
 */
sealed class SectionElement {
    /**
     * A named EDS block (e.g., columns, cards, hero).
     * Identified by the CSS class on the div element.
     *
     * @param name The primary block name (first CSS class)
     * @param variants Additional CSS classes representing block variants
     * @param rows The rows within the block, each containing columns
     */
    data class Block(
        val name: String,
        val variants: List<String>,
        val rows: List<BlockRow>
    ) : SectionElement()

    /**
     * Default content outside of any named block (headings, paragraphs, images, etc.).
     *
     * @param element The ksoup Element representing this content
     */
    data class DefaultContent(
        val element: Element
    ) : SectionElement()
}

/**
 * Represents a row within a block.
 * Each row is a direct child <div> of the block element.
 *
 * @param columns The columns within this row
 */
data class BlockRow(
    val columns: List<BlockColumn> = emptyList()
)

/**
 * Represents a column within a block row.
 * Each column is a child <div> within a row.
 *
 * @param element The ksoup Element containing the column content
 */
data class BlockColumn(
    val element: Element
)
