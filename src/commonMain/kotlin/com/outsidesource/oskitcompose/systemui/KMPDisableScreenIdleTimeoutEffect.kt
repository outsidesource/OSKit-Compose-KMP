package com.outsidesource.oskitcompose.systemui

import androidx.compose.runtime.Composable

/**
 * Disables the Screen Idle Timeout on Android and iOS preventing the screen from sleeping
 */
@Composable
expect fun KMPDisableScreenIdleTimeoutEffect(isEnabled: Boolean = true)