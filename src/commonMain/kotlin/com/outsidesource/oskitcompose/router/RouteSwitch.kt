package com.outsidesource.oskitcompose.router

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.platform.LocalDensity
import com.outsidesource.oskitkmp.coordinator.Coordinator
import com.outsidesource.oskitkmp.coordinator.ICoordinatorObserver
import com.outsidesource.oskitkmp.router.*
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Observes a [Coordinator] and switches out content based on the current route.
 *
 * @param [coordinator] The [Coordinator] to observe.
 *
 * @param [content] The composable content to switch between routes. The current route to render is provided as the
 * parameter of the block.
 */
@Composable
fun RouteSwitch(
    coordinator: Coordinator,
    content: @Composable (route: IRoute) -> Unit,
) = RouteSwitch(
    coordinatorObserver = remember(coordinator) { Coordinator.Companion.createObserver(coordinator) },
    content = content
)

/**
 * Observes an [IRouter] and switches out content based on the current route.
 *
 * @param [router] The [Router] to observe.
 *
 * @param [content] The composable content to switch between routes. The current route to render is provided as the
 * parameter of the block.
 */
@Composable
fun RouteSwitch(
    router: IRouter,
    content: @Composable (route: IRoute) -> Unit,
) = RouteSwitch(
    coordinatorObserver = remember(router) {
        object : ICoordinatorObserver {
            override val routeFlow: StateFlow<RouteStackEntry> = router.routeFlow
            override val routeStack: List<RouteStackEntry>
                get() = router.routeStack
            override fun hasBackStack(): Boolean = router.hasBackStack()
            override fun pop(ignoreTransitionLock: Boolean) = router.pop(ignoreTransitionLock)
            override fun markTransitionStatus(status: RouteTransitionStatus) = router.markTransitionStatus(status)
            override fun addRouteLifecycleListener(listener: IRouteLifecycleListener) =
                router.addRouteLifecycleListener(listener)
        }
    },
    content = content
)

/**
 * Observes an [ICoordinatorObserver] and switches out content based on the current route.
 *
 * @param [coordinatorObserver] The [ICoordinatorObserver] to observe.
 *
 * @param [content] The composable content to switch between routes. The current route to render is provided as the
 * parameter of the block.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RouteSwitch(
    coordinatorObserver: ICoordinatorObserver,
    content: @Composable (route: IRoute) -> Unit,
) {
    val saveableStateHolder = rememberSaveableStateHolder()
    val currentRoute by coordinatorObserver.routeFlow.collectAsState()
    var progress by remember { mutableStateOf(0f) }
    var inPredictiveBack by remember { mutableStateOf(false) }
    val zIndices = remember { mutableMapOf<Int, Float>() }
    val density = LocalDensity.current

    KmpPredictiveBackHandler(coordinatorObserver.hasBackStack()) { ev ->
        progress = 0f
        try {
            ev.collect {
                // TODO: Limit to one edge on iOS?
                inPredictiveBack = true
                progress = it.progress
            }
            inPredictiveBack = false
            coordinatorObserver.pop(ignoreTransitionLock = true)
        } catch (_: CancellationException) {
            inPredictiveBack = false
        }
    }

    val transitionState = remember { SeekableTransitionState(currentRoute) }
    val transition = rememberTransition(transitionState)

    if (inPredictiveBack) {
        LaunchedEffect(progress) {
            val previousEntry = coordinatorObserver.routeStack[coordinatorObserver.routeStack.size - 2]
            transitionState.seekTo(progress, previousEntry)
        }
    } else {
        LaunchedEffect(currentRoute) {
            // This ensures we don't animate after the back gesture is cancelled and we
            // are already on the current state
            if (transitionState.currentState != currentRoute) {
                transitionState.animateTo(currentRoute)
            } else {
                // convert from nanoseconds to milliseconds
                val totalDuration = transition.totalDurationNanos / 1_000_000
                // When the predictive back gesture is cancel, we need to manually animate
                // the SeekableTransitionState from where it left off, to zero and then
                // snapTo the final position.
                animate(
                    initialValue = transitionState.fraction,
                    targetValue = 0f,
                    animationSpec = tween((transitionState.fraction * totalDuration).toInt())
                ) { value, _ ->
                    this@LaunchedEffect.launch {
                        if (value > 0) {
                            // Seek the original transition back to the currentState
                            transitionState.seekTo(value)
                        }
                        if (value == 0f) {
                            // Once we animate to the start, we need to snap to the right state.
                            transitionState.snapTo(currentRoute)
                        }
                    }
                }
            }
        }
    }

    // Example: https://github.com/JetBrains/compose-multiplatform-core/blob/00374fd96c631a5df051dc4c6e917ffb011235ce/navigation/navigation-compose/src/commonMain/kotlin/androidx/navigation/compose/NavHost.kt
    transition.AnimatedContent(
        transitionSpec = createComposeRouteTransition().let { composeTransition ->
            {
                val contentTransform = if (inPredictiveBack) {
                    // TODO: This is a test if I can override the predictive back transition
                    PredictiveBackTransition.toContentTransform(this, true, density)
                } else {
                    composeTransition()
                }
                
                // TODO: Redo zlayering to match NavHost
                val initialZIndex = zIndices[initialState.id] ?: 0f.also { zIndices[initialState.id] = 0f }
                val targetZ = when {
                    inPredictiveBack -> initialZIndex - 1f
                    else -> initialZIndex + contentTransform.targetContentZIndex
                }.also { z -> zIndices[targetState.id] = z }

                ContentTransform(
                    targetContentEnter = contentTransform.targetContentEnter,
                    initialContentExit = contentTransform.initialContentExit,
                    targetContentZIndex = targetZ,
                    sizeTransform = contentTransform.sizeTransform,
                )
            }
        },
        contentKey = { it.id }
    ) { state ->
        if (transition.currentState != transition.targetState) {
            coordinatorObserver.markTransitionStatus(RouteTransitionStatus.Running)
        } else {
            coordinatorObserver.markTransitionStatus(RouteTransitionStatus.Idle)
        }

        CompositionLocalProvider(
            localCoordinatorObserver provides coordinatorObserver,
            LocalRoute provides state,
        ) {
            RouteDestroyedEffect("com.outsidesource.oskitcompose.router.RouteSwitch") {
                saveableStateHolder.removeState(state.id)
            }
            saveableStateHolder.SaveableStateProvider(state.id) {
                content(state.route)
            }
        }
    }

    LaunchedEffect(transition.currentState, transition.targetState) {
        if (transition.currentState == transition.targetState) {
            zIndices
                .filter { it.key != transition.targetState.id }
                .forEach { zIndices.remove(it.key) }
        }
    }
}