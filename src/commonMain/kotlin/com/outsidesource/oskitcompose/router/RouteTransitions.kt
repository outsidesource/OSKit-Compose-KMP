package com.outsidesource.oskitcompose.router

import androidx.compose.animation.*
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.outsidesource.oskitkmp.lib.Platform
import com.outsidesource.oskitkmp.lib.current
import com.outsidesource.oskitkmp.router.IAnimatedRoute
import com.outsidesource.oskitkmp.router.IRouteTransition
import com.outsidesource.oskitkmp.router.RouteStackEntry

/**
 * [routeTransition] is a convenience delegate function to help implement [IAnimatedRoute]
 *
 * ```
 * sealed class Route : IRoute {
 *     object Home : Route(), IAnimatedRoute by routeTransition(SlideRouteTransition)
 * }
 * ```
 */
fun routeTransition(transition: IRouteTransition): IAnimatedRoute {
    return object : IAnimatedRoute {
        override val animatedRouteTransition = transition
    }
}

/**
 * [ComposeRouteTransition] defines a route transition
 *
 * @param [enter] The animation for incoming content during a push()
 * @param [exit] The animation for the outgoing content during a push()
 * @param [popEnter] The animation for incoming content during a pop()
 * @param [popExit] The animation for outgoing content during a pop()
 * @param [predictiveBackEnter] An optional animation for predictive back. If no animation is supplied, the pop enter animation will be used
 * @param [predictiveBackExit] An optional animation for predictive back. If no animation is supplied, the pop exit animation will be used
 * @param [enterZ] The z-layer for the enter animation. By default, the enter animation will be on top of the exit
 *   animation. Valid values are 1f or -1f. The exit animation will be placed on the inverse z-layer. For example,
 *   if [enterZ] is set to 1f, the exit animation is set to -1f.
 * @param [baseLayerOverlay] An optional overlay for the bottom-most layer when transitioning. This allows
 *   placing a scrim or blackout transition during the animation.
 */
data class ComposeRouteTransition(
    val enter: AnimatedContentTransitionScope<RouteStackEntry>.(density: Density) -> EnterTransition,
    val exit: AnimatedContentTransitionScope<RouteStackEntry>.(density: Density) -> ExitTransition,
    val popEnter: AnimatedContentTransitionScope<RouteStackEntry>.(density: Density) -> EnterTransition,
    val popExit: AnimatedContentTransitionScope<RouteStackEntry>.(density: Density) -> ExitTransition,
    val supportsPredictiveBackForEdge: (edge: Int) -> Boolean = DefaultPredictiveBackSupport,
    val predictiveBackEnter: (swipeEdge: Int) -> (AnimatedContentTransitionScope<RouteStackEntry>.(density: Density) -> EnterTransition) = { popEnter },
    val predictiveBackExit: (swipeEdge: Int) -> (AnimatedContentTransitionScope<RouteStackEntry>.(density: Density) -> ExitTransition) = { popExit },
    val enterZ: Float = 1f,
    val baseLayerOverlay: (@Composable BoxScope.(isPopping: Boolean, transition: SeekableTransitionState<RouteStackEntry>) -> Unit)? = null,
) : IRouteTransition {

    companion object {
        val DefaultPredictiveBackSupport: (edge: Int) -> Boolean = { edge ->
            when {
                Platform.current == Platform.IOS -> edge == 0
                else -> true
            }
        }
    }
}

val PushFromTopRouteTransition = ComposeRouteTransition(
    enter = {
        val offsetY = with(it) { -25.dp.toPx() }.toInt()
        fadeIn(tween(300), 0f) + slideIn(tween(300)) { IntOffset(0, offsetY) }
    },
    exit = { fadeOut(tween(300), 0f) },
    popEnter = { fadeIn(tween(300), 0f) },
    popExit = {
        val offsetY = with(it) { -25.dp.toPx() }.toInt()
        slideOut(tween(300)) { IntOffset(0, offsetY) } + fadeOut(tween(300), 0f)
    },
)

val PushFromRightRouteTransition = ComposeRouteTransition(
    enter = {
        val offsetX = with(it) { 40.dp.toPx() }.toInt()
        fadeIn(tween(250, easing = EaseInOut)) + slideIn(tween(250, easing = EaseInOut)) { IntOffset(offsetX, 0) }
    },
    exit = {
        val offsetX = with(it) { -40.dp.toPx() }.toInt()
        slideOut(tween(250, easing = EaseInOut)) { IntOffset(offsetX, 0) }
    },
    popEnter = {
        val offsetX = with(it) { -40.dp.toPx() }.toInt()
        slideIn(tween(250, easing = EaseInOut)) { IntOffset(offsetX, 0) }
    },
    popExit = {
        val offsetX = with(it) { 40.dp.toPx() }.toInt()
        fadeOut(tween(250, easing = EaseInOut)) + slideOut(tween(250, easing = EaseInOut)) { IntOffset(offsetX, 0) }
    },
    predictiveBackEnter = { edge ->
        {
            slideIntoContainer(
                towards = if (edge == 0) AnimatedContentTransitionScope.SlideDirection.End else AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(durationMillis = 250, easing = LinearEasing),
                initialOffset = { fullOffset -> (fullOffset * 0.3f).toInt() }
            )
        }
    },
    predictiveBackExit = { edge ->
        {
            slideOutOfContainer(
                towards = if (edge == 0) AnimatedContentTransitionScope.SlideDirection.End else AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(durationMillis = 250, easing = LinearEasing)
            )
        }
    },
    baseLayerOverlay = { isPopping, transitionState ->
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawBehind {
                    val blackoutFraction = if (isPopping) 1 - transitionState.fraction else transitionState.fraction
                    drawRect(Color.Black, alpha = .106f * blackoutFraction)
                }
        )
    }
)

//val PushFromRightRouteTransition = ComposeRouteTransition(
//    enter = {
//        val offsetX = with(it) { 80.dp.toPx() }.toInt()
//        slideIn(tween(350, easing = CubicBezierEaseInEaseOut)) { IntOffset(it.width, 0) }
//    },
//    exit = {
//        val offsetX = with(it) { -80.dp.toPx() }.toInt()
//        slideOut(tween(350, easing = CubicBezierEaseInEaseOut)) { IntOffset(offsetX, 0) }
//    },
//    popEnter = {
//        val offsetX = with(it) { -80.dp.toPx() }.toInt()
//        slideIn(tween(350, easing = CubicBezierEaseInEaseOut)) { IntOffset(offsetX, 0) }
//    },
//    popExit = {
//        val offsetX = with(it) { 80.dp.toPx() }.toInt()
//        slideOut(tween(350, easing = CubicBezierEaseInEaseOut)) { IntOffset(it.width, 0) }
//    },
//)

val SlideFromBottomRouteTransition = ComposeRouteTransition(
    enter = {
        slideIn(tween(400)) { IntOffset(0, it.height) }
    },
    exit = {
        fadeOut(tween(400), .99f) + scaleOut(tween(400), targetScale = .9f)
    },
    popEnter = {
        fadeIn(tween(400), 0f) + scaleIn(tween(400), initialScale = .9f)
    },
    popExit = {
        slideOut(tween(400)) { IntOffset(0, (it.height * .5).toInt()) } + fadeOut(tween(400))
    },
)

val ScaleRouteTransition = ComposeRouteTransition(
    enter = { fadeIn(tween(300), 0f) + scaleIn(tween(300), initialScale = .9f) },
    exit = { fadeOut(tween(300), 0f) },
    popEnter = { scaleIn(tween(300), initialScale = 1.1f) + fadeIn(tween(300), 0f) },
    popExit = { fadeOut(tween(300), .99f) },
)

val FadeRouteTransition = ComposeRouteTransition(
    enter = { fadeIn(tween(300)) },
    exit = { fadeOut(tween(300)) },
    popEnter = { fadeIn(tween(300)) },
    popExit = { fadeOut(tween(300)) },
)

val NoRouteTransition = ComposeRouteTransition(
    enter = { EnterTransition.None },
    exit = { ExitTransition.None },
    popEnter = { EnterTransition.None },
    popExit = { ExitTransition.None },
)
