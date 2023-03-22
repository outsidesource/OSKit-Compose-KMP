package com.outsidesource.oskitcompose.lib

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.outsidesource.oskitcompose.router.rememberForRoute
import org.koin.core.parameter.ParametersDefinition
import org.koin.java.KoinJavaComponent.inject

@Composable
inline fun <reified T : Any> rememberInjectForRoute(
    key: String? = null,
    noinline parameters: ParametersDefinition? = null,
): T =
    rememberForRoute(key) { inject<T>(T::class.java, parameters = parameters).value }

@Composable
inline fun <reified T : Any> rememberInject(
    noinline parameters: ParametersDefinition? = null,
): T =
    remember { inject<T>(T::class.java, parameters = parameters).value }