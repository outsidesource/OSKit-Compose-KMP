package com.outsidesource.oskitcompose.systemui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.outsidesource.oskitkmp.lib.AndroidKmpScreenWakeLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

@Composable
actual fun KmpScreenWakeLockEffect(isEnabled: Boolean) {
    if (!isEnabled) return
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val wakeLock = AndroidKmpScreenWakeLock(context)
        scope.launch { wakeLock.acquire() }
        onDispose { scope.launch { wakeLock.release() } }
    }
}