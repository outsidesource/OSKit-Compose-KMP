package com.outsidesource.oskitcompose.modifier

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import okio.FileSystem
import okio.Path.Companion.toPath
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.dnd.DropTargetDragEvent
import java.io.File

@OptIn(ExperimentalComposeUiApi::class)
actual fun Modifier.kmpPointerMoveFilter(
    onMove: (Offset) -> Boolean,
    onEnter: () -> Boolean,
    onExit: () -> Boolean,
): Modifier = onPointerEvent(PointerEventType.Move) { onMove(it.changes.first().position) }
    .onPointerEvent(PointerEventType.Enter) { onEnter() }
    .onPointerEvent(PointerEventType.Exit) { onExit() }

@OptIn(ExperimentalFoundationApi::class)
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
): Modifier {
    val target = remember(isEnabled, onStarted, onEntered, onMoved, onChanged, onDrop, onExited, onEnded) {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) = onStarted(event.toKmpExternalDragEvent())
            override fun onEnded(event: DragAndDropEvent) = onEnded(event.toKmpExternalDragEvent())
            override fun onDrop(event: DragAndDropEvent) = onDrop(event.toKmpExternalDropEvent())
            override fun onEntered(event: DragAndDropEvent) = onEntered(event.toKmpExternalDragEvent())
            override fun onMoved(event: DragAndDropEvent) = onMoved(event.toKmpExternalDragEvent())
            override fun onExited(event: DragAndDropEvent) = onExited(event.toKmpExternalDragEvent())
            override fun onChanged(event: DragAndDropEvent) = onChanged(event.toKmpExternalDragEvent())
        }
    }

    return dragAndDropTarget(
        shouldStartDragAndDrop = { isEnabled(it.toKmpExternalDragEvent()) },
        target = target
    )
}

@OptIn(ExperimentalComposeUiApi::class)
private fun DragAndDropEvent.toKmpExternalDragEvent(): KmpExternalDragEvent {
    return KmpExternalDragEvent(
        position = (nativeEvent as? DropTargetDragEvent)?.location
            ?.let { Offset(it.x.toFloat(), it.y.toFloat()) } ?: Offset(0f,0f),
    )
}

@OptIn(ExperimentalComposeUiApi::class)
private fun DragAndDropEvent.toKmpExternalDropEvent(): KmpExternalDropEvent {
    return KmpExternalDropEvent(
        position = (nativeEvent as? DropTargetDragEvent)?.location
            ?.let { Offset(it.x.toFloat(), it.y.toFloat()) } ?: Offset(0f,0f),
        data = awtTransferable.toKmpExternalDragData(),
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Suppress("UNCHECKED_CAST")
private fun Transferable.toKmpExternalDragData(): KmpExternalDropData = when {
    isDataFlavorSupported(DataFlavor.javaFileListFlavor) -> {
        val data = (getTransferData(DataFlavor.javaFileListFlavor) as? List<File>)?.map {
            KmpExternalFile(name = it.name, path = it.path, source = FileSystem.SYSTEM.source(it.path.toPath()))
        }
        KmpExternalDropData.Files(data ?: emptyList())
    }
    isDataFlavorSupported(DataFlavor.stringFlavor) -> {
        val data = getTransferData(DataFlavor.stringFlavor) as? String
        KmpExternalDropData.Text(data ?: "")
    }
    else -> KmpExternalDropData.Unknown
}