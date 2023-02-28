package com.outsidesource.oskitcompose.lib

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember

data class VarRef<T>(var value: T)

@Immutable
data class ValRef<T>(val value: T)

@Composable
fun <T> rememberValRef(value: T) = remember(value) { ValRef(value) }
