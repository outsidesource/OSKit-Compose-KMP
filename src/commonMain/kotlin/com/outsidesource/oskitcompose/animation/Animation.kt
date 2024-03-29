package com.outsidesource.oskitcompose.animation

import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.unit.DpOffset

@Composable
fun animateDpOffsetAsState(
    targetValue: DpOffset,
    animationSpec: AnimationSpec<DpOffset> = tween(400),
    finishedListener: ((DpOffset) -> Unit)? = null
): State<DpOffset> {
    return animateValueAsState(
        targetValue, DpOffset.VectorConverter, animationSpec, finishedListener = finishedListener
    )
}