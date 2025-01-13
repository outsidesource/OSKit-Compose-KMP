package com.outsidesource.oskitcompose.modifier

import android.content.ClipData
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged

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

@OptIn(ExperimentalFoundationApi::class)
actual fun Modifier.kmpOnExternalDrag(
    enabled: Boolean,
    onDrop: (KMPExternalDragValue) -> Unit,
    onStarted: (KMPExternalDragValue) -> Unit,
    onEntered: (KMPExternalDragValue) -> Unit,
    onMoved: (KMPExternalDragValue) -> Unit,
    onExited: (KMPExternalDragValue) -> Unit,
    onChanged: (KMPExternalDragValue) -> Unit,
    onEnded: (KMPExternalDragValue) -> Unit,
): Modifier = dragAndDropTarget(
    shouldStartDragAndDrop = { enabled },
    target = object : DragAndDropTarget {
        override fun onDrop(event: DragAndDropEvent): Boolean {
            onDrop(event.toKmpExternalDragValue())
            return true
        }

        override fun onStarted(event: DragAndDropEvent) {
            super.onStarted(event)
            onStarted(event.toKmpExternalDragValue())
        }

        override fun onEntered(event: DragAndDropEvent) {
            super.onEntered(event)
            onEntered(event.toKmpExternalDragValue())
        }

        override fun onMoved(event: DragAndDropEvent) {
            super.onMoved(event)
            onMoved(event.toKmpExternalDragValue())
        }

        override fun onExited(event: DragAndDropEvent) {
            super.onExited(event)
            onExited(event.toKmpExternalDragValue())
        }

        override fun onChanged(event: DragAndDropEvent) {
            super.onChanged(event)
            onChanged(event.toKmpExternalDragValue())
        }

        override fun onEnded(event: DragAndDropEvent) {
            super.onEnded(event)
            onEnded(event.toKmpExternalDragValue())
        }
    }
)

private fun DragAndDropEvent.toKmpExternalDragValue(): KMPExternalDragValue {
    val dragEvent = this.toAndroidDragEvent()
    val position = Offset(x = dragEvent.x, y = dragEvent.y)
    val dragData = dragEvent.clipData?.toKmpDragData() ?: object : KMPDragData.Unknown {
        override val value: Any = "Unknown Drag Data"
    }

    return KMPExternalDragValue(
        dragPosition = position,
        dragData = dragData,
    )
}

private fun ClipData.toKmpDragData(): KMPDragData {
    return when {
        itemCount > 0 && getItemAt(0).text != null -> object : KMPDragData.Text {
            override val bestMimeType: String = "text/plain"
            override fun readText(): String = getItemAt(0).text.toString()
        }

        else -> object : KMPDragData.Unknown {
            override val value: Any = this@toKmpDragData
        }
    }
}
