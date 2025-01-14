package com.outsidesource.oskitcompose.systemui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable


actual val KmpWindowInsets.Companion.top: WindowInsets
    @Composable
    get() = LocalKmpWindowInsets.current?.topInsets ?: WindowInsets.systemBars.only(WindowInsetsSides.Top)

actual val KmpWindowInsets.Companion.bottom: WindowInsets
    @Composable
    get() = LocalKmpWindowInsets.current?.bottomInsets ?: WindowInsets.systemBars.only(WindowInsetsSides.Bottom)

actual val KmpWindowInsets.Companion.vertical: WindowInsets
    @Composable
    get() = LocalKmpWindowInsets.current?.verticalInsets ?: WindowInsets.systemBars.only(WindowInsetsSides.Vertical)

actual val KmpWindowInsets.Companion.right: WindowInsets
    @Composable
    get() = LocalKmpWindowInsets.current?.rightInsets ?: WindowInsets.systemBars.only(WindowInsetsSides.Right)

actual val KmpWindowInsets.Companion.left: WindowInsets
    @Composable
    get() = LocalKmpWindowInsets.current?.leftInsets ?: WindowInsets.systemBars.only(WindowInsetsSides.Left)

actual val KmpWindowInsets.Companion.horizontal: WindowInsets
    @Composable
    get() = LocalKmpWindowInsets.current?.horizontalInsets ?: WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)

actual val KmpWindowInsets.Companion.all: WindowInsets
    @Composable
    get() = LocalKmpWindowInsets.current?.allInsets ?: WindowInsets.systemBars

actual val KmpWindowInsets.Companion.ime: WindowInsets
    @Composable
    get() = LocalKmpWindowInsets.current?.imeInsets ?: WindowInsets.ime