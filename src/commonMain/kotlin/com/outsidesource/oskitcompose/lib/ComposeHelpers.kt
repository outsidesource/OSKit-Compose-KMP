package com.outsidesource.oskitcompose.lib

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun <T : Any?> rememberPreviousNonNullState(state: T): T {
    val nonNullState = remember { VarRef(state) }
    if (state != null) nonNullState.value = state
    return nonNullState.value
}