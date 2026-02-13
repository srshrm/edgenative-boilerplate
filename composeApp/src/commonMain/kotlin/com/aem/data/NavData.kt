package com.aem.data

/**
 * Represents parsed navigation data from nav.plain.html.
 *
 * The nav.plain.html structure has three sections:
 * 1. Brand section - site name/logo with link
 * 2. Navigation sections - categories with child links
 * 3. Tools section - icons like search (optional)
 *
 * @param brand The brand/site name and link
 * @param sections The navigation sections (categories with items)
 */
data class NavData(
    val brand: NavBrand? = null,
    val sections: List<NavSection> = emptyList()
)

/**
 * The brand/logo portion of the navigation.
 *
 * @param title The brand/site name (e.g., "Boilerplate")
 * @param href The brand link URL (usually home "/")
 */
data class NavBrand(
    val title: String,
    val href: String = "/"
)

/**
 * A navigation section/category containing child items.
 * Represents a top-level <li> in the nav structure.
 *
 * In the EDS mobile nav, section titles appear as bold category headers
 * with their child items listed below.
 *
 * @param title The section/category title (e.g., "Getting Started")
 * @param items The child navigation items in this section
 */
data class NavSection(
    val title: String,
    val items: List<NavItem> = emptyList()
)

/**
 * A single navigation link item.
 *
 * @param title The display text (e.g., "Architecture")
 * @param href The link URL
 */
data class NavItem(
    val title: String,
    val href: String
)
