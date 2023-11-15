package com.outsidesource.oskitcompose.modifier

import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.painter.Painter
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

@OptIn(ExperimentalComposeUiApi::class)
actual fun Modifier.kmpOnExternalDrag(
    enabled: Boolean,
    onDragStart: (KMPExternalDragValue) -> Unit,
    onDrag: (KMPExternalDragValue) -> Unit,
    onDragExit: () -> Unit,
    onDrop: (KMPExternalDragValue) -> Unit,
): Modifier = composed {
    onExternalDrag(
        enabled = enabled,
        onDragStart = { onDragStart(it.toKmpExternalDragValue()) },
        onDrag = { onDrag(it.toKmpExternalDragValue()) },
        onDragExit = onDragExit,
        onDrop = { onDrop(it.toKmpExternalDragValue()) },
    )
}

@OptIn(ExperimentalComposeUiApi::class)
private fun ExternalDragValue.toKmpExternalDragValue(): KMPExternalDragValue {
    return KMPExternalDragValue(
        dragPosition = dragPosition,
        dragData = dragData.toKmpDragData(),
    )
}

@OptIn(ExperimentalComposeUiApi::class)
private fun DragData.toKmpDragData(): KMPDragData {
    return when (this) {
        is DragData.FilesList -> object : KMPDragData.FilesList {
            override fun readFiles(): List<String> = this@toKmpDragData.readFiles()
        }
        is DragData.Image -> object : KMPDragData.Image {
            override fun readImage(): Painter = this@toKmpDragData.readImage()
        }
        is DragData.Text -> object : KMPDragData.Text {
            override val bestMimeType: String = this@toKmpDragData.bestMimeType
            override fun readText(): String = this@toKmpDragData.readText()
        }
        else -> object : KMPDragData.Unknown {
            override val value: Any = this@toKmpDragData
        }
    }
}