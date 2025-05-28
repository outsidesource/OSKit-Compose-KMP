package com.outsidesource.oskitcompose.router

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Composable
actual fun KmpBackHandler(enabled: Boolean, onBack: () -> Unit) = BackHandler(enabled, onBack)

@SuppressLint("NoCollectCallFound")
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