package com.outsidesource.oskitcompose.router

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.PredictiveBackHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Composable
actual fun KmpBackHandler(enabled: Boolean, onBack: () -> Unit) {}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun KmpPredictiveBackHandler(
    enabled: Boolean,
    onBack: suspend (Flow<IKmpBackEvent>) -> Unit
) = PredictiveBackHandler(enabled) { flow ->
    val mappedFlow = flow.map {
        KmpBackEvent(
            progress = it.progress,
            touchX = it.touchX,
            touchY = it.touchY,
            swipeEdge = it.swipeEdge,
        )
    }
    onBack(mappedFlow)
}