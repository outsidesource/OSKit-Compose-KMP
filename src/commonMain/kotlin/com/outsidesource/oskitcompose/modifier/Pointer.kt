package com.outsidesource.oskitcompose.modifier

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.*
import com.outsidesource.oskitcompose.pointer.awaitFirstDownEvent
import com.outsidesource.oskitcompose.pointer.awaitForUpOrCancellationEvent
import okio.Source

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

/**
 * [kmpOnExternalDragAndDrop] Simplifies dragging and dropping objects from outside the application. For now only Desktop
 * is supported
 */
@Composable
expect fun Modifier.kmpOnExternalDragAndDrop(
    isEnabled: (KmpExternalDragEvent) -> Boolean,
    onStarted: (KmpExternalDragEvent) -> Unit = {},
    onEntered: (KmpExternalDragEvent) -> Unit = {},
    onMoved: (KmpExternalDragEvent) -> Unit = {},
    onChanged: (KmpExternalDragEvent) -> Unit = {},
    onDrop: (KmpExternalDropEvent) -> Boolean = { false },
    onExited: (KmpExternalDragEvent) -> Unit = {},
    onEnded: (KmpExternalDragEvent) -> Unit = {},
): Modifier

data class KmpExternalDragEvent(
    val position: Offset,
)

data class KmpExternalDropEvent(
    val position: Offset,
    val data: KmpExternalDropData,
)

sealed class KmpExternalDropData {
    data class Files(val files: List<KmpExternalFile>) : KmpExternalDropData()
    data class Text(val text: String) : KmpExternalDropData()
    data object Unknown : KmpExternalDropData()
}

data class KmpExternalFile(
    val name: String,
    val path: String,
    val source: Source,
)