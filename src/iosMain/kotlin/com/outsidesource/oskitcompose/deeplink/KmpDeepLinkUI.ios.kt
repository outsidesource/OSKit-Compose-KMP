package com.outsidesource.oskitcompose.deeplink

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.outsidesource.oskitkmp.deeplink.KmpDeepLink

@Composable
actual fun KmpDeepLinkEffect(
    initialDeepLink: KmpDeepLink?,
    onNewDeepLink: (KmpDeepLink) -> Unit,
) {
    DisposableEffect(Unit) {
        if (initialDeepLink != null) onNewDeepLink(initialDeepLink)
        onDispose {  }
    }
}