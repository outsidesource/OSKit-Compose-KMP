package com.outsidesource.oskitcompose.lib

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Remembers the last value that was not null even if the state is set to null. If the initial value is set to null,
 * the value will be null. Once the value is set to a non-null value it will remain non-null.
 *
 * This is useful in Popups if you are displaying content in a Popup based on a piece of nullable state. It prevents the
 * content from disappearing as the popup animates out.
 */
@Composable
fun <T : Any?> rememberLastNonNullValue(state: T): T {
    val nonNullValue = remember { VarRef(state) }
    if (state != null) nonNullValue.value = state
    return nonNullValue.value
}