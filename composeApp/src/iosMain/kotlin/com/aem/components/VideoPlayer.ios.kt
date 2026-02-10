package com.aem.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerLayer
import platform.AVFoundation.currentItem
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.seekToTime
import platform.AVFoundation.setMuted
import platform.CoreGraphics.CGRectZero
import platform.CoreMedia.CMTimeMake
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURL
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VideoPlayer(
    url: String,
    autoPlay: Boolean,
    loop: Boolean,
    muted: Boolean,
    modifier: Modifier,
) {
    val player = remember(url) {
        val nsUrl = NSURL.URLWithString(url) ?: return@remember null
        val playerItem = AVPlayerItem(uRL = nsUrl)
        AVPlayer(playerItem = playerItem).apply {
            setMuted(muted)
        }
    } ?: return

    // Loop support via notification
    DisposableEffect(url, loop) {
        val observer = if (loop) {
            NSNotificationCenter.defaultCenter.addObserverForName(
                name = AVPlayerItemDidPlayToEndTimeNotification,
                `object` = player.currentItem,
                queue = null,
            ) { _ ->
                player.seekToTime(CMTimeMake(value = 0, timescale = 1))
                player.play()
            }
        } else null

        onDispose {
            observer?.let {
                NSNotificationCenter.defaultCenter.removeObserver(it)
            }
            player.pause()
        }
    }

    UIKitView(
        factory = {
            val container = object : UIView(frame = CGRectZero.readValue()) {
                override fun layoutSubviews() {
                    super.layoutSubviews()
                    val playerLayer = layer.sublayers?.firstOrNull() as? AVPlayerLayer
                    playerLayer?.setFrame(bounds)
                }
            }
            val playerLayer = AVPlayerLayer.playerLayerWithPlayer(player).apply {
                videoGravity = AVLayerVideoGravityResizeAspectFill
            }
            container.layer.addSublayer(playerLayer)

            // Start playback
            if (autoPlay) {
                player.play()
            }

            container
        },
        modifier = modifier,
    )
}