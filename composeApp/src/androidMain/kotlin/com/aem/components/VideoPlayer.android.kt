package com.aem.components

import android.R.style.Theme_Black_NoTitleBar_Fullscreen
import android.app.Dialog
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.LocalActivity
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.aem.AppContextProvider

@OptIn(UnstableApi::class)
@Composable
actual fun VideoPlayer(
    url: String,
    autoPlay: Boolean,
    loop: Boolean,
    muted: Boolean,
    showControls: Boolean,
    modifier: Modifier,
) {
    val context = AppContextProvider.applicationContext
    val activity = LocalActivity.current

    var aspectRatio by remember { mutableFloatStateOf(16f / 9f) }

    val exoPlayer = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(url.toUri())
            setMediaItem(mediaItem)
            repeatMode = if (loop) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
            playWhenReady = autoPlay
            if (muted) volume = 0f
            prepare()
        }
    }

    // Track the fullscreen dialog so we can dismiss it on dispose
    var fullscreenDialog: Dialog? = remember { null }

    DisposableEffect(url) {
        val listener = object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                if (videoSize.width > 0 && videoSize.height > 0) {
                    aspectRatio = videoSize.width.toFloat() / videoSize.height.toFloat()
                }
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            fullscreenDialog?.dismiss()
            exoPlayer.release()
        }
    }

    val videoModifier = if (modifier == Modifier) {
        Modifier.fillMaxWidth().aspectRatio(aspectRatio)
    } else {
        modifier
    }

    AndroidView(
        factory = { ctx ->
            val inlinePlayerView = PlayerView(ctx).apply {
                player = exoPlayer
                useController = showControls
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            }

            if (showControls) {
                inlinePlayerView.setFullscreenButtonClickListener { isFullscreen ->
                    val act = activity ?: return@setFullscreenButtonClickListener

                    if (isFullscreen) {
                        // Create a second PlayerView for the dialog overlay
                        val fullscreenPlayerView = PlayerView(act).apply {
                            useController = true
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        }

                        val dialog = Dialog(
                            act,
                            Theme_Black_NoTitleBar_Fullscreen
                        ).apply {
                            setContentView(
                                FrameLayout(act).apply {
                                    addView(
                                        fullscreenPlayerView,
                                        FrameLayout.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT
                                        )
                                    )
                                }
                            )
                            setOnDismissListener {
                                // Hand the player back to the inline view
                                fullscreenPlayerView.player = null
                                inlinePlayerView.player = exoPlayer
                                fullscreenDialog = null
                            }
                            setCancelable(true)
                        }

                        // Wire up the exit-fullscreen button inside the dialog PlayerView
                        fullscreenPlayerView.setFullscreenButtonClickListener {
                            dialog.dismiss()
                        }

                        // Hand the player to the fullscreen view
                        inlinePlayerView.player = null
                        fullscreenPlayerView.player = exoPlayer

                        // Hide system bars for immersive experience
                        dialog.window?.let { w ->
                            val controller =
                                WindowCompat.getInsetsController(w, w.decorView)
                            controller.hide(WindowInsetsCompat.Type.systemBars())
                            controller.systemBarsBehavior =
                                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                        }

                        fullscreenDialog = dialog
                        dialog.show()
                    } else {
                        fullscreenDialog?.dismiss()
                    }
                }
            }

            inlinePlayerView
        },
        update = { playerView ->
            playerView.useController = showControls
        },
        modifier = videoModifier,
    )
}
