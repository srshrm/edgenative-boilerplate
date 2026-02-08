package com.aem.data

import androidx.compose.runtime.compositionLocalOf

/**
 * CompositionLocal to provide EdsConfig throughout the UI tree.
 * Used primarily for resolving relative URLs in images and links.
 */
val LocalEdsConfig = compositionLocalOf { DefaultEdsConfig }

