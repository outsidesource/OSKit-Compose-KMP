package com.outsidesource.oskitcompose.systemui

import androidx.compose.runtime.Composable

/**
 * Disables the Screen Idle Timeout preventing the screen from sleeping
 *
 * Android: Supported
 * iOS: Supported
 * JVM: Not supported
 * Web: Supported on all browsers that support navigator.wakeLock
 */
@Composable
expect fun KMPDisableScreenIdleTimeoutEffect(isEnabled: Boolean = true)