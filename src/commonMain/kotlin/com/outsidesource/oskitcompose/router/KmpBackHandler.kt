package com.outsidesource.oskitcompose.router

import androidx.compose.runtime.Composable

@Composable
expect fun KMPBackHandler(enabled: Boolean, onBack: () -> Unit)
