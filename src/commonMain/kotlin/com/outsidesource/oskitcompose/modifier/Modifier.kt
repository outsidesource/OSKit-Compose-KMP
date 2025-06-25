package com.outsidesource.oskitcompose.modifier

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Modifier.ifThen(predicate: Boolean, modifier: @Composable (Modifier) -> Modifier): Modifier =
    if (predicate) modifier(this) else this