package com.outsidesource.oskitcompose.deeplink

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.util.Consumer

@Composable
fun AndroidDeepLinkEffect(
    initialDeepLink: Intent?,
    onNewDeepLink: (Intent) -> Unit
) {
    val view = LocalView.current
    val parentActivity = (view.context as ComponentActivity)

    DisposableEffect(Unit) {
        if (initialDeepLink != null) onNewDeepLink(initialDeepLink)

        val listener = Consumer<Intent> { onNewDeepLink(it) }
        parentActivity.addOnNewIntentListener(listener)
        onDispose { parentActivity.removeOnNewIntentListener(listener) }
    }
}