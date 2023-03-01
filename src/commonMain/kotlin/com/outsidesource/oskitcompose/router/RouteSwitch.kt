package com.outsidesource.oskitcompose.router

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import com.outsidesource.oskitkmp.coordinator.*
import com.outsidesource.oskitkmp.router.IRoute
import com.outsidesource.oskitkmp.router.RouteTransitionStatus

/**
 * [RouteSwitch] is the primary means of using a [Coordinator] in a composable. [RouteSwitch] will automatically subscribe
 * to the passed in [Coordinator] and update when the [Router] updates.
 *
 * @param [coordinator] The [Coordinator] to listen to.
 *
 * @param [content] The composable content to switch between routes. The current route to render is provided as the
 * parameter of the block.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RouteSwitch(
    coordinator: Coordinator,
    content: @Composable (route: IRoute) -> Unit,
) {
    val saveableStateHolder = rememberSaveableStateHolder()
    val coordinatorObserver = remember(coordinator) { createCoordinatorObserver(coordinator) }
    val currentRoute by coordinatorObserver.routeFlow.collectAsState()

    KMPBackHandler(enabled = coordinatorObserver.hasBackStack()) { coordinatorObserver.pop() }

    AnimatedContent(
        targetState = currentRoute,
        transitionSpec = createComposeRouteTransition()
    ) { state ->
        if (transition.currentState != transition.targetState) {
            coordinatorObserver.markTransitionStatus(RouteTransitionStatus.Running)
        } else {
            coordinatorObserver.markTransitionStatus(RouteTransitionStatus.Completed)
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
