package com.aem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Cross-platform video player for self-hosted video content (.mp4, .webm).
 *
 * For YouTube/Vimeo embeds, use [EmbedVideoPlayer] (WebView-based) instead.
 *
 * By default, the player fills the available width and sizes its height to match
 * the video's native aspect ratio (16:9 fallback). Callers can override this
 * with a custom [modifier].
 *
 * @param url Direct video URL (e.g., https://example.com/video.mp4)
 * @param autoPlay Start playback automatically when ready
 * @param loop Restart playback when video ends
 * @param muted Play without audio (required for autoplay on most platforms)
 * @param showControls Show native playback controls (play/pause, seek, etc.)
 * @param modifier Compose modifier for sizing and layout
 */
@Composable
expect fun VideoPlayer(
    url: String,
    autoPlay: Boolean = false,
    loop: Boolean = false,
    muted: Boolean = false,
    showControls: Boolean = true,
    modifier: Modifier = Modifier,
)

/**
 * Returns true if the URL is a YouTube or Vimeo embed (requires WebView, not native player).
 */
fun isEmbedVideo(url: String): Boolean {
    val lower = url.lowercase()
    return lower.contains("youtube.com") ||
            lower.contains("youtu.be") ||
            lower.contains("vimeo.com")
}