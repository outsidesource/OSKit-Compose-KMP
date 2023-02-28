package com.outsidesource.oskitcompose.modifier

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent

@OptIn(ExperimentalComposeUiApi::class)
actual fun Modifier.kmpPointerMoveFilter(
    onMove: (Offset) -> Boolean,
    onEnter: () -> Boolean,
    onExit: () -> Boolean,
): Modifier = onPointerEvent(PointerEventType.Move) { onMove(it.changes.first().position) }
    .onPointerEvent(PointerEventType.Enter) { onEnter() }
    .onPointerEvent(PointerEventType.Exit) { onExit() }
