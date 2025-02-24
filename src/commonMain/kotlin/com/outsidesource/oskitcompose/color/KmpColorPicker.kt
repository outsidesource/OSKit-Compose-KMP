package com.outsidesource.oskitcompose.color

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.outsidesource.oskitcompose.modifier.outerShadow
import kotlin.math.pow
import kotlin.math.sqrt


@Composable
fun KmpColorPicker(
    color: HsvColor,
    renderer: IKmpColorPickerRenderer = KmpColorPickerRenderer.Sv,
    rendererOptions: KmpColorPickerRendererOptions = remember { KmpColorPickerRendererOptions() },
    onChange: (HsvColor, offset: Offset, size: IntSize) -> Unit = { _, _, _ -> },
    onDone: (HsvColor, offset: Offset, size: IntSize) -> Unit = { _, _, _ -> },
    handle: @Composable (color: HsvColor, interactionSource: MutableInteractionSource) -> Unit = { color, interactionSource ->
        KmpColorPickerHandle(color, interactionSource)
    },
    modifier: Modifier = Modifier,
) {
    var draggingColor by remember { mutableStateOf(color) }
    var isDragging by remember { mutableStateOf(false) }
    val mergedColor = rememberUpdatedState(if (isDragging) draggingColor else color)
    val interactionSource = remember { MutableInteractionSource() }
    val focusRequester = remember { FocusRequester() }

    BoxWithConstraints(
        modifier = modifier
    ) {
        val parentSize by rememberUpdatedState(IntSize(constraints.maxWidth, constraints.maxHeight))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val downColor = renderer.colorForOffset(mergedColor.value, down.position, size)
                        draggingColor = downColor
                        isDragging = true
                        onChange(downColor, down.position, size)

                        val pressInteraction = PressInteraction.Press(down.position)
                        interactionSource.tryEmit(pressInteraction)
                        focusRequester.requestFocus()

                        var lastPosition = down.position
                        drag(down.id) {
                            lastPosition = it.position
                            val updatedColor = renderer.colorForOffset(mergedColor.value, it.position, size)
                            draggingColor = updatedColor
                            onChange(updatedColor, it.position, size)
                            it.consume()
                        }

                        val upColor = renderer.colorForOffset(mergedColor.value, lastPosition, size)
                        isDragging = false
                        onDone(upColor, lastPosition, size)

                        interactionSource.tryEmit(PressInteraction.Release(pressInteraction))
                    }
                }
                .graphicsLayer {
                    if (rendererOptions.renderAlpha) alpha = mergedColor.value.alpha
                }
                .drawBehind {
                    drawIntoCanvas { renderer.draw(mergedColor.value, it, size, rendererOptions) }
                }
        )

        Box(
            modifier = Modifier
                .focusRequester(focusRequester)
                .focusable(interactionSource = interactionSource)
                .onKeyEvent {
                    if (it.type != KeyEventType.KeyDown) return@onKeyEvent false
                    val multiplier = if (it.isShiftPressed) 10f else 1f
                    val directionOffset = when (it.key) {
                        Key.DirectionUp -> Offset(0f, -1f * multiplier)
                        Key.DirectionRight -> Offset(1f * multiplier, 0f)
                        Key.DirectionDown -> Offset(0f, 1f * multiplier)
                        Key.DirectionLeft -> Offset(-1f * multiplier, 0f)
                        else -> return@onKeyEvent false
                    }

                    val newOffset = renderer.offsetForColor(mergedColor.value, parentSize) + directionOffset
                    val updatedColor = renderer.colorForOffset(mergedColor.value, newOffset, parentSize)
                    onChange(updatedColor, newOffset, parentSize)
                    true
                }
                .graphicsLayer {
                    val offset = renderer.offsetForColor(mergedColor.value, parentSize)
                    translationX = offset.x - (size.width / 2)
                    translationY = offset.y - (size.width / 2)
                }
                // TODO: This allows clicking on the handle outside of the picker, but results in multiple events
//                .pointerInput(Unit) {
//                    awaitEachGesture {
//                        val down = awaitFirstDown(requireUnconsumed = false)
//                        val parentSize = IntSize(constraints.maxWidth, constraints.maxHeight)
//                        val parentOffset = renderer.offsetForColor(mergedColor.value, parentSize)
//                        var offset = parentOffset + down.position - Offset(size.width / 2f,  size.height / 2f)
//
//                        val downColor = renderer.colorForOffset(mergedColor.value, offset, parentSize)
//                        draggingColor = downColor
//                        isDragging = true
//                        onChange(downColor, offset, parentSize)
//
//                        val pressInteraction = PressInteraction.Press(down.position)
//                        interactionSource.tryEmit(pressInteraction)
//                        focusRequester.requestFocus()
//
//                        drag(down.id) {
//                            offset = offset + (it.position - it.previousPosition)
//                            val dragColor = renderer.colorForOffset(mergedColor.value, offset, parentSize)
//                            draggingColor = dragColor
//                            onChange(dragColor, offset, parentSize)
//                        }
//
//                        val upColor = renderer.colorForOffset(mergedColor.value, offset, size)
//                        isDragging = false
//                        onDone(upColor, offset, size)
//
//                        interactionSource.tryEmit(PressInteraction.Release(pressInteraction))
//                    }
//                }
        ) {
            handle(mergedColor.value, interactionSource)
        }
    }
}

@Composable
fun KmpColorPicker(
    colors: Map<String, HsvColor>,
    renderer: IKmpColorPickerRenderer = KmpColorPickerRenderer.Sv,
    rendererOptions: KmpColorPickerRendererOptions = remember { KmpColorPickerRendererOptions() },
    onChange: (String, HsvColor, offset: Offset, size: IntSize) -> Unit = { _, _, _, _ -> },
    onDone: (String, HsvColor, offset: Offset, size: IntSize) -> Unit = { _, _, _, _ -> },
    handle: @Composable (color: HsvColor, interactionSource: MutableInteractionSource) -> Unit = { color, interactionSource ->
        KmpColorPickerHandle(color, interactionSource)
    },
    modifier: Modifier = Modifier,
) {
    val lambdaColors by rememberUpdatedState(colors)
    var draggingColor by remember { mutableStateOf<HsvColor?>(null) }
    var draggingKey by remember { mutableStateOf<String?>(null) }
    val interactionSourceRegister = remember { mutableMapOf<String, MutableInteractionSource>() }
    val focusRequesterRegister = remember { mutableMapOf<String, FocusRequester>() }

    BoxWithConstraints(
        modifier = modifier
    ) {
        val parentSize by rememberUpdatedState(IntSize(constraints.maxWidth, constraints.maxHeight))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val activeKey = run {
                            var minDistance = Pair<String?, Float>(null, Float.MAX_VALUE)
                            lambdaColors.forEach { (key, color) ->
                                val distance = cartesianDistance(down.position, renderer.offsetForColor(color, size))
                                if (distance > minDistance.second) return@forEach
                                minDistance = Pair(key, distance)
                            }
                            minDistance.first ?: return@awaitEachGesture
                        }

                        val downColor = renderer.colorForOffset(lambdaColors[activeKey] ?: HsvColor.Black, down.position, size)
                        draggingColor = downColor
                        draggingKey = activeKey
                        onChange(activeKey, downColor, down.position, size)

                        val pressInteraction = PressInteraction.Press(down.position)
                        interactionSourceRegister[activeKey]?.tryEmit(pressInteraction)
                        focusRequesterRegister[activeKey]?.requestFocus()

                        var lastPosition = down.position
                        drag(down.id) {
                            lastPosition = it.position
                            val updatedColor = renderer.colorForOffset(draggingColor ?: HsvColor.Black, it.position, size)
                            draggingColor = updatedColor
                            onChange(activeKey, updatedColor, it.position, size)
                            it.consume()
                        }

                        val upColor = renderer.colorForOffset(draggingColor ?: HsvColor.Black, lastPosition, size)
                        draggingKey = null
                        onDone(activeKey, upColor, lastPosition, size)

                        interactionSourceRegister[activeKey]?.tryEmit(PressInteraction.Release(pressInteraction))
                    }
                }
                .graphicsLayer {
                    if (rendererOptions.renderAlpha) alpha = (lambdaColors.values.firstOrNull() ?: HsvColor.Black).alpha
                }
                .drawBehind {
                    drawIntoCanvas { renderer.draw(lambdaColors.values.firstOrNull() ?: HsvColor.Black, it, size, rendererOptions) }
                }
        )

        colors.forEach { (key, color) ->
            key(key) {
                val mergedColor = rememberUpdatedState(if (draggingKey == key) draggingColor ?: color else color)
                val interactionSource = remember { MutableInteractionSource() }
                val focusRequester = remember { FocusRequester() }

                DisposableEffect(Unit) {
                    interactionSourceRegister[key] = interactionSource
                    focusRequesterRegister[key] = focusRequester

                    onDispose {
                        interactionSourceRegister.remove(key)
                        focusRequesterRegister.remove(key)
                    }
                }

                Box(
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .focusable(interactionSource = interactionSource)
                        .onKeyEvent {
                            if (it.type != KeyEventType.KeyDown) return@onKeyEvent false
                            val multiplier = if (it.isShiftPressed) 10f else 1f
                            val directionOffset = when (it.key) {
                                Key.DirectionUp -> Offset(0f, -1f * multiplier)
                                Key.DirectionRight -> Offset(1f * multiplier, 0f)
                                Key.DirectionDown -> Offset(0f, 1f * multiplier)
                                Key.DirectionLeft -> Offset(-1f * multiplier, 0f)
                                else -> return@onKeyEvent false
                            }

                            val newOffset = renderer.offsetForColor(mergedColor.value, parentSize) + directionOffset
                            val updatedColor = renderer.colorForOffset(mergedColor.value, newOffset, parentSize)
                            onChange(key, updatedColor, newOffset, parentSize)
                            true
                        }
                        .graphicsLayer {
                            val offset = renderer.offsetForColor(mergedColor.value, parentSize)
                            translationX = offset.x - (size.width / 2)
                            translationY = offset.y - (size.width / 2)
                        }
                    // TODO: This allows clicking on the handle outside of the picker, but results in multiple events
//                    .pointerInput(Unit) {
//                        awaitEachGesture {
//                            val down = awaitFirstDown(requireUnconsumed = false)
//                            val parentSize = IntSize(constraints.maxWidth, constraints.maxHeight)
//                            val parentOffset = renderer.offsetForColor(mergedColor.value, parentSize)
//                            var offset = parentOffset + down.position - Offset(size.width / 2f,  size.height / 2f)
//
//                            val downColor = renderer.colorForOffset(mergedColor.value, offset, parentSize)
//                            draggingColor = downColor
//                            draggingKey = key
//                            onChange(key, downColor, offset, parentSize)
//
//                            drag(down.id) {
//                                offset = offset + (it.position - it.previousPosition)
//                                val dragColor = renderer.colorForOffset(mergedColor.value, offset, parentSize)
//                                draggingColor = dragColor
//                                onChange(key, dragColor, offset, parentSize)
//                            }
//
//                            val upColor = renderer.colorForOffset(mergedColor.value, offset, size)
//                            draggingKey = null
//                            onDone(key, upColor, offset, size)
//                        }
//                    }
                ) {
                    handle(mergedColor.value, interactionSource)
                }
            }
        }
    }
}

@Composable
fun KmpColorPickerHandle(
    color: HsvColor,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier.size(30.dp),
) {
    Box(
        modifier = modifier
            .border(2.dp, color = Color.White, CircleShape)
            .outerShadow(
                blur = 2.dp,
                offset = DpOffset(0.dp, 1.dp),
                color = Color.Black.copy(alpha = .5f),
                shape = CircleShape,
            )
            .background(Color.White, CircleShape)
            .background(color.toColor(), CircleShape)
    )
}


private fun cartesianDistance(p1: Offset, p2: Offset): Float {
    val dx = p2.x - p1.x
    val dy = p2.y - p1.y
    return sqrt(dx.pow(2) + dy.pow(2))
}
