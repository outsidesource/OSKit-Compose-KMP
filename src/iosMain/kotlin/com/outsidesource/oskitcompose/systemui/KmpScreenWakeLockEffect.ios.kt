package com.outsidesource.oskitcompose.systemui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.outsidesource.oskitkmp.lib.IosKmpScreenWakeLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import platform.UIKit.UIApplication

private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

@Composable
actual fun KmpScreenWakeLockEffect(isEnabled: Boolean) {
    if (!isEnabled) return

    DisposableEffect(Unit) {
        val wakeLock = IosKmpScreenWakeLock()
        scope.launch { wakeLock.acquire() }
        onDispose { scope.launch { wakeLock.release() } }
    }
}