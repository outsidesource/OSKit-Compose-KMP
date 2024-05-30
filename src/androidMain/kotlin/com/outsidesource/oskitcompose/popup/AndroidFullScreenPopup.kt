package com.outsidesource.oskitcompose.popup

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.findViewTreeOnBackPressedDispatcherOwner
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.popup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.lifecycle.*
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import java.util.*

internal data class AndroidFullScreenPopupProperties(
    val focusable: Boolean = false,
    val securePolicy: SecureFlagPolicy = SecureFlagPolicy.Inherit,
    val clippingEnabled: Boolean = false,
    val dismissOnBackPress: Boolean = true,
)

/**
 * [AndroidFullScreenPopup] Creates a popup that uses the full screen (including status bars and navigation bars)
 * regardless of [WindowCompat.setDecorFitsSystemWindows()]. This allows a popup to draw behind system bars.
 */
@Composable
internal fun AndroidFullScreenPopup(
    onDismissRequest: (() -> Unit)? = null,
    onPreviewKeyEvent: (KeyEvent) -> Boolean,
    onKeyEvent: (KeyEvent) -> Boolean,
    properties: AndroidFullScreenPopupProperties = AndroidFullScreenPopupProperties(),
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    val parentComposition = rememberCompositionContext()
    val currentContent by rememberUpdatedState(content)
    val popupId = rememberSaveable { UUID.randomUUID() }
    val backPressedDispatcherOwner = LocalOnBackPressedDispatcherOwner.current

    val fullScreenPopupLayout = remember {
        FullScreenPopupLayout(
            onDismissRequest = onDismissRequest,
            properties = properties,
            composeView = view,
            popupId = popupId,
            onKeyEvent = onKeyEvent,
            backPressedDispatcherOwner = backPressedDispatcherOwner,
        ).apply {
            setContent(parent = parentComposition) {
                Box(
                    modifier = Modifier
                        .focusable()
                        .semantics { popup() }
                        .onPreviewKeyEvent(onPreviewKeyEvent)
                        .fillMaxSize(),
                ) {
                    currentContent()
                }
            }
        }
    }

    DisposableEffect(fullScreenPopupLayout) {
        fullScreenPopupLayout.show()
        fullScreenPopupLayout.updateParameters(
            onDismissRequest = onDismissRequest,
            properties = properties,
        )

        onDispose {
            fullScreenPopupLayout.disposeComposition()
            fullScreenPopupLayout.dismiss()
        }
    }

    SideEffect {
        fullScreenPopupLayout.updateParameters(
            onDismissRequest = onDismissRequest,
            properties = properties,
        )
    }
}

@SuppressLint("ViewConstructor")
internal class FullScreenPopupLayout(
    private var onDismissRequest: (() -> Unit)?,
    private val onKeyEvent: ((KeyEvent) -> Boolean) = { false },
    private val backPressedDispatcherOwner: OnBackPressedDispatcherOwner?,
    private var properties: AndroidFullScreenPopupProperties,
    private val composeView: View,
    popupId: UUID,
) : AbstractComposeView(composeView.context) {

    private val windowManager = composeView.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var params: WindowManager.LayoutParams = createLayoutParams()

    init {
        id = android.R.id.content
        setViewTreeLifecycleOwner(composeView.findViewTreeLifecycleOwner())
        setViewTreeViewModelStoreOwner(composeView.findViewTreeViewModelStoreOwner())
        setViewTreeSavedStateRegistryOwner(composeView.findViewTreeSavedStateRegistryOwner())
        // Set unique id for AbstractComposeView. This allows state restoration for the state
        // defined inside the Popup via rememberSaveable()
        setTag(androidx.compose.ui.R.id.compose_view_saveable_id_tag, "FullScreenPopupWindowLayout:$popupId")

        // Enable children to draw their shadow by not clipping them
        clipChildren = false
    }

    private fun createLayoutParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams().apply {
            gravity = Gravity.TOP
            type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL
            token = composeView.applicationWindowToken
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            format = PixelFormat.TRANSLUCENT

            flags = flags and (
                    WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES or
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                            WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM or
                            WindowManager.LayoutParams.FLAG_SPLIT_TOUCH
                    ).inv()
        }
    }

    private var content: @Composable () -> Unit by mutableStateOf({})

    override var shouldCreateCompositionOnAttachedToWindow: Boolean = false
        private set

    fun setContent(parent: CompositionContext, content: @Composable () -> Unit) {
        setParentCompositionContext(parent)
        this.content = content
        shouldCreateCompositionOnAttachedToWindow = true
    }

    @Composable
    override fun Content() {
        content()
    }

    fun show() {
        windowManager.addView(this, params)
    }

    fun dismiss() {
        windowManager.removeViewImmediate(this)
        setViewTreeLifecycleOwner(null)
    }

    override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {
        val consumed = onKeyEvent(KeyEvent(event))
        if (event.keyCode == android.view.KeyEvent.KEYCODE_BACK) {
            if (keyDispatcherState == null) {
                return consumed
            }
            if (event.action == android.view.KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                val state = keyDispatcherState
                state?.startTracking(event, this)
                return false
            } else if (event.action == android.view.KeyEvent.ACTION_UP) {
                if (consumed) return true

                if (backPressedDispatcherOwner?.onBackPressedDispatcher?.hasEnabledCallbacks() == true) {
                    backPressedDispatcherOwner.onBackPressedDispatcher.onBackPressed()
                    return true
                } else {
                    if (!properties.dismissOnBackPress) return false

                    val state = keyDispatcherState
                    if (state != null && state.isTracking(event) && !event.isCanceled) {
                        onDismissRequest?.invoke()
                        return true
                    }
                }

                return false
            }
        }

        return consumed
    }

    private fun applyNewFlags(flags: Int) {
        params.flags = flags
        windowManager.updateViewLayout(this, params)
    }

    private fun setIsFocusable(isFocusable: Boolean) = applyNewFlags(
        if (!isFocusable) {
            params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        } else {
            params.flags and (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv())
        }
    )

    private fun setSecurePolicy(securePolicy: SecureFlagPolicy) {
        val secureFlagEnabled = securePolicy.shouldApplySecureFlagWrapper(composeView.isFlagSecureEnabledWrapper())
        applyNewFlags(
            if (secureFlagEnabled) {
                params.flags or WindowManager.LayoutParams.FLAG_SECURE
            } else {
                params.flags and (WindowManager.LayoutParams.FLAG_SECURE.inv())
            }
        )
    }

    private fun setClippingEnabled(clippingEnabled: Boolean) = applyNewFlags(
        if (clippingEnabled) {
            params.flags and (WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS.inv())
        } else {
            params.flags or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        }
    )

    fun updateParameters(
        onDismissRequest: (() -> Unit)?,
        properties: AndroidFullScreenPopupProperties,
    ) {
        this.onDismissRequest = onDismissRequest
        this.properties = properties
        setIsFocusable(properties.focusable)
        setSecurePolicy(properties.securePolicy)
        setClippingEnabled(properties.clippingEnabled)
    }
}

private fun View.isFlagSecureEnabledWrapper(): Boolean {
    val windowParams = rootView.layoutParams as? WindowManager.LayoutParams
    if (windowParams != null) {
        return (windowParams.flags and WindowManager.LayoutParams.FLAG_SECURE) != 0
    }
    return false
}

private fun SecureFlagPolicy.shouldApplySecureFlagWrapper(isSecureFlagSetOnParent: Boolean): Boolean {
    return when (this) {
        SecureFlagPolicy.SecureOff -> false
        SecureFlagPolicy.SecureOn -> true
        SecureFlagPolicy.Inherit -> isSecureFlagSetOnParent
    }
}