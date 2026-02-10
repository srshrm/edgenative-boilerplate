package com.aem.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.browser.document
import org.w3c.dom.HTMLVideoElement

/**
 * wasmJs video player using HTML5 <video> element.
 *
 * Creates a native <video> element and overlays it on the Compose canvas.
 * The element is removed on dispose.
 */
@Composable
actual fun VideoPlayer(
    url: String,
    autoPlay: Boolean,
    loop: Boolean,
    muted: Boolean,
    modifier: Modifier,
) {
    val videoElementId = remember(url) { "video-${url.hashCode()}" }

    DisposableEffect(url, autoPlay, loop, muted) {
        val video = (document.createElement("video") as HTMLVideoElement).apply {
            id = videoElementId
            src = url
            this.autoplay = autoPlay
            this.loop = loop
            this.muted = muted
            playsInline = true
            setAttribute("playsinline", "true")
            style.apply {
                position = "absolute"
                top = "0"
                left = "0"
                width = "100%"
                height = "100%"
                objectFit = "cover"
                setProperty("pointer-events", "none")
                zIndex = "-1"
            }
        }

        // Append to the canvas container
        val container = document.getElementById("ComposeTarget")
        container?.appendChild(video)

        onDispose {
            val existing = document.getElementById(videoElementId)
            existing?.parentElement?.removeChild(existing)
        }
    }
}