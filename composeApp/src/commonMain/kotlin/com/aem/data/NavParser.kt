package com.aem.data

import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element

/**
 * Parses EDS nav.plain.html documents into structured [NavData] models.
 *
 * The nav.plain.html has a consistent structure:
 * - First <div>: Brand section with a <p><a> containing the site name
 * - Second <div>: Navigation section with nested <ul>/<li> for categories and items
 * - Third <div> (optional): Tools section (e.g., search icon)
 *
 * Example nav.plain.html:
 * ```html
 * <div>
 *   <p><a href="/">Boilerplate</a></p>
 * </div>
 * <div>
 *   <ul>
 *     <li>Example Content
 *       <ul>
 *         <li><a href="/">Default Content</a></li>
 *       </ul>
 *     </li>
 *   </ul>
 * </div>
 * <div>
 *   <p><span class="icon icon-search"></span></p>
 * </div>
 * ```
 */
object NavParser {

    /**
     * Parse a ksoup Document (from nav.plain.html) into [NavData].
     *
     * @param doc The parsed HTML document
     * @return The structured NavData with brand and sections
     */
    fun parseNav(doc: Document): NavData {
        val body = doc.body()
        val topDivs = body.children().filter { it.tagName() == "div" }

        if (topDivs.isEmpty()) return NavData()

        // First div: Brand section
        val brand = if (topDivs.isNotEmpty()) parseBrand(topDivs[0]) else null

        // Second div: Navigation sections
        val sections = if (topDivs.size > 1) parseSections(topDivs[1]) else emptyList()

        return NavData(brand = brand, sections = sections)
    }

    /**
     * Parse the brand section (first div).
     * Looks for a <p><a> structure with the site name.
     *
     * @param brandDiv The first div element
     * @return NavBrand with title and href, or null if not found
     */
    private fun parseBrand(brandDiv: Element): NavBrand? {
        val link = brandDiv.selectFirst("a") ?: return null
        val title = link.text().trim()
        val href = link.attr("href").takeIf { it.isNotBlank() } ?: "/"

        return if (title.isNotBlank()) {
            NavBrand(title = title, href = href)
        } else {
            null
        }
    }

    /**
     * Parse the navigation sections (second div).
     * The structure is a <ul> with top-level <li> elements representing categories.
     * Each category <li> has:
     * - Text content as the category title
     * - A nested <ul> with <li><a> items as sub-links
     *
     * @param navDiv The second div element containing the navigation
     * @return List of NavSection with their child items
     */
    private fun parseSections(navDiv: Element): List<NavSection> {
        val sections = mutableListOf<NavSection>()

        // Find the top-level <ul>
        val topUl = navDiv.selectFirst("ul") ?: return emptyList()

        // Each direct <li> child of the top <ul> is a category
        for (li in topUl.children().filter { it.tagName() == "li" }) {
            val section = parseNavSection(li)
            if (section != null) {
                sections.add(section)
            }
        }

        return sections
    }

    /**
     * Parse a single navigation section from a top-level <li>.
     *
     * The <li> structure:
     * ```html
     * <li>Category Title
     *   <ul>
     *     <li><a href="/path">Item Title</a></li>
     *   </ul>
     * </li>
     * ```
     *
     * @param li The top-level list item element
     * @return NavSection with title and items, or null if invalid
     */
    private fun parseNavSection(li: Element): NavSection? {
        // The category title is the text node directly in the <li>,
        // excluding text from nested elements like <ul>
        val title = extractDirectText(li)
        if (title.isBlank()) return null

        // Find the nested <ul> for sub-items
        val subUl = li.selectFirst("ul")
        val items = if (subUl != null) {
            subUl.children()
                .filter { it.tagName() == "li" }
                .mapNotNull { parseNavItem(it) }
        } else {
            emptyList()
        }

        return NavSection(title = title, items = items)
    }

    /**
     * Parse a single navigation item from a sub-level <li>.
     *
     * @param li The list item element containing a link
     * @return NavItem with title and href, or null if no valid link found
     */
    private fun parseNavItem(li: Element): NavItem? {
        val link = li.selectFirst("a") ?: return null
        val title = link.text().trim()
        val href = link.attr("href").takeIf { it.isNotBlank() } ?: return null

        return if (title.isNotBlank()) {
            NavItem(title = title, href = href)
        } else {
            null
        }
    }

    /**
     * Extract only the direct text content of an element,
     * excluding text from child elements.
     *
     * For example, given:
     * ```html
     * <li>Category Title
     *   <ul><li><a>Sub Item</a></li></ul>
     * </li>
     * ```
     * This returns "Category Title" (not "Category Title Sub Item").
     *
     * @param element The element to extract direct text from
     * @return The direct text content, trimmed
     */
    private fun extractDirectText(element: Element): String {
        return element.textNodes()
            .joinToString(" ") { it.text() }
            .trim()
    }
}
