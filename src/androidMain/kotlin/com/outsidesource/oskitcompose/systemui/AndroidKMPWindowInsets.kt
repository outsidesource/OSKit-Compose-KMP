package com.outsidesource.oskitcompose.systemui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable


actual val KMPWindowInsets.Companion.topInsets: WindowInsets
    @Composable
    get() = LocalKMPWindowInsets.current?.topInsets ?: WindowInsets.statusBars

actual val KMPWindowInsets.Companion.bottomInsets: WindowInsets
    @Composable
    get() = LocalKMPWindowInsets.current?.bottomInsets ?: WindowInsets.navigationBars

actual val KMPWindowInsets.Companion.verticalInsets: WindowInsets
    @Composable
    get() = LocalKMPWindowInsets.current?.verticalInsets ?: WindowInsets.systemBars

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
    get() = LocalKMPWindowInsets.current?.allInsets ?: WindowInsets.systemBars