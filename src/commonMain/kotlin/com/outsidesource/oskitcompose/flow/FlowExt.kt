package com.outsidesource.oskitcompose.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter

@Suppress("UNCHECKED_CAST")
inline fun <reified R> Flow<*>.filterIsInstance(crossinline predicate: suspend (R) -> Boolean): Flow<R> =
    filter { it is R && predicate(it)  } as Flow<R>