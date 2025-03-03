package com.outsidesource.oskitcompose.color

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.outsidesource.oskitcompose.modifier.outerShadow

@Composable
fun KmpColorPickerHueSlider(
    color: HsvColor,
    onChange: (HsvColor) -> Unit,
    modifier: Modifier = Modifier,
    trackSize: Dp = 24.dp,
    direction: ColorPickerSliderDirection = ColorPickerSliderDirection.Horizontal,
) {
    val localColor = rememberUpdatedState(color)

    KmpColorPickerComponentSlider(
        modifier = modifier,
        trackSize = trackSize,
        color = color.copy(saturation = 1f, value = 1f, alpha = 1f),
        onChange = { onChange(localColor.value.copy(hue = (it * 360f).coerceIn(0f, 360f))) },
        position = localColor.value.hue / 360f,
        direction = direction,
        draw = {
            val start = if (direction == ColorPickerSliderDirection.Horizontal) Offset.Zero else Offset(0f, size.height)
            val end = if (direction == ColorPickerSliderDirection.Horizontal) Offset(size.width, 0f) else Offset.Zero

            drawRect(
                brush = Brush.linearGradient(
                    start = start,
                    end = end,
                    colors = hueColors,
                ),
            )
        }
    )
}

@Composable
fun KmpColorPickerSaturationSlider(
    color: HsvColor,
    onChange: (HsvColor) -> Unit,
    modifier: Modifier = Modifier,
    trackSize: Dp = 24.dp,
    direction: ColorPickerSliderDirection = ColorPickerSliderDirection.Horizontal,
) {
    val localColor = rememberUpdatedState(color)

    KmpColorPickerComponentSlider(
        modifier = modifier,
        trackSize = trackSize,
        color = color.copy(alpha = 1f),
        onChange = { onChange(localColor.value.copy(saturation = it.coerceIn(0f, 1f))) },
        position = localColor.value.saturation / 1f,
        direction = direction,
        draw = {
            val start = if (direction == ColorPickerSliderDirection.Horizontal) Offset.Zero else Offset(0f, size.height)
            val end = if (direction == ColorPickerSliderDirection.Horizontal) Offset(size.width, 0f) else Offset.Zero

            drawRect(
                brush = Brush.linearGradient(
                    start = start,
                    end = end,
                    colors = listOf(
                        localColor.value.copy(saturation = 0f, alpha = 1f).toColor(),
                        localColor.value.copy(saturation = 1f, alpha = 1f).toColor()
                    ),
                ),
            )
        }
    )
}

@Composable
fun KmpColorPickerValueSlider(
    color: HsvColor,
    onChange: (HsvColor) -> Unit,
    modifier: Modifier = Modifier,
    trackSize: Dp = 24.dp,
    direction: ColorPickerSliderDirection = ColorPickerSliderDirection.Horizontal,
) {
    val localColor = rememberUpdatedState(color)

    KmpColorPickerComponentSlider(
        modifier = modifier,
        trackSize = trackSize,
        color = color.copy(alpha = 1f),
        onChange = { onChange(localColor.value.copy(value = it.coerceIn(0f, 1f))) },
        position = localColor.value.value / 1f,
        direction = direction,
        draw = {
            val start = if (direction == ColorPickerSliderDirection.Horizontal) Offset.Zero else Offset(0f, size.height)
            val end = if (direction == ColorPickerSliderDirection.Horizontal) Offset(size.width, 0f) else Offset.Zero

            drawRect(
                brush = Brush.linearGradient(
                    start = start,
                    end = end,
                    colors = listOf(
                        localColor.value.copy(value = 0f, alpha = 1f).toColor(),
                        localColor.value.copy(value = 1f, alpha = 1f).toColor()
                    ),
                ),
            )
        }
    )
}

@Composable
fun KmpColorPickerAlphaSlider(
    color: HsvColor,
    onChange: (HsvColor) -> Unit,
    modifier: Modifier = Modifier,
    trackSize: Dp = 24.dp,
    direction: ColorPickerSliderDirection = ColorPickerSliderDirection.Horizontal,
) {
    val localColor = rememberUpdatedState(color)

    KmpColorPickerComponentSlider(
        modifier = modifier,
        trackSize = trackSize,
        color = color,
        onChange = { onChange(localColor.value.copy(alpha = it.coerceIn(0f, 1f))) },
        position = localColor.value.alpha / 1f,
        direction = direction,
        draw = {
            val start = if (direction == ColorPickerSliderDirection.Horizontal) Offset.Zero else Offset(0f, size.height)
            val end = if (direction == ColorPickerSliderDirection.Horizontal) Offset(size.width, 0f) else Offset.Zero

            drawRect(brush = AlphaCheckerShader(density))
            drawRect(
                brush = Brush.linearGradient(
                    start = start,
                    end = end,
                    colors = listOf(
                        localColor.value.copy(alpha = 0f).toColor(),
                        localColor.value.copy(alpha = 1f).toColor()
                    ),
                ),
            )
        }
    )
}

@Composable
private fun KmpColorPickerComponentSlider(
    color: HsvColor,
    onChange: (Float) -> Unit,
    position: Float,
    modifier: Modifier = Modifier,
    trackSize: Dp,
    direction: ColorPickerSliderDirection = ColorPickerSliderDirection.Horizontal,
    draw: DrawScope.() -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = when (direction) {
            ColorPickerSliderDirection.Horizontal -> Alignment.CenterStart
            ColorPickerSliderDirection.Vertical -> Alignment.TopCenter
        },
    ) {
        Box(
            modifier =
                when (direction) {
                    ColorPickerSliderDirection.Horizontal -> Modifier.fillMaxWidth().height(trackSize)
                    ColorPickerSliderDirection.Vertical -> Modifier.fillMaxHeight().width(trackSize)
                }
                    .clip(CircleShape)
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val dimension = when (direction) {
                                ColorPickerSliderDirection.Horizontal -> size.width
                                else -> size.height
                            }

                            val down = awaitFirstDown()
                            val pos = if (direction == ColorPickerSliderDirection.Horizontal) down.position.x else size.height - down.position.y
                            onChange((pos - (trackSize.toPx() / 2)) / (dimension - trackSize.toPx()))

                            drag(down.id) {
                                val pos = if (direction == ColorPickerSliderDirection.Horizontal) it.position.x else size.height - it.position.y
                                onChange((pos - (trackSize.toPx() / 2)) / (dimension - trackSize.toPx()))
                                it.consume()
                            }
                        }
                    }
                    .drawBehind { draw() }
        )
        Box(
            modifier = Modifier
                .size(trackSize)
                .graphicsLayer {
                    when (direction) {
                        ColorPickerSliderDirection.Horizontal ->
                            translationX = (position * (constraints.maxWidth - trackSize.toPx()))
                        ColorPickerSliderDirection.Vertical ->
                            translationY = ((1f - position) * (constraints.maxHeight - trackSize.toPx()))
                    }
                }
                .padding(2.dp)
                .outerShadow(
                    blur = 2.dp,
                    offset = DpOffset(0.dp, 1.dp),
                    color = Color.Black.copy(alpha = .5f),
                    shape = CircleShape,
                )
                .background(Color.White, CircleShape)
                .background(color.toColor(), CircleShape)
                .border(2.dp, Color.White, CircleShape)
        )
    }
}

enum class ColorPickerSliderDirection {
    Vertical,
    Horizontal
}