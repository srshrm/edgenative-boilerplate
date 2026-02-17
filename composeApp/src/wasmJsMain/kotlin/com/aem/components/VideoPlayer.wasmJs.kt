@file:OptIn(ExperimentalWasmJsInterop::class)

package com.aem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * wasmJs video player stub — tapping opens a fullscreen HTML video overlay.
 *
 * Compose WASM renders to a canvas, so HTML elements can't participate in
 * Compose z-ordering. Instead, we show a Compose-native play button and,
 * on click, create a fullscreen HTML video that covers everything.
 * Pressing the close button or Escape dismisses it.
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
            .background(Color.Black)
            .clickable { openFullscreenVideo(url, loop, muted) },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play video",
            tint = Color.White,
            modifier = Modifier.size(64.dp),
        )
    }
}

@JsFun(
    """(url, loop, muted) => {
    // Backdrop covers everything
    var backdrop = document.createElement('div');
    backdrop.id = '__video_fullscreen__';
    backdrop.style.cssText = 'position:fixed;top:0;left:0;width:100%;height:100%;background:#000;z-index:99999;display:flex;align-items:center;justify-content:center';

    // Close button
    var closeBtn = document.createElement('button');
    closeBtn.textContent = '\u2715';
    closeBtn.style.cssText = 'position:absolute;top:16px;right:16px;z-index:100000;background:rgba(255,255,255,0.2);border:none;color:#fff;font-size:24px;width:44px;height:44px;border-radius:50%;cursor:pointer';
    closeBtn.onclick = function() { backdrop.remove(); };
    backdrop.appendChild(closeBtn);

    // Video element
    var video = document.createElement('video');
    video.src = url;
    video.autoplay = true;
    video.loop = loop;
    video.muted = muted;
    video.controls = true;
    video.playsInline = true;
    video.style.cssText = 'max-width:100%;max-height:100%;outline:none';
    backdrop.appendChild(video);

    // Escape key to close
    function onKey(e) {
        if (e.key === 'Escape') { backdrop.remove(); document.removeEventListener('keydown', onKey); }
    }
    document.addEventListener('keydown', onKey);

    // Append inside the Shadow DOM so it renders above the Compose canvas.
    // ComposeViewport attaches a shadowRoot to its host element; light DOM
    // children of body are hidden behind it.
    var target = null;
    var all = document.querySelectorAll('*');
    for (var i = 0; i < all.length; i++) {
        var sr = all[i].shadowRoot;
        if (sr && sr.querySelector('canvas')) { target = sr.querySelector('canvas').parentElement; break; }
    }
    (target || document.body).appendChild(backdrop);
}"""
)
private external fun openFullscreenVideo(url: String, loop: Boolean, muted: Boolean)
