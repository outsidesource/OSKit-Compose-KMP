package com.outsidesource.oskitcompose.color

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.outsidesource.oskitcompose.form.KmpSlider
import com.outsidesource.oskitcompose.form.KmpSliderDirection
import com.outsidesource.oskitcompose.form.KmpSliderStyles
import com.outsidesource.oskitcompose.modifier.outerShadow

@Composable
fun KmpColorPickerHueSlider(
    color: HsvColor,
    onChange: (HsvColor) -> Unit,
    modifier: Modifier = Modifier,
    trackSize: Dp = 24.dp,
    direction: KmpSliderDirection = KmpSliderDirection.Horizontal,
) {
    val localColor = rememberUpdatedState(color)

    KmpColorPickerComponentSlider(
        modifier = modifier,
        value = localColor.value.hue,
        range = 0f..360f,
        trackSize = trackSize,
        color = color.copy(saturation = 1f, value = 1f, alpha = 1f),
        onChange = { onChange(localColor.value.copy(hue = it)) },
        direction = direction,
        draw = {
            val start = if (direction == KmpSliderDirection.Horizontal) Offset.Zero else Offset(0f, size.height)
            val end = if (direction == KmpSliderDirection.Horizontal) Offset(size.width, 0f) else Offset.Zero

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
    direction: KmpSliderDirection = KmpSliderDirection.Horizontal,
) {
    val localColor = rememberUpdatedState(color)

    KmpColorPickerComponentSlider(
        modifier = modifier,
        value = localColor.value.saturation * 100,
        range = 0f..100f,
        trackSize = trackSize,
        color = color.copy(alpha = 1f),
        onChange = { onChange(localColor.value.copy(saturation = (it / 100f).coerceIn(0f, 1f))) },
        direction = direction,
        draw = {
            val start = if (direction == KmpSliderDirection.Horizontal) Offset.Zero else Offset(0f, size.height)
            val end = if (direction == KmpSliderDirection.Horizontal) Offset(size.width, 0f) else Offset.Zero

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
    direction: KmpSliderDirection = KmpSliderDirection.Horizontal,
) {
    val localColor = rememberUpdatedState(color)

    KmpColorPickerComponentSlider(
        modifier = modifier,
        value = localColor.value.value * 100,
        range = 0f..100f,
        trackSize = trackSize,
        color = color.copy(alpha = 1f),
        onChange = { onChange(localColor.value.copy(value = (it / 100f).coerceIn(0f, 1f))) },
        direction = direction,
        draw = {
            val start = if (direction == KmpSliderDirection.Horizontal) Offset.Zero else Offset(0f, size.height)
            val end = if (direction == KmpSliderDirection.Horizontal) Offset(size.width, 0f) else Offset.Zero

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
    direction: KmpSliderDirection = KmpSliderDirection.Horizontal,
) {
    val localColor = rememberUpdatedState(color)

    KmpColorPickerComponentSlider(
        modifier = modifier,
        value = localColor.value.alpha * 100,
        range = 0f..100f,
        trackSize = trackSize,
        color = color,
        onChange = { onChange(localColor.value.copy(alpha = (it / 100f).coerceIn(0f, 1f))) },
        direction = direction,
        draw = {
            val start = if (direction == KmpSliderDirection.Horizontal) Offset.Zero else Offset(0f, size.height)
            val end = if (direction == KmpSliderDirection.Horizontal) Offset(size.width, 0f) else Offset.Zero

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
    value: Float,
    range: ClosedRange<Float>,
    onChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    trackSize: Dp,
    direction: KmpSliderDirection = KmpSliderDirection.Horizontal,
    draw: DrawScope.() -> Unit,
) {
    KmpSlider(
        modifier = modifier,
        value = value,
        range = range,
        onChange = onChange,
        valueLabelSlot = null,
        direction = direction,
        styles = remember {
            KmpSliderStyles(
                trackThickness = trackSize,
                trackShape = CircleShape,
            )
        },
        trackDecoratorSlot = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .drawBehind { draw() }
            )
        },
        thumbSlot = {
            Box(
                modifier = Modifier
                    .focusable(isEnabled, remember { MutableInteractionSource() })
                    .onThumbKeyEvent(it, this)
                    .size(trackSize)
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
    )
}