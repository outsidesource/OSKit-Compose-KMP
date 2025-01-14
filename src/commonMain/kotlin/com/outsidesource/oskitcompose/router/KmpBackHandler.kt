package com.outsidesource.oskitcompose.router

import androidx.compose.runtime.Composable

@Composable
expect fun KmpBackHandler(enabled: Boolean, onBack: () -> Unit)
