package com.outsidesource.oskitcompose.animation

import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
// Adapted from CrossFade
fun <T> TransitionAnimatedContent(
    targetState: T,
    modifier: Modifier = Modifier,
    contentKey: (targetState: T) -> Any? = { it },
    content: @Composable (targetState: T, transition: Transition<T>) -> Unit,
) {
    val transition = updateTransition(targetState)
    val currentlyVisible = remember { mutableStateListOf<T>().apply { add(transition.currentState) } }
    val contentMap = remember { mutableMapOf<T, @Composable () -> Unit>() }

    if (transition.currentState == transition.targetState) {
        // If not animating, just display the current state
        if (currentlyVisible.size != 1 || currentlyVisible[0] != transition.targetState) {
            // Remove all the intermediate items from the list once the animation is finished.
            currentlyVisible.removeAll { it != transition.targetState }
            contentMap.clear()
        }
    }

    if (!contentMap.contains(transition.targetState)) {
        // Replace target with the same key if any
        val replacementId = currentlyVisible.indexOfFirst {
            contentKey(it) == contentKey(transition.targetState)
        }

        if (replacementId == -1) {
            currentlyVisible.add(transition.targetState)
        } else {
            currentlyVisible[replacementId] = transition.targetState
        }

        contentMap.clear()
        currentlyVisible.forEach { stateForContent ->
            contentMap[stateForContent] = {
                content(stateForContent, transition)
            }
        }
    }

    Box(modifier) {
        currentlyVisible.forEach {
            key(contentKey(it)) {
                contentMap[it]?.invoke()
            }
        }
    }
}