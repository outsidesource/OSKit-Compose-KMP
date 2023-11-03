package com.outsidesource.oskitcompose.uikit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.LocalUIViewController
import androidx.compose.ui.window.ComposeUIViewController
import com.outsidesource.oskitcompose.systemui.KMPWindowInsets
import com.outsidesource.oskitcompose.systemui.bottomInsets
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import platform.UIKit.*

fun OSComposeUIViewController(
    content: @Composable () -> Unit,
): UIViewController {

    return OSUIViewControllerWrapper(
        ComposeUIViewController {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                content()
                NavBarBackground()
            }
        }
    )
}

@Composable
private fun BoxScope.NavBarBackground() {
    val vc = LocalUIViewController.current.parentViewController
    if (vc !is OSUIViewControllerWrapper) return
    val color by vc.navigationBarColor.collectAsState()

    Box(modifier = Modifier
        .align(Alignment.BottomStart)
        .fillMaxWidth()
        .background(color)
        .windowInsetsPadding(KMPWindowInsets.bottomInsets)
    )
}

internal class OSUIViewControllerWrapper(
    composeViewController: UIViewController
): UIViewController(
    nibName = null,
    bundle = null,
) {

    private val childVC = composeViewController
    private var statusBarStyle = UIStatusBarStyleDarkContent
    private var statusBarView: UIView? = null
    internal val navigationBarColor = MutableStateFlow(Color.Transparent)

    @OptIn(ExperimentalForeignApi::class)
    override fun viewDidLoad() {
        super.viewDidLoad()

        val statusBarFrame = view.window?.windowScene?.statusBarManager?.statusBarFrame
            ?: UIApplication.sharedApplication.statusBarFrame

        addChildViewController(childVC)
        childVC.view.setTranslatesAutoresizingMaskIntoConstraints(false)
        view.addSubview(childVC.view)
        statusBarView = UIView(frame = statusBarFrame).apply {
            userInteractionEnabled = false
            view.addSubview(this)
        }

        NSLayoutConstraint.activateConstraints(listOf(
            childVC.view.leadingAnchor.constraintEqualToAnchor(view.leadingAnchor),
            childVC.view.trailingAnchor.constraintEqualToAnchor(view.trailingAnchor),
            childVC.view.topAnchor.constraintEqualToAnchor(view.topAnchor),
            childVC.view.bottomAnchor.constraintEqualToAnchor(view.bottomAnchor),
        ))

        childVC.didMoveToParentViewController(this)
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()

        val statusBarFrame = view.window?.windowScene?.statusBarManager?.statusBarFrame
            ?: UIApplication.sharedApplication.statusBarFrame
        statusBarView?.setFrame(statusBarFrame)
    }

    override fun preferredStatusBarStyle(): UIStatusBarStyle {
        return statusBarStyle
    }

    fun setStatusBarIconColor(useDark: Boolean) {
        statusBarStyle = if (useDark) UIStatusBarStyleDarkContent else UIStatusBarStyleLightContent
        setNeedsStatusBarAppearanceUpdate()
    }

    fun setStatusBarBackground(color: UIColor) {
        statusBarView?.backgroundColor = color
    }

    fun setNavigationBarBackground(color: Color) {
        navigationBarColor.tryEmit(color)
    }
}