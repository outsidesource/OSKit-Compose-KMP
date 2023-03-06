package com.outsidesource.oskitcompose.popup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun BottomSheetControl(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    onDismissRequest: (() -> Unit)? = null,
    shouldDismissOnExternalClick: Boolean = true,
    shouldDismissOnEscapeKey: Boolean = true,
    bottomSheetStyles: BottomSheetStyles = remember { BottomSheetStyles() },
    controlContent: @Composable BoxScope.() -> Unit,
    bottomSheetContent: @Composable BoxScope.() -> Unit,
) {
    Box {
        controlContent()
        BottomSheet(
            modifier = modifier,
            bottomSheetStyles = bottomSheetStyles,
            isVisible = isVisible,
            onDismissRequest = onDismissRequest,
            shouldDismissOnEscapeKey = shouldDismissOnEscapeKey,
            shouldDismissOnExternalClick = shouldDismissOnExternalClick,
            content = bottomSheetContent
        )
    }
}