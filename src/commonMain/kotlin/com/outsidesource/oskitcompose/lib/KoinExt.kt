package com.outsidesource.oskitcompose.lib

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.outsidesource.oskitcompose.router.rememberForRoute
import org.koin.compose.currentKoinScope
import org.koin.compose.koinInject
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.KoinContext
import org.koin.core.parameter.ParametersDefinition


@Composable
inline fun <reified T : Any> rememberInjectForRoute(
    key: String? = null,
    noinline onDestroy: (T) -> Unit = {},
    noinline parameters: ParametersDefinition? = null,
): T {
    val scope = currentKoinScope()
    return rememberForRoute(key) { scope.inject<T>(parameters = parameters).value.also { onDestroy { onDestroy(it) } } }
}

@Composable
inline fun <reified T : Any> rememberInject(): T = koinInject<T>()

@Composable
inline fun <reified T : Any> rememberInject(noinline parameters: ParametersDefinition): T =
    koinInject<T>(parameters = parameters)