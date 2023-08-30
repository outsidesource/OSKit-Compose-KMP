package com.outsidesource.oskitcompose.lib

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.outsidesource.oskitcompose.router.rememberForRoute
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.ParametersDefinition


@Composable
inline fun <reified T : Any> rememberInjectForRoute(
    key: String? = null,
    noinline parameters: ParametersDefinition? = null,
): T = rememberForRoute(key) { koinInjector.inject<T>(parameters = parameters).value }

@Composable
inline fun <reified T : Any> rememberInject(
    noinline parameters: ParametersDefinition? = null,
): T = remember { koinInjector.inject<T>(parameters = parameters).value }

val koinInjector = object : KoinComponent {}