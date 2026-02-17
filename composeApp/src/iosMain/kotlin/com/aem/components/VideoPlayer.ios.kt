package com.aem.components

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.viewinterop.UIKitViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.AVFoundation.AVLayerVideoGravityResizeAspect
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerLayer
import platform.AVFoundation.currentItem
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.seekToTime
import platform.AVFoundation.setMuted
import platform.AVKit.AVPlayerViewController
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
    showControls: Boolean,
    modifier: Modifier,
) {
    val player = remember(url) {
        val nsUrl = NSURL.URLWithString(url) ?: return@remember null
        val playerItem = AVPlayerItem(uRL = nsUrl)
        AVPlayer(playerItem = playerItem).apply {
            setMuted(muted)
        }
    } ?: return

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

    val videoModifier = if (modifier == Modifier) {
        Modifier.fillMaxWidth().aspectRatio(16f / 9f)
    } else {
        modifier
    }

    if (showControls) {
        // AVPlayerViewController provides native iOS playback controls
        // (play/pause, scrubber, AirPlay, fullscreen, etc.)
        UIKitViewController(
            factory = {
                AVPlayerViewController().apply {
                    this.player = player
                    this.showsPlaybackControls = true
                }
            },
            modifier = videoModifier,
        )

        if (autoPlay) {
            DisposableEffect(url) {
                player.play()
                onDispose { }
            }
        }
    } else {
        // Raw AVPlayerLayer for chromeless background/hero video
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
                    videoGravity = AVLayerVideoGravityResizeAspect
                }
                container.layer.addSublayer(playerLayer)

                if (autoPlay) {
                    player.play()
                }

                container
            },
            modifier = videoModifier,
        )
    }
}
