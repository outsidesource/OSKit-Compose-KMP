package com.outsidesource.oskitcompose.router

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow

@Composable
actual fun KmpBackHandler(
    enabled: Boolean,
    onCancel: () -> Unit,
    onProgress: (KmpBackProgressEvent) -> Unit,
    onBackComplete: () -> Unit,
) {
}