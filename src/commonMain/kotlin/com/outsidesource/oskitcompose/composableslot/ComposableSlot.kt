package com.outsidesource.oskitcompose.composableslot

import androidx.compose.runtime.*


typealias ComposableSlotState = MutableState<(@Composable () -> Unit)?>

/**
 * Creates state for a composable slot, allowing rendering of a composable in a different part of the composable tree.
 */
@Composable
fun rememberComposableSlotState(): ComposableSlotState = remember { mutableStateOf(null) }


/**
 * Renders a [ComposableSlotState] populated by [ComposableSlot]
 */
@Composable
fun ComposableSlotRenderer(composableSlotState: ComposableSlotState) {
    composableSlotState.value?.invoke()
}

/**
 * Populates a [ComposableSlotState] for rendering with [ComposableSlotRenderer]. This allows rendering children
 * composables in a parent tree.
 */
@Composable
fun ComposableSlot(composableSlotState: ComposableSlotState, content: @Composable () -> Unit) {
    DisposableEffect(Unit) {
        composableSlotState.value = content
        onDispose { composableSlotState.value = null }
    }
}