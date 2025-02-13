package com.outsidesource.oskitcompose.router

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import com.outsidesource.oskitkmp.coordinator.Coordinator
import com.outsidesource.oskitkmp.coordinator.ICoordinatorObserver
import com.outsidesource.oskitkmp.router.*
import kotlinx.coroutines.flow.StateFlow

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
            override fun pop() = router.pop()
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

    KmpBackHandler(enabled = coordinatorObserver.hasBackStack()) { coordinatorObserver.pop() }

    AnimatedContent(
        targetState = currentRoute,
        transitionSpec = createComposeRouteTransition()
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
}