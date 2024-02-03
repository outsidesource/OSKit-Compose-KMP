package com.outsidesource.oskitcompose.lib

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.node.Ref

/**
 * Remembers the last state that was not null even if the state is set to null. If the initial value is set to null,
 * the value will be null. Once the value is set to a non-null value it will remain non-null.
 *
 * This is useful in Popups if you are displaying content in a Popup based on a piece of nullable state. It prevents the
 * content from disappearing as the popup animates out.
 */
@Composable
fun <T : Any?> rememberLastNonNullState(state: T): T {
    val nonNullState = remember { VarRef(state) }
    if (state != null) nonNullState.value = state
    return nonNullState.value
}