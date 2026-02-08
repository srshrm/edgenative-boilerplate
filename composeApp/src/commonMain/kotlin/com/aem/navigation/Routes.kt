package com.aem.navigation

import kotlinx.serialization.Serializable

/**
 * Navigation routes for the app using Navigation 3.
 * All routes are type-safe and serializable.
 */

/**
 * Home screen route - displays the site's home page.
 */
@Serializable
object Home

/**
 * Page detail route - displays any EDS page by path.
 *
 * @param path The relative page path (e.g., "products/item1")
 */
@Serializable
data class PageDetail(val path: String)

