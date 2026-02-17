package com.aem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign

/**
 * Desktop video player placeholder.
 *
 * Native video on JVM desktop requires JavaFX or VLCJ which add significant dependencies.
 * For production use, consider integrating javafx-media or vlcj-javafx.
 */
@Composable
actual fun VideoPlayer(
    url: String,
    autoPlay: Boolean,
    loop: Boolean,
    muted: Boolean,
    showControls: Boolean,
    modifier: Modifier,
) {
    val videoModifier = if (modifier == Modifier) {
        Modifier.fillMaxWidth().aspectRatio(16f / 9f)
    } else {
        modifier
    }

    Box(
        modifier = videoModifier
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Video: $url",
            color = Color.White,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
