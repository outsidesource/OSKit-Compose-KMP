package com.outsidesource.oskitcompose.modifier

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.*
import com.outsidesource.oskitcompose.pointer.awaitFirstDownEvent
import com.outsidesource.oskitcompose.pointer.awaitForUpOrCancellationEvent

fun Modifier.disablePointerInput(disabled: Boolean) = pointerInput(disabled) {
    if (!disabled) return@pointerInput

    awaitPointerEventScope {
        while (true) {
            awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
        }
    }
}

fun Modifier.consumePointerInput(pass: PointerEventPass = PointerEventPass.Main) = pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            awaitPointerEvent(pass).changes.forEach { it.consume() }
        }
    }
}

fun Modifier.preventClickPropagationToParent() = pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            awaitPointerEvent(PointerEventPass.Main).changes.forEach {
                if (it.changedToUp()) it.consume()
            }
        }
    }
}

expect fun Modifier.kmpPointerMoveFilter(
    onMove: (Offset) -> Boolean = { false },
    onEnter: () -> Boolean = { false },
    onExit: () -> Boolean = { false },
): Modifier

@ExperimentalComposeUiApi
inline fun Modifier.kmpMouseScrollFilter(
    vararg keys: Any? = emptyArray(),
    crossinline block: (event: PointerEvent, offset: Offset) -> Unit
): Modifier =
    pointerInput(keys) {
        while (true) {
            awaitPointerEventScope {
                val event = awaitPointerEvent()
                if (event.type == PointerEventType.Scroll) block(event, event.changes.first().scrollDelta)
            }
        }
    }

fun Modifier.kmpClickableEvent(vararg keys: Any, onClick: (down: PointerEvent, up: PointerEvent) -> Unit): Modifier =
    pointerInput(keys) {
        awaitEachGesture {
            val down =
                awaitFirstDownEvent().also { it.changes.forEach { change -> if (change.pressed != change.previousPressed) change.consume() } }
            val up = awaitForUpOrCancellationEvent() ?: return@awaitEachGesture
            onClick(down, up)
        }
    }

expect fun Modifier.kmpOnExternalDrag(
    enabled: Boolean = true,
    onDrop: (KMPExternalDragValue) -> Unit = {},
    onStarted: (KMPExternalDragValue) -> Unit = {},
    onEntered: (KMPExternalDragValue) -> Unit = {},
    onMoved: (KMPExternalDragValue) -> Unit = {},
    onExited: (KMPExternalDragValue) -> Unit = {},
    onChanged: (KMPExternalDragValue) -> Unit = {},
    onEnded: (KMPExternalDragValue) -> Unit = {},
): Modifier