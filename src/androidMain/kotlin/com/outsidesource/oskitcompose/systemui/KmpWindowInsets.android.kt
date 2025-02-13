package com.outsidesource.oskitcompose.systemui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable


actual val KmpWindowInsets.Companion.top: WindowInsets
    @Composable
    get() = LocalKmpWindowInsets.current?.topInsets ?: WindowInsets.statusBars

actual val KmpWindowInsets.Companion.bottom: WindowInsets
    @Composable
    get() = LocalKmpWindowInsets.current?.bottomInsets ?: WindowInsets.navigationBars

actual val KmpWindowInsets.Companion.vertical: WindowInsets
    @Composable
    get() = LocalKmpWindowInsets.current?.verticalInsets ?: WindowInsets.systemBars

actual val KmpWindowInsets.Companion.right: WindowInsets
    @Composable
    get() = LocalKmpWindowInsets.current?.rightInsets ?: DefaultKmpWindowInsets

actual val KmpWindowInsets.Companion.left: WindowInsets
    @Composable
    get() = LocalKmpWindowInsets.current?.leftInsets ?: DefaultKmpWindowInsets

actual val KmpWindowInsets.Companion.horizontal: WindowInsets
    @Composable
    get() = LocalKmpWindowInsets.current?.horizontalInsets ?: DefaultKmpWindowInsets

actual val KmpWindowInsets.Companion.all: WindowInsets
    @Composable
    get() = LocalKmpWindowInsets.current?.allInsets ?: WindowInsets.systemBars

actual val KmpWindowInsets.Companion.ime: WindowInsets
    @Composable
    get() = LocalKmpWindowInsets.current?.imeInsets ?: WindowInsets.ime