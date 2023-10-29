package com.outsidesource.oskitcompose.uikit

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
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
    bundle = null
) {

    private val childVC = composeViewController
    private var statusBarStyle = UIStatusBarStyleDarkContent
    private var statusBarView: UIView? = null

    @OptIn(ExperimentalForeignApi::class)
    override fun viewDidLoad() {
        super.viewDidLoad()

        val statusBarFrame = view.window?.windowScene?.statusBarManager?.statusBarFrame
            ?: UIApplication.sharedApplication.statusBarFrame

        addChildViewController(childVC)
        childVC.view.setTranslatesAutoresizingMaskIntoConstraints(false)
        view.addSubview(childVC.view)
        statusBarView = UIView(frame = statusBarFrame).apply { view.addSubview(this) }

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
}