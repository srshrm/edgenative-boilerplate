package com.aem.components

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.aem.AppContextProvider

@OptIn(UnstableApi::class)
@Composable
actual fun VideoPlayer(
    url: String,
    autoPlay: Boolean,
    loop: Boolean,
    muted: Boolean,
    modifier: Modifier,
) {
    val context = AppContextProvider.applicationContext

    val exoPlayer = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(url.toUri())
            setMediaItem(mediaItem)
            repeatMode = if (loop) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
            playWhenReady = autoPlay
            if (muted) {
                volume = 0f
            }
            videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            prepare()
        }
    }

    DisposableEffect(url) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
            }
        },
        modifier = modifier,
    )
}