package com.outsidesource.oskitcompose.systemui

import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.outsidesource.oskitcompose.context.findActivity

@Composable
actual fun KMPDisableScreenIdleTimeoutEffect(isEnabled: Boolean) {
    if (!isEnabled) return
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val window = context.findActivity()?.window ?: return@DisposableEffect onDispose { }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
}