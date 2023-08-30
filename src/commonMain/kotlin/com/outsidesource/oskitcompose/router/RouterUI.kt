package com.outsidesource.oskitcompose.router

import androidx.compose.animation.*
import androidx.compose.runtime.*
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalDensity
import com.outsidesource.oskitkmp.coordinator.ICoordinatorObserver
import com.outsidesource.oskitkmp.router.*
import com.outsidesource.oskitkmp.tuples.Tup3
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.reflect.KClass


internal val localRouteObjectStore = staticCompositionLocalOf { RouteObjectStore() }
internal val localCoordinatorObserver = staticCompositionLocalOf<ICoordinatorObserver> {
    object : ICoordinatorObserver {
        override val routeFlow: StateFlow<RouteStackEntry> = MutableStateFlow(RouteStackEntry(object : IRoute {}))
        override fun addRouteDestroyedListener(block: () -> Unit) {}
        override fun hasBackStack() = false
        override fun markTransitionStatus(status: RouteTransitionStatus) {}
        override fun pop() {}
    }
}
val LocalRoute = staticCompositionLocalOf { RouteStackEntry(object : IRoute {}) }

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun createComposeRouteTransition(): AnimatedContentTransitionScope<RouteStackEntry>.() -> ContentTransform {
    val density = LocalDensity.current

    return {
        val isPopping = targetState.id < initialState.id
        val route = if (isPopping) initialState else targetState
        val transition = (route.transition as? ComposeRouteTransition) ?: NoRouteTransition

        (if (isPopping) transition.popEnter else transition.enter)(density) togetherWith
                (if (isPopping) transition.popExit else transition.exit)(density)
    }
}

/**
 * [RouteDestroyedEffect] runs only once when the [IRoute] is popped off the backstack. If the route the effect is
 * attached to is currently visible in the composition, the effect will not be run until the composable has been disposed
 *
 * [effectId] Uniquely identifies the effect for a given route. [effectId] should be a unique constant.
 */
@Composable
@NonRestartableComposable
@Suppress("UNCHECKED_CAST")
fun RouteDestroyedEffect(effectId: String, effect: () -> Unit) {
    val router = localCoordinatorObserver.current

    val (storedEffect, isVisibleRef, isDestroyedRef) = rememberForRoute(Tup3::class, effectId) {
        val isDestroyedRef = Ref<Boolean>()
        val isVisibleRef = Ref<Boolean>()

        router.addRouteDestroyedListener {
            if (isVisibleRef.value == false) effect()
            isDestroyedRef.value = true
        }

        Tup3(effect, isVisibleRef, isDestroyedRef)
    } as Tup3<() -> Unit, Ref<Boolean>, Ref<Boolean>>

    return DisposableEffect(Unit) {
        isVisibleRef.value = true

        onDispose {
            isVisibleRef.value = false
            if (isDestroyedRef.value == true) storedEffect()
        }
    }
}

/**
 * [rememberForRoute] Remembers a given object for the lifetime of the route. There may only be one instance of
 * a given class for a given route. Additional instances may be created if a constant and unique [key] is provided.
 * Nullable types have undefined behaviour.
 */
@Composable
inline fun <reified T : Any> rememberForRoute(key: String? = null, noinline factory: () -> T): T =
    rememberForRoute(T::class, key, factory)

/**
 * [rememberForRoute] Remembers a given object for the lifetime of the route. There may only be one instance of
 * a given class for a given route. Additional instances may be created if a constant and unique [key] is provided.
 * Nullable types have undefined behaviour.
 */
@Composable
@Suppress("UNCHECKED_CAST")
fun <T : Any> rememberForRoute(objectType: KClass<T>, key: String? = null, factory: () -> T): T {
    val objectStore = localRouteObjectStore.current
    val route = LocalRoute.current
    val router = localCoordinatorObserver.current

    val storedObject = objectStore[route.id, key, objectType]

    return if (storedObject != null) storedObject as T else factory().apply {
        objectStore[route.id, key, objectType] = this
        router.addRouteDestroyedListener {
            objectStore.remove(route.id, key, objectType)
        }
    }
}

class RouteObjectStore {
    private val objects = mutableMapOf<String, Any>()

    operator fun <T: Any> get(routeId: Int, key: String?, objectType: KClass<T>): Any? {
        return objects["$routeId:${objectType.qualifiedName}:${key ?: ""}"]
    }

    operator fun <T: Any> set(routeId: Int, key: String?, objectType: KClass<T>, value: T) {
        objects["$routeId:${objectType.qualifiedName}:${key ?: ""}"] = value as Any
    }

    fun <T: Any> remove(routeId: Int, key: String?, objectType: KClass<T>) {
        objects.remove("$routeId:${objectType.qualifiedName}:${key ?: ""}")
    }
}