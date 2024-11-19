package com.outsidesource.oskitcompose.systemui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable


actual val KMPWindowInsets.Companion.topInsets: WindowInsets
    @Composable
    get() = LocalKMPWindowInsets.current?.topInsets ?: WindowInsets.systemBars.only(WindowInsetsSides.Top)

actual val KMPWindowInsets.Companion.bottomInsets: WindowInsets
    @Composable
    get() = LocalKMPWindowInsets.current?.bottomInsets ?: WindowInsets.systemBars.only(WindowInsetsSides.Bottom)

actual val KMPWindowInsets.Companion.verticalInsets: WindowInsets
    @Composable
    get() = LocalKMPWindowInsets.current?.verticalInsets ?: WindowInsets.systemBars.only(WindowInsetsSides.Vertical)

actual val KMPWindowInsets.Companion.rightInsets: WindowInsets
    @Composable
    get() = LocalKMPWindowInsets.current?.rightInsets ?: WindowInsets.systemBars.only(WindowInsetsSides.Right)

actual val KMPWindowInsets.Companion.leftInsets: WindowInsets
    @Composable
    get() = LocalKMPWindowInsets.current?.leftInsets ?: WindowInsets.systemBars.only(WindowInsetsSides.Left)

actual val KMPWindowInsets.Companion.horizontalInsets: WindowInsets
    @Composable
    get() = LocalKMPWindowInsets.current?.horizontalInsets ?: WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)

actual val KMPWindowInsets.Companion.allInsets: WindowInsets
    @Composable
    get() = LocalKMPWindowInsets.current?.allInsets ?: WindowInsets.systemBars