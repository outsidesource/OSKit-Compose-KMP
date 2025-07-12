package com.outsidesource.oskitcompose.router

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.platform.LocalDensity
import com.outsidesource.oskitcompose.lib.VarRef
import com.outsidesource.oskitkmp.coordinator.Coordinator
import com.outsidesource.oskitkmp.coordinator.ICoordinatorObserver
import com.outsidesource.oskitkmp.router.*
import kotlinx.coroutines.CancellationException
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
    val density = LocalDensity.current
    val saveableStateHolder = rememberSaveableStateHolder()
    val currentRoute by coordinatorObserver.routeFlow.collectAsState()
    val isPredictiveBackTransitionRunning = remember { VarRef(false) }
    var predictiveBackEdge by remember { mutableStateOf<Int?>(null) }
    val zIndices = remember { mutableMapOf<Int, Float>() }

    val transitionState = remember { SeekableTransitionState(currentRoute) }
    val transition = rememberTransition(transitionState)

    KmpPredictiveBackHandler(coordinatorObserver.hasBackStack()) { ev ->
        try {
            var supportsPredictiveBack: Boolean? = null
            ev.collect {
                val transition = coordinatorObserver.routeFlow.value.transition as? ComposeRouteTransition
                    ?: return@collect
                if (supportsPredictiveBack == null) {
                    supportsPredictiveBack = transition.supportsPredictiveBackForEdge(it.swipeEdge)
                }
                if (!supportsPredictiveBack) return@collect

                isPredictiveBackTransitionRunning.value = true
                predictiveBackEdge = it.swipeEdge
                val previousEntry = coordinatorObserver.routeStack[coordinatorObserver.routeStack.size - 2]
                transitionState.seekTo(it.progress, previousEntry)
            }
            predictiveBackEdge = null
            if (supportsPredictiveBack == true) coordinatorObserver.pop(ignoreTransitionLock = true)
        } catch (_: CancellationException) {
            predictiveBackEdge = null
        }
    }

    if (predictiveBackEdge == null) {
        LaunchedEffect(currentRoute) {
            // This ensures we don't animate after the back gesture is canceled and we are already on the current state
            if (transitionState.currentState != currentRoute) {
                transitionState.animateTo(currentRoute)
            } else {
                val totalDurationMillis = transition.totalDurationNanos / 1_000_000
                // When the predictive back gesture is canceled, we need to manually animate
                // the SeekableTransitionState from where it left off, to zero and then
                // snapTo the final position.
                animate(
                    initialValue = transitionState.fraction,
                    targetValue = 0f,
                    animationSpec = tween((transitionState.fraction * totalDurationMillis).toInt())
                ) { value, _ ->
                    this@LaunchedEffect.launch {
                        if (value > 0) transitionState.seekTo(value)
                        if (value == 0f) transitionState.snapTo(currentRoute)
                    }
                }
            }
        }
    }

    val composeTransitionRef = remember { VarRef<ComposeRouteTransition?>(null) }

    // Example: https://github.com/JetBrains/compose-multiplatform-core/blob/jb-main/navigation/navigation-compose/src/commonMain/kotlin/androidx/navigation/compose/NavHost.kt
    transition.AnimatedContent(
        transitionSpec = {
            val isPopping = targetState.id < initialState.id
            val route = if (isPopping) initialState else targetState
            val transition = (route.transition as? ComposeRouteTransition) ?: NoRouteTransition
            composeTransitionRef.value = transition

            val localPredictiveBackEdge = predictiveBackEdge
            val initialZIndex = zIndices[initialState.id] ?: (0f.also { zIndices[initialState.id] = 0f })
            val targetZ = when {
                targetState.id == initialState.id -> initialZIndex
                predictiveBackEdge != null -> initialZIndex + transition.predictiveBackEnterZ
                else -> initialZIndex + (if (isPopping) transition.popEnterZ else transition.enterZ)
            }
            zIndices[targetState.id] = targetZ

            ContentTransform(
                targetContentEnter = when {
                    localPredictiveBackEdge != null -> transition.predictiveBackEnter(this, density, localPredictiveBackEdge)
                    isPopping -> transition.popEnter(this, density)
                    else -> transition.enter(this, density)
                },
                initialContentExit = when {
                    localPredictiveBackEdge != null -> transition.predictiveBackExit(this, density, localPredictiveBackEdge)
                    isPopping -> transition.popExit(this, density)
                    else -> transition.exit(this, density)
                },
                targetContentZIndex = targetZ,
            )
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
                Box {
                    content(state.route)

                    val initialZ = zIndices[transition.segment.initialState.id] ?: 0f
                    val targetZ = zIndices[transition.segment.targetState.id] ?: 0f
                    val isPopping = transition.segment.targetState.id < transition.segment.initialState.id
                    val showMask = if (transition.isRunning) {
                        if (state.id == transition.segment.initialState.id) {
                            initialZ < targetZ
                        } else {
                            targetZ < initialZ
                        }
                    } else {
                        false
                    }

                    if (showMask) composeTransitionRef.value?.baseLayerOverlay?.invoke(this, isPopping, isPredictiveBackTransitionRunning.value, transitionState)
                    if (!transition.isRunning) isPredictiveBackTransitionRunning.value = false
                }
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