package com.outsidesource.oskitcompose.systemui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import kotlinx.coroutines.*
import kotlin.js.Promise

private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

/***
 *
 */
@Composable
actual fun KMPDisableScreenIdleTimeoutEffect(isEnabled: Boolean) {
    if (!isEnabled) return

    DisposableEffect(Unit) {
        var wakeLock: WakeLockSentinel? = null

        scope.launch {
            try {
                wakeLock = requestWakeLock().await<WakeLockSentinel?>()
            } catch (t: Throwable) {
                // Ignore
            }
        }

        onDispose {
            scope.launch {
                try {
                    wakeLock?.release()?.await<JsAny?>()
                } catch (t: Throwable) {
                    // Ignore
                }
            }
        }
    }
}

private fun requestWakeLock(): Promise<WakeLockSentinel?> = js(
    """{
        try {
            if (navigator["wakeLock"] === undefined) return Promise.resolve(null);
            return navigator.wakeLock.request("screen");
        } catch (err) {
            return Promise.resolve(null);
        }
    }"""
)

private external interface WakeLockSentinel : JsAny {
    val released: Boolean
    val type: String
    fun release(): Promise<JsAny?>
}