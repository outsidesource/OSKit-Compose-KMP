package com.outsidesource.oskitcompose.lib

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
actual fun KMPBackHandler(enabled: Boolean, onBack: () -> Unit) = BackHandler(enabled, onBack)
