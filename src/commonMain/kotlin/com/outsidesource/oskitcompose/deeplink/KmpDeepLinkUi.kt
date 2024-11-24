package com.outsidesource.oskitcompose.deeplink

import androidx.compose.runtime.Composable
import com.outsidesource.oskitkmp.annotation.ExperimentalOsKitApi
import com.outsidesource.oskitkmp.deeplink.KmpDeepLink

@ExperimentalOsKitApi
@Composable
expect fun KmpDeepLinkEffect(
    initialDeepLink: KmpDeepLink?,
    onNewDeepLink: (KmpDeepLink) -> Unit
)