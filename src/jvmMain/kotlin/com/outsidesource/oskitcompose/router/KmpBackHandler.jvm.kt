package com.outsidesource.oskitcompose.router

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow

@Composable
actual fun KmpBackHandler(enabled: Boolean, onBack: () -> Unit) {}

@Composable
actual fun KmpPredictiveBackHandler(
    enabled: Boolean,
    onBack: suspend (Flow<IKmpBackEvent>) -> Unit
) {}