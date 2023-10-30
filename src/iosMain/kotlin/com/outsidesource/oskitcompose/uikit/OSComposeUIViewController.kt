package com.outsidesource.oskitcompose.uikit

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSTimeInterval
import platform.UIKit.*

fun OSComposeUIViewController(
    content: @Composable () -> Unit,
): UIViewController {
    return OSUIViewControllerWrapper(ComposeUIViewController(content))
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
    private var navigationBarView: UIView? = null

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

        val navBarHeight = (UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow)?.safeAreaInsets?.useContents { bottom } ?: 0.0
        val navBarFrame = CGRectMake(
            x = 0.0,
            y = view.frame.useContents { size.height - navBarHeight },
            width = view.frame.useContents { size.width },
            height = navBarHeight,
        )

        navigationBarView = UIView(frame = navBarFrame).apply {
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

        val navBarHeight = (UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow)?.safeAreaInsets?.useContents { bottom } ?: 0.0
        navigationBarView?.setFrame(CGRectMake(
            x = 0.0,
            y = view.frame.useContents { size.height - navBarHeight },
            width = view.frame.useContents { size.width },
            height = navBarHeight
        ))
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

    fun setNavigationBarBackground(color: UIColor) {
        navigationBarView?.backgroundColor = color
    }
}