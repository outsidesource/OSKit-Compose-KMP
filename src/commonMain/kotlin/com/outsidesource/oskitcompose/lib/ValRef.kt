package com.outsidesource.oskitcompose.lib

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember

/**
 * A variable container class to store and update ephemeral data without causing recompositions
 */
data class VarRef<T>(var value: T)

/**
 * A container class to store non-stable objects to enable better recomposition skipping
 */
@Immutable
data class ValRef<T>(val value: T)

@Composable
fun <T> rememberValRef(value: T) = remember(value) { ValRef(value) }
