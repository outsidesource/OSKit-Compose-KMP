package com.outsidesource.oskitcompose.router

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.outsidesource.oskitcompose.animation.CubicBezierEaseOutCirc
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
 */
data class ComposeRouteTransition(
    val enter: AnimatedContentTransitionScope<RouteStackEntry>.(density: Density) -> EnterTransition,
    val exit: AnimatedContentTransitionScope<RouteStackEntry>.(density: Density) -> ExitTransition,
    val popEnter: AnimatedContentTransitionScope<RouteStackEntry>.(density: Density) -> EnterTransition,
    val popExit: AnimatedContentTransitionScope<RouteStackEntry>.(density: Density) -> ExitTransition,
) : IRouteTransition

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
        fadeIn(tween(400, easing = CubicBezierEaseOutCirc)) + slideIn(tween(400, easing = CubicBezierEaseOutCirc)) { IntOffset(offsetX, 0) }
    },
    exit = {
        val offsetX = with(it) { -40.dp.toPx() }.toInt()
        slideOut(tween(400, easing = CubicBezierEaseOutCirc)) { IntOffset(offsetX, 0) }
    },
    popEnter = {
        val offsetX = with(it) { -40.dp.toPx() }.toInt()
        fadeIn(tween(400, easing = CubicBezierEaseOutCirc)) + slideIn(tween(400, easing = CubicBezierEaseOutCirc)) { IntOffset(offsetX, 0) }
    },
    popExit = {
        val offsetX = with(it) { 40.dp.toPx() }.toInt()
        slideOut(tween(400, easing = CubicBezierEaseOutCirc)) { IntOffset(offsetX, 0) }
    },
)

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
        slideOut(tween(400)) { IntOffset(0, (it.height * .5).toInt()) }
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
