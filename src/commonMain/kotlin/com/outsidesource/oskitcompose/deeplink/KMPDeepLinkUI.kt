package com.outsidesource.oskitcompose.deeplink

import androidx.compose.runtime.Composable
import com.outsidesource.oskitkmp.deeplink.KMPDeepLink

@Composable
expect fun KMPDeepLinkEffect(
    initialDeepLink: KMPDeepLink?,
    onNewDeepLink: (KMPDeepLink) -> Unit
)