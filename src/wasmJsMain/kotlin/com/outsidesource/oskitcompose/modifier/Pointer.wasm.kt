package com.outsidesource.oskitcompose.modifier

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.*

actual fun Modifier.kmpPointerMoveFilter(
    onMove: (Offset) -> Boolean,
    onEnter: () -> Boolean,
    onExit: () -> Boolean,
): Modifier = this.pointerInput(Unit) {
    awaitEachGesture {
        awaitFirstDown()
        onEnter()

        while (true) {
            val event = awaitPointerEvent().changes.first()
            if (event.changedToUp()) {
                onExit()
                break
            } else if (event.positionChanged()) {
                onMove(event.position)
            }
        }
    }
}

@Composable
actual fun Modifier.kmpOnExternalDragAndDrop(
    isEnabled: (KmpExternalDragEvent) -> Boolean,
    onStarted: (KmpExternalDragEvent) -> Unit,
    onEntered: (KmpExternalDragEvent) -> Unit,
    onMoved: (KmpExternalDragEvent) -> Unit,
    onChanged: (KmpExternalDragEvent) -> Unit,
    onDrop: (KmpExternalDropEvent) -> Boolean,
    onExited: (KmpExternalDragEvent) -> Unit,
    onEnded: (KmpExternalDragEvent) -> Unit,
): Modifier = this