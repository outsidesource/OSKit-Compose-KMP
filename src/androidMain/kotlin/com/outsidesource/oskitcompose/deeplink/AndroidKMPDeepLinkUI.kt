package com.outsidesource.oskitcompose.deeplink

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.util.Consumer
import com.outsidesource.oskitkmp.deeplink.KMPDeepLink

@Composable
actual fun KMPDeepLinkEffect(
    initialDeepLink: KMPDeepLink?,
    onNewDeepLink: (KMPDeepLink) -> Unit
) {
    val view = LocalView.current
    val parentActivity = (view.context as ComponentActivity)

    DisposableEffect(Unit) {
        if (initialDeepLink != null) onNewDeepLink(initialDeepLink)

        val listener = Consumer<Intent> { onNewDeepLink(KMPDeepLink.Android(it)) }
        parentActivity.addOnNewIntentListener(listener)
        onDispose { parentActivity.removeOnNewIntentListener(listener) }
    }
}