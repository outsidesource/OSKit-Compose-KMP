package com.outsidesource.oskitcompose.systemui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable


actual val KMPWindowInsets.Companion.topInsets: WindowInsets
    @Composable
    get() = LocalKMPWindowInsets.current?.topInsets ?: DefaultKMPWindowInsets

actual val KMPWindowInsets.Companion.bottomInsets: WindowInsets
    @Composable
    get() = LocalKMPWindowInsets.current?.bottomInsets ?: DefaultKMPWindowInsets

actual val KMPWindowInsets.Companion.verticalInsets: WindowInsets
    @Composable
    get() = LocalKMPWindowInsets.current?.verticalInsets ?: DefaultKMPWindowInsets

actual val KMPWindowInsets.Companion.rightInsets: WindowInsets
    @Composable
    get() = LocalKMPWindowInsets.current?.rightInsets ?: DefaultKMPWindowInsets

actual val KMPWindowInsets.Companion.leftInsets: WindowInsets
    @Composable
    get() = LocalKMPWindowInsets.current?.leftInsets ?: DefaultKMPWindowInsets

actual val KMPWindowInsets.Companion.horizontalInsets: WindowInsets
    @Composable
    get() = LocalKMPWindowInsets.current?.horizontalInsets ?: DefaultKMPWindowInsets

actual val KMPWindowInsets.Companion.allInsets: WindowInsets
    @Composable
    get() = LocalKMPWindowInsets.current?.allInsets ?: DefaultKMPWindowInsets