package com.outsidesource.oskitcompose.systemui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import platform.UIKit.UIApplication

@Composable
actual fun KMPDisableScreenIdleTimeoutEffect(isEnabled: Boolean) {
    if (!isEnabled) return

    DisposableEffect(Unit) {
        UIApplication.sharedApplication.setIdleTimerDisabled(true)
        onDispose { UIApplication.sharedApplication.setIdleTimerDisabled(false) }
    }
}