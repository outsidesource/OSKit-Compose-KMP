package com.outsidesource.oskitcompose.deeplink

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.outsidesource.oskitkmp.deeplink.KMPDeepLink

@Composable
actual fun KMPDeepLinkEffect(
    initialDeepLink: KMPDeepLink?,
    onNewDeepLink: (KMPDeepLink) -> Unit,
) {
    DisposableEffect(Unit) {
        if (initialDeepLink != null) onNewDeepLink(initialDeepLink)
        onDispose {  }
    }
}