package com.outsidesource.oskitcompose.modifier

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.ui.*
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.dragData
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.draganddrop.DragData as DragAndDropData

@OptIn(ExperimentalComposeUiApi::class)
actual fun Modifier.kmpPointerMoveFilter(
    onMove: (Offset) -> Boolean,
    onEnter: () -> Boolean,
    onExit: () -> Boolean,
): Modifier = onPointerEvent(PointerEventType.Move) { onMove(it.changes.first().position) }
    .onPointerEvent(PointerEventType.Enter) { onEnter() }
    .onPointerEvent(PointerEventType.Exit) { onExit() }


@OptIn(ExperimentalFoundationApi::class)
actual fun Modifier.kmpOnExternalDrag(
    enabled: Boolean,
    onDragStart: (KMPExternalDragValue) -> Unit,
    onDrag: (KMPExternalDragValue) -> Unit,
    onDragExit: () -> Unit,
    onDrop: (KMPExternalDragValue) -> Unit,
): Modifier = composed {
    dragAndDropTarget(
        shouldStartDragAndDrop = { enabled },
        target = object : DragAndDropTarget {
            override fun onDrop(event: DragAndDropEvent): Boolean {
                onDrop(event.toKmpExternalDragValue())
                return true
            }

            override fun onStarted(event: DragAndDropEvent) {
                super.onStarted(event)
                onDragStart(event.toKmpExternalDragValue())
            }

            override fun onMoved(event: DragAndDropEvent) {
                super.onMoved(event)
                onDrag(event.toKmpExternalDragValue())
            }

            override fun onExited(event: DragAndDropEvent) {
                super.onExited(event)
                onDragExit()
            }
        }
    )
}


@OptIn(ExperimentalComposeUiApi::class)
private fun DragAndDropEvent.toKmpExternalDragValue(): KMPExternalDragValue {
    return KMPExternalDragValue(
        dragPosition = Offset(0f,0f),
        dragData = this.dragData().toKmpDragData(),
    )
}


@OptIn(ExperimentalComposeUiApi::class)
private fun DragAndDropData.toKmpDragData(): KMPDragData {
    return when (this) {
        is DragAndDropData.FilesList -> object : KMPDragData.FilesList {
            override fun readFiles(): List<String> = this@toKmpDragData.readFiles()
        }
        is DragAndDropData.Image -> object : KMPDragData.Image {
            override fun readImage(): Painter = this@toKmpDragData.readImage()
        }
        is DragAndDropData.Text -> object : KMPDragData.Text {
            override val bestMimeType: String = this@toKmpDragData.bestMimeType
            override fun readText(): String = this@toKmpDragData.readText()
        }
        else -> object : KMPDragData.Unknown {
            override val value: Any = this@toKmpDragData
        }
    }
}