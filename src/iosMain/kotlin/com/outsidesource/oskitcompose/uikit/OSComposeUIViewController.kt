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
import com.outsidesource.oskitcompose.systemui.KmpWindowInsets
import com.outsidesource.oskitcompose.systemui.bottom
import com.outsidesource.oskitcompose.systemui.top
import kotlinx.coroutines.flow.MutableStateFlow
import platform.UIKit.*

/**
 * Used in conjunction with [SystemBarColorEffect] to help set colors for system bars
 */
fun OSComposeUIViewController(
    content: @Composable () -> Unit,
): UIViewController {

    return OSUIViewControllerWrapper(
        ComposeUIViewController {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                content()
                StatusBarBackground()
                NavBarBackground()
            }
        }
    )
}

@Composable
private fun BoxScope.StatusBarBackground() {
    val vc = LocalUIViewController.current.parentViewController
    if (vc !is OSUIViewControllerWrapper) return
    val color by vc.statusBarColor.collectAsState()

    Box(modifier = Modifier
        .align(Alignment.TopStart)
        .fillMaxWidth()
        .background(color)
        .windowInsetsPadding(KmpWindowInsets.top)
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
        .windowInsetsPadding(KmpWindowInsets.bottom)
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
    internal val statusBarColor = MutableStateFlow(Color.Transparent)
    internal val navigationBarColor = MutableStateFlow(Color.Transparent)

    override fun viewDidLoad() {
        super.viewDidLoad()

        addChildViewController(childVC)
        childVC.view.setTranslatesAutoresizingMaskIntoConstraints(false)
        view.addSubview(childVC.view)

        NSLayoutConstraint.activateConstraints(listOf(
            childVC.view.leadingAnchor.constraintEqualToAnchor(view.leadingAnchor),
            childVC.view.trailingAnchor.constraintEqualToAnchor(view.trailingAnchor),
            childVC.view.topAnchor.constraintEqualToAnchor(view.topAnchor),
            childVC.view.bottomAnchor.constraintEqualToAnchor(view.bottomAnchor),
        ))

        childVC.didMoveToParentViewController(this)
    }

    override fun preferredStatusBarStyle(): UIStatusBarStyle {
        return statusBarStyle
    }

    fun setStatusBarIconColor(useDark: Boolean) {
        statusBarStyle = if (useDark) UIStatusBarStyleDarkContent else UIStatusBarStyleLightContent
        setNeedsStatusBarAppearanceUpdate()
    }

    fun setStatusBarBackground(color: Color) {
        statusBarColor.tryEmit(color)
    }

    fun setNavigationBarBackground(color: Color) {
        navigationBarColor.tryEmit(color)
    }
}