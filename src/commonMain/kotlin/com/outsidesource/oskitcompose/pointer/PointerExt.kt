package com.outsidesource.oskitcompose.pointer

import androidx.compose.ui.input.pointer.*

suspend fun AwaitPointerEventScope.awaitFirstUp(
    pass: PointerEventPass = PointerEventPass.Main,
    requireUnconsumed: Boolean = false
): PointerInputChange {
    var event: PointerEvent

    while (true) {
        event = awaitPointerEvent(pass)
        if (!event.changes.all { if (requireUnconsumed) it.changedToUp() else it.changedToUpIgnoreConsumed() }) continue
        break
    }

    return event.changes[0]
}

suspend fun AwaitPointerEventScope.awaitFirstDownEvent(): PointerEvent {
    while (true) {
        val event = awaitPointerEvent(PointerEventPass.Main)
        if (event.changes.first().changedToDown()) {
            return event
        }
    }
}

suspend fun AwaitPointerEventScope.awaitForUpOrCancellationEvent(): PointerEvent? {
    while (true) {
        val event = awaitPointerEvent(PointerEventPass.Main)
        if (event.changes.first().changedToUp()) {
            return event
        }

        if (event.changes.first().consumed.downChange ||
            event.changes.first().isOutOfBounds(size, extendedTouchPadding)
        ) {
            return null // Canceled
        }

        // Check for cancel by position consumption. We can look on the Final pass of the
        // existing pointer event because it comes after the Main pass we checked above.
        val consumeCheck = awaitPointerEvent(PointerEventPass.Final)
        if (consumeCheck.changes.first().isConsumed) {
            return null
        }
    }
}