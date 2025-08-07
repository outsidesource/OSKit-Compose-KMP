package com.outsidesource.oskitcompose.router

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.outsidesource.oskitcompose.router.ComposeRouteTransition.Companion.defaultBaseLayerOverlay
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
    val predictiveBackEnter: AnimatedContentTransitionScope<RouteStackEntry>.(
        density: Density,
        swipeEdge: Int
    ) -> EnterTransition = { density, edge -> popEnter(density) },
    val predictiveBackExit: AnimatedContentTransitionScope<RouteStackEntry>.(
        density: Density,
        swipeEdge: Int
    ) -> ExitTransition = { density, edge -> popExit(density) },
    val enterZ: Float = 1f,
    val popEnterZ: Float = -1f,
    val predictiveBackEnterZ: Float = -1f,
    val baseLayerOverlay: (@Composable BoxScope.(
        isPopping: Boolean,
        isPredictiveBack: Boolean,
        transition: SeekableTransitionState<RouteStackEntry>
    ) -> Unit)? = null,
) : IRouteTransition {

    companion object {
        val DefaultPredictiveBackSupport: (edge: Int) -> Boolean = { edge ->
            when (Platform.current) {
                Platform.IOS -> edge == 0
                Platform.Android -> true
                else -> false
            }
        }

        fun AnimatedContentTransitionScope<RouteStackEntry>.defaultIosPredictiveEnter(density: Density, swipeEdge: Int): EnterTransition {
            return slideIntoContainer(
                towards = if (swipeEdge == 0) AnimatedContentTransitionScope.SlideDirection.End else AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(durationMillis = 300, easing = LinearEasing),
                initialOffset = { (it * 0.3f).toInt() }
            )
        }

        fun AnimatedContentTransitionScope<RouteStackEntry>.defaultIosPredictiveExit(density: Density, swipeEdge: Int): ExitTransition {
            return slideOutOfContainer(
                towards = if (swipeEdge == 0) AnimatedContentTransitionScope.SlideDirection.End else AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(durationMillis = 300, easing = LinearEasing)
            )
        }

        @Composable
        fun BoxScope.defaultBaseLayerOverlay(
            isPopping: Boolean,
            isPredictiveBack: Boolean,
            transition: SeekableTransitionState<RouteStackEntry>
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .drawBehind {
                        val level = if (isPopping) 1f - transition.fraction else transition.fraction
                        drawRect(Color.Black, alpha = .106f * level)
                    }
            )
        }
    }
}

val PushFromTopRouteTransition = run {
    val duration = 300
    val distance = 40.dp

    ComposeRouteTransition(
        enter = {
            val offsetY = with(it) { -distance.toPx() }.toInt()
            fadeIn(tween(duration), initialAlpha = 0f) + slideIn(tween(duration)) { IntOffset(0, offsetY) }
        },
        exit = { fadeOut(tween(duration), targetAlpha = 0f) },
        popEnter = { fadeIn(tween(duration), initialAlpha = 0f) },
        popExit = {
            val offsetY = with(it) { -distance.toPx() }.toInt()
            slideOut(tween(duration)) { IntOffset(0, offsetY) } + fadeOut(tween(duration), targetAlpha = 0f)
        },
        baseLayerOverlay = { isPopping, isPredictiveBack, transitionState ->
            defaultBaseLayerOverlay(isPopping, isPredictiveBack, transitionState)
        }
    )
}

val PushFromRightRouteTransition = run {
    val duration = 300
    val distance = 60.dp

    ComposeRouteTransition(
        enter = {
            val offsetX = with(it) { distance.toPx() }.toInt()
            fadeIn(tween(duration)) + slideIn(tween(duration)) { IntOffset(offsetX, 0) }
        },
        exit = {
            val offsetX = with(it) { -distance.toPx() }.toInt()
            slideOut(tween(duration)) { IntOffset(offsetX, 0) }
        },
        popEnter = {
            val offsetX = with(it) { -distance.toPx() }.toInt()
            slideIn(tween(duration)) { IntOffset(offsetX, 0) }
        },
        popExit = {
            val offsetX = with(it) { distance.toPx() }.toInt()
            fadeOut(tween(duration)) + slideOut(tween(duration)) { IntOffset(offsetX, 0) }
        },
        baseLayerOverlay = { isPopping, isPredictiveBack, transitionState ->
            defaultBaseLayerOverlay(isPopping, isPredictiveBack, transitionState)
        }
    )
}

val SlideFromBottomRouteTransition = run {
    val duration = 400

    ComposeRouteTransition(
        enter = { slideIn(tween(duration)) { IntOffset(0, it.height) } },
        exit = { fadeOut(tween(duration), .99f) + scaleOut(tween(duration), targetScale = .9f) },
        popEnter = { fadeIn(tween(duration), 0f) + scaleIn(tween(duration), initialScale = .9f) },
        popExit = { slideOut(tween(duration)) { IntOffset(0, (it.height * .5).toInt()) } + fadeOut(tween(duration)) },
    )
}

val SlideFromRightRouteTransition = run {
    val duration = 300

    ComposeRouteTransition(
        enter = { slideIntoContainer(animationSpec = tween(duration), towards = AnimatedContentTransitionScope.SlideDirection.Start) },
        exit = { slideOutOfContainer(animationSpec = tween(duration), towards = AnimatedContentTransitionScope.SlideDirection.Start) { it / 3 } },
        popEnter = { slideIntoContainer(animationSpec = tween(duration), towards = AnimatedContentTransitionScope.SlideDirection.End) { it / 3 } },
        popExit = { slideOutOfContainer(animationSpec = tween(duration), towards = AnimatedContentTransitionScope.SlideDirection.End) },
        predictiveBackEnter = { _, _ ->
            val easing = if (Platform.current == Platform.IOS) LinearEasing else FastOutSlowInEasing
            slideIntoContainer(animationSpec = tween(duration, easing = easing), towards = AnimatedContentTransitionScope.SlideDirection.End) { it / 3 }
        },
        predictiveBackExit = { _, _ ->
            val easing = if (Platform.current == Platform.IOS) LinearEasing else FastOutSlowInEasing
            slideOutOfContainer(animationSpec = tween(duration, easing = easing), towards = AnimatedContentTransitionScope.SlideDirection.End)
        },
        baseLayerOverlay = { isPopping, isPredictiveBack, transitionState ->
            defaultBaseLayerOverlay(isPopping, isPredictiveBack, transitionState)
        }
    )
}

val ScaleRouteTransition = run {
    val duration = 300

    ComposeRouteTransition(
        enter = { fadeIn(tween(duration)) + scaleIn(tween(duration), initialScale = .9f) },
        exit = { fadeOut(tween(duration)) },
        popEnter = { fadeIn(tween(duration)) },
        popExit = { fadeOut(tween(duration)) + scaleOut(targetScale = .9f) },
    )
}

val FadeRouteTransition = run {
    val duration = 300

    ComposeRouteTransition(
        enter = { fadeIn(tween(duration)) },
        exit = { fadeOut(tween(duration)) },
        popEnter = { fadeIn(tween(duration)) },
        popExit = { fadeOut(tween(duration)) },
    )
}

val NoRouteTransition = ComposeRouteTransition(
    enter = { EnterTransition.None },
    exit = { ExitTransition.None },
    popEnter = { EnterTransition.None },
    popExit = { ExitTransition.None },
)