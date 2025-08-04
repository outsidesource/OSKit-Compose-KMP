package com.outsidesource.oskitcompose.form

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.zIndex
import com.outsidesource.oskitcompose.modifier.OuterShadow
import com.outsidesource.oskitcompose.modifier.outerShadow
import com.outsidesource.oskitcompose.popup.Modal
import com.outsidesource.oskitkmp.lib.snapTo
import com.outsidesource.oskitkmp.text.KmpNumberFormatter
import com.outsidesource.oskitkmp.text.parseFloatOrNull
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.*


/**
 * A flexible and customizable Slider
 *
 * @param value The current value of the slider
 * @param range The allowable range of the current value
 * @param step The increment between selectable values
 * @param isEnabled If interaction with the slider is enabled or not
 * @param ticks The list of ticks to mark on the slider
 * @param units The units the slider value represents (i.e., millimeters, Hz, dB, .etc.)
 * @param label The label for the slider. Setting to null will not render any label.
 * @param valueFormatter Formats the float value into a string for display in the value label and manual entry modal
 * @param direction The direction the slider is displayed; horizontal (default) or vertical
 * @param logarithmic Sets the scale interpretation to be logarithmic rather than linear
 *
 * @param styles Styles to customize the look and feel of default elements
 *
 * @param layoutSlot An optional composable to control the layout of the Slider
 * @param labelSlot An optional composable to control the look and layout of the label
 * @param valueLabelSlot An optional composable to control the look and layout of the value label. Setting to null will not render a value label.
 * @param thumbSlot An optional composable to control the look and layout of the Thumb
 * @param trackDecoratorSlot An optional composable to control the look and layout of the track
 * @param manualEntryState Mutable state to control if the manual entry is open or not
 * @param manualEntrySlot An optional composable to control the look and layout of manual entry
 *
 * @param onDragStart The callback for when the user begins interacting with a thumb
 * @param onDragDone The callback for when the user stops interacting with a thumb
 * @param onChange The callback for when the value changes
 */
@Composable
fun KmpSlider(
    value: Float,
    range: ClosedRange<Float>,
    step: Float = 1f,
    isEnabled: Boolean = true,
    ticks: List<SliderTick> = emptyList(),
    units: String? = null,
    label: String? = null,
    valueFormatter: ((value: Float) -> String) = remember { { it.roundToInt().toString() } },
    modifier: Modifier = Modifier,
    direction: KmpSliderDirection = KmpSliderDirection.Horizontal,
    logarithmic: Boolean = false,

    styles: KmpSliderStyles = remember { KmpSliderStyles() },

    layoutSlot: @Composable KmpSliderScope.() -> Unit = { DefaultLayout() },
    labelSlot: @Composable KmpSliderScope.() -> Unit = { Label() },
    valueLabelSlot: @Composable (KmpSliderScope.() -> Unit)? = { ValueLabel() },
    thumbSlot: @Composable KmpSliderScope.(key: String) -> Unit = { Thumb(it) },
    trackDecoratorSlot: @Composable KmpSliderTrackScope.(trackFill: @Composable KmpSliderTrackScope.() -> Unit) -> Unit =
        { TrackDecoration { it() } },
    manualEntryState: MutableState<Boolean>? = remember { mutableStateOf(false) },
    manualEntrySlot: KmpSliderManualEntrySlot = { isVisible, valueString, onTextChange, onCancel, onCommit ->
        ManualEntryModal(
            isVisible = isVisible,
            valueString = valueString,
            onTextChange = onTextChange,
            onCancel = onCancel,
            onCommit = onCommit,
        )
    },

    onDragStart: (Float) -> Unit = { },
    onDragDone: (Float) -> Unit = { },
    onChange: (Float) -> Unit,
) = KmpSlider(
    values = mapOf("" to value),
    range = range,
    step = step,
    isEnabled = isEnabled,
    ticks = ticks,
    units = units,
    label = label,
    valueFormatter = valueFormatter,
    modifier = modifier,
    direction = direction,
    logarithmic = logarithmic,

    styles = styles,

    layoutSlot = layoutSlot,
    labelSlot = labelSlot,
    valueLabelSlot = valueLabelSlot,
    thumbSlot = thumbSlot,
    trackDecoratorSlot = trackDecoratorSlot,
    manualEntryState = manualEntryState,
    manualEntrySlot = manualEntrySlot,

    onDragStart = { onDragStart(it.values.firstOrNull() ?: return@KmpSlider) },
    onDragDone = { onDragDone(it.values.firstOrNull() ?: return@KmpSlider) },
    onChange = { onChange(it.values.firstOrNull() ?: return@KmpSlider) },
)


/**
 * A flexible and customizable Slider
 *
 * @param value The current value of the slider
 * @param range The allowable range of the current value
 * @param step The increment between selectable values
 * @param isEnabled If interaction with the slider is enabled or not
 * @param ticks The list of ticks to mark on the slider
 * @param units The units the slider value represents (i.e., millimeters, Hz, dB, .etc.)
 * @param label The label for the slider. Setting to null will not render a label.
 * @param valueFormatter Formats the float value into a string for display in the value label and manual entry modal
 * @param direction The direction the slider is displayed; horizontal (default) or vertical
 * @param multiThumbMode Sets the mode multiple thumbs should work.
 *   * Range: show all thumbs and allow them to be interacted with individually
 *   * Group: Show one thumb for all values. The thumb will grow on the main axis to show the full size of the values. Moving the thumb will adjust all values by the same distance.
 * @param deadband The minimum allowed distance between two thumbs. This is only used when [multiThumbMode] is Range.
 * @param logarithmic Sets the scale interpretation to be logarithmic rather than linear
 *
 * @param styles Styles to customize the look and feel of default elements
 *
 * @param layoutSlot An optional composable to control the layout of the Slider
 * @param labelSlot An optional composable to control the look and layout of the label
 * @param valueLabelSlot An optional composable to control the look and layout of the value label. Setting to null will not render a value label.
 * @param thumbSlot An optional composable to control the look and layout of the Thumb
 * @param trackDecoratorSlot An optional composable to control the look and layout of the track
 * @param manualEntryState Mutable state to control if the manual entry is open or not
 * @param manualEntrySlot An optional composable to control the look and layout of manual entry
 *
 * @param onDragStart The callback for when the user begins interacting with a thumb
 * @param onDragDone The callback for when the user stops interacting with a thumb
 * @param onChange The callback for when the value changes. Only changed thumb values will be passed.
 */
@Composable
fun KmpSlider(
    values: Map<String, Float>,
    range: ClosedRange<Float>,
    step: Float = 1f,
    isEnabled: Boolean = true,
    ticks: List<SliderTick> = emptyList(),
    units: String? = null,
    label: String? = null,
    valueFormatter: ((value: Float) -> String) = remember { { it.roundToInt().toString() } },
    modifier: Modifier = Modifier,
    direction: KmpSliderDirection = KmpSliderDirection.Horizontal,
    multiThumbMode: MultiThumbMode = MultiThumbMode.Range,
    deadband: Float? = null,
    logarithmic: Boolean = false,

    styles: KmpSliderStyles = remember { KmpSliderStyles() },

    layoutSlot: @Composable KmpSliderScope.() -> Unit = { DefaultLayout() },
    labelSlot: @Composable KmpSliderScope.() -> Unit = { Label() },
    valueLabelSlot: @Composable (KmpSliderScope.() -> Unit)? = { ValueLabel() },
    thumbSlot: @Composable KmpSliderScope.(key: String) -> Unit = { Thumb(it) },
    trackDecoratorSlot: @Composable KmpSliderTrackScope.(trackFill: @Composable KmpSliderTrackScope.() -> Unit) -> Unit =
        { TrackDecoration { it() } },
    manualEntryState: MutableState<Boolean>? = remember { mutableStateOf(false) },
    manualEntrySlot: KmpSliderManualEntrySlot = { isVisible, valueString, onTextChange, onCancel, onCommit ->
        ManualEntryModal(
            isVisible = isVisible,
            valueString = valueString,
            onTextChange = onTextChange,
            onCancel = onCancel,
            onCommit = onCommit,
        )
    },

    onDragStart: (Map<String, Float>) -> Unit = { },
    onDragDone: (Map<String, Float>) -> Unit = { },
    onChange: (Map<String, Float>) -> Unit,
) {
    val draggingKey = remember { mutableStateOf<String?>(null) }
    val draggingValues = remember { mutableStateOf(mapOf<String, Float>()) }
    val userValues = rememberUpdatedState(values)
    val currentValues = rememberUpdatedState(if (draggingKey.value != null) draggingValues.value else values)

    val scope = KmpSliderScope(
        userValues = userValues,
        currentValues = currentValues,
        range = range,
        step = step,
        deadband = deadband,
        multiThumbMode = multiThumbMode,
        direction = direction,
        units = units,
        label = label,
        valueFormatter = valueFormatter,
        ticks = ticks,
        styles = styles,
        isEnabled = isEnabled,
        manualEntryState = manualEntryState,
        manualEntrySlot = manualEntrySlot,
        labelSlot = labelSlot,
        valueLabelSlot = valueLabelSlot,
        thumbSlot = thumbSlot,
        trackDecoratorSlot = trackDecoratorSlot,
        logarithmic = logarithmic,
        onDragStart = onDragStart,
        onDragDone = onDragDone,
        onChange = onChange,
        density = LocalDensity.current,
        draggingValues = draggingValues,
        draggingKey = draggingKey,
    )

    Box(
        modifier = modifier
            .semantics {
                contentDescription = "Slider"
                stateDescription = scope.formatCurrentValueLabel()
            },
    ) {
        scope.ManualEntrySlot()
        scope.layoutSlot()
    }
}

@Composable
private fun KmpSliderScope.ManualEntrySlot() {
    val localManualValue = remember(manualEntryState?.value, isGroupThumbMode) {
        if (isGroupThumbMode) return@remember mutableStateOf("")
        mutableStateOf(valueFormatter(currentValues.value.values.firstOrNull() ?: range.start))
    }

    val onManualEntryTextChange: (String) -> Unit = remember(localManualValue) {
        return@remember { localManualValue.value = it.replace(Regex("[^\\-\\.0-9,]"), "") }
    }

    val onManualEntryCommit = remember(localManualValue) {
        return@remember {
            val values = currentValues.value.mapValues {
                (localManualValue.value.parseFloatOrNull() ?: it.value).snapTo(step).coerceIn(range)
            }
            onChange(values)
            manualEntryState?.value = false
        }
    }

    val onManualEntryCancel = remember { { manualEntryState?.value = false } }

    manualEntrySlot(
        manualEntryState?.value ?: false,
        localManualValue.value,
        onManualEntryTextChange,
        onManualEntryCancel,
        onManualEntryCommit,
    )
}

@Composable
fun KmpSliderScope.DefaultLayout() {
    if (direction.isHorizontal) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (label != null || valueLabelSlot != null) 4.dp else 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (label != null) labelSlot()
                if (label == null && valueLabelSlot != null) Spacer(modifier = Modifier.weight(1f))
                valueLabelSlot?.invoke(this@DefaultLayout)
            }
            Track()
        }
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (label != null) labelSlot()
            Box(modifier = Modifier.weight(1f)) {
                Track()
            }
            valueLabelSlot?.invoke(this@DefaultLayout)
        }
    }
}

@Composable
fun KmpSliderScope.Label(
    modifier: Modifier = Modifier,
) {
    if (label == null) return
    Text(
        modifier = modifier,
        text = label,
        style = styles.labelTextStyle,
        overflow = styles.labelTextOverflow,
        maxLines = 1
    )
}

@Composable
fun KmpSliderScope.ValueLabel(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
) {
    Box(
        modifier = modifier
            .clip(styles.valueLabelShape)
            .clickable(enabled = isEnabled, onClick = { manualEntryState?.let { it.value = true } })
            .background(if (isEnabled) styles.valueLabelBackground else styles.valueLabelBackgroundDisabled)
            .padding(padding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = formatCurrentValueLabel(),
            style = styles.valueLabelTextStyle,
            overflow = styles.valueLabelTextOverflow,
            maxLines = 1,
        )
    }
}

@Composable
fun KmpSliderScope.Track() {
    val textMeasurer = rememberTextMeasurer(ticks.size)
    val (ticksSize, ticksOffset, ticksLabels) = remember(ticks, textMeasurer) { measureTicks(ticks, textMeasurer) }

    Layout(
        modifier = Modifier
            .then(if (direction.isHorizontal) Modifier.fillMaxWidth() else Modifier.fillMaxHeight())
            .pointerInput(range, step, isEnabled) {
                if (!isEnabled) return@pointerInput

                detectTapGestures {
                    val key = findClosestKeyForPos(it, size, this)
                    val change = calculatePointerChange(position = it, size = size, key = key)
                    if (change.isNotEmpty()) onChange(change)
                }
            }
            .pointerInput(range, step, isEnabled) {
                if (!isEnabled) return@pointerInput

                awaitEachGesture {
                    val down = awaitFirstDown()
                    val startingValues = userValues.value.toMap()
                    val valueRange = calculateValueRange(startingValues)
                    val isOnThumb = isGestureOnThumb(this@Track, down, valueRange)
                    val key = findClosestKeyForPos(down.position, size, this)

                    val onDrag = fun (inputChange: PointerInputChange, callOnStart: Boolean): Map<String, Float> {
                        inputChange.consume()
                        val change = calculatePointerChange(
                            position = inputChange.position,
                            size = size,
                            values = startingValues,
                            valueRange = valueRange,
                            key = key,
                        )

                        if (callOnStart) onDragStart(change)
                        if (change.isNotEmpty()) onChange(change)
                        draggingValues.value = userValues.value.toMutableMap().apply {
                            change.forEach { this[it.key] = it.value }
                        }
                        return change
                    }

                    var lastChange = emptyMap<String, Float>()
                    if (isOnThumb || down.type == PointerType.Mouse) {
                        lastChange = onDrag(down, true)
                    } else if (!isOnThumb) {
                        val dragChange = awaitTouchSlopOrCancellation(down.id) { change, offset ->
                            if (offset.mainAxis.absoluteValue > offset.crossAxis.absoluteValue) change.consume()
                        } ?: return@awaitEachGesture
                        lastChange = onDrag(dragChange, true)
                    }

                    draggingKey.value = key

                    drag(down.id) {
                        lastChange = onDrag(it, false)
                    }

                    onDragDone(lastChange)
                    draggingKey.value = null
                    draggingValues.value = emptyMap()
                }
            },
        measurePolicy = sliderMeasurePolicy(ticksSize, ticksOffset),
        content = {
            val trackScope = KmpSliderTrackScope(
                sliderScope = this,
                ticksSize = ticksSize,
                ticksOffset = ticksOffset,
                tickLabelMeasurements = ticksLabels
            )

            Box(
                modifier = if (direction.isHorizontal) {
                    Modifier
                        .fillMaxWidth()
                        .height(styles.trackThickness)
                } else {
                    Modifier
                        .fillMaxHeight()
                        .width(styles.trackThickness)
                }
            ) {
                trackDecoratorSlot(trackScope) {
                    TrackFill()
                }
            }

            trackScope.Ticks()

            if (isGroupThumbMode) {
                thumbSlot("")
            } else {
                currentValues.value.entries.forEach { (key, _) ->
                    Box(modifier = Modifier
                        .layoutId(key)
                        .zIndex(if (draggingKey.value == key) 1.1f else 1f)
                    ) {
                        thumbSlot(key)
                    }
                }
            }
        }
    )
}

@Composable
fun KmpSliderTrackScope.TrackDecoration(
    modifier: Modifier = Modifier,
    trackFill: @Composable KmpSliderTrackScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(if (sliderScope.direction.isHorizontal) {
                Modifier.padding(horizontal = if (styles.isTrackFullSize) 0.dp else styles.thumbSize.mainAxisSize / 2)
            } else {
                Modifier.padding(vertical = if (styles.isTrackFullSize) 0.dp else styles.thumbSize.mainAxisSize / 2)
            })
            .clip(styles.trackShape)
            .background(brush = styles.trackBackground)
            .then(modifier),
    ) {
        trackFill()
    }
}

@Composable
private fun KmpSliderTrackScope.TrackFill() {
    val density = LocalDensity.current
    val thumbSizePx = remember (styles.thumbSize, density) { with (density) { styles.thumbSize.crossAxisSize.toPx() } }

    val posMultRange = remember(
        currentValues.value,
        range,
        step,
        sliderScope.multiThumbMode,
        sliderScope.draggingValues.value,
        sliderScope.draggingKey.value,
    ) {
        var minValue = range.endInclusive
        var maxValue = range.start

        for ((_, value) in currentValues.value.entries) {
            minValue = min(value, minValue)
            maxValue = max(value, maxValue)
        }

        if (sliderScope.direction.isHorizontal) {
            calculatePosMultForValue(minValue, sliderScope)..calculatePosMultForValue(maxValue, sliderScope)
        } else {
            calculatePosMultForValue(maxValue, sliderScope)..calculatePosMultForValue(minValue, sliderScope)
        }
    }

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val trackFill = if (sliderScope.isEnabled) styles.trackFill else styles.trackFillDisabled
        val mainAxisSize = size.mainAxisSize - if (styles.isTrackFullSize) thumbSizePx else 0f
        val fullSizeTrackOffset = if (styles.isTrackFullSize) thumbSizePx / 2 else 0f

        if (currentValues.value.size > 1 && sliderScope.multiThumbMode == MultiThumbMode.Range) {
            val start = (mainAxisSize * posMultRange.start) + fullSizeTrackOffset
            val end = (mainAxisSize * posMultRange.endInclusive) + fullSizeTrackOffset
            if (sliderScope.direction.isHorizontal) {
                clipRect(left = start, right = end) { drawRect(brush = trackFill) }
            } else {
                clipRect(top = start, bottom = end) { drawRect(brush = trackFill) }
            }
            return@Canvas
        }

        val posMult = (posMultRange.start + posMultRange.endInclusive) / 2
        val value = currentValues.value.values.firstOrNull() ?: range.start

        when (styles.trackFillAlignment) {
            SliderAlignment.Start -> {
                val size = if (sliderScope.direction.isHorizontal) {
                    Size(
                        width = (mainAxisSize * posMult) + fullSizeTrackOffset,
                        height = size.crossAxisSize
                    )
                } else {
                    Size(
                        height = (mainAxisSize * (1 - posMult)) + fullSizeTrackOffset,
                        width = size.crossAxisSize
                    )
                }
                val topLeft = if (sliderScope.direction.isHorizontal) {
                    Offset.Zero
                } else {
                    Offset(0f, mainAxisSize - size.mainAxisSize)
                }
                drawRect(brush = trackFill, topLeft = topLeft, size = size)
            }

            SliderAlignment.Center -> {
                val rangeCenter = (range.endInclusive - range.start) / 2
                val start =
                    (if (value >= rangeCenter) mainAxisSize / 2 else mainAxisSize * posMult) + fullSizeTrackOffset
                val end =
                    (if (value >= rangeCenter) mainAxisSize * posMult else mainAxisSize / 2) + fullSizeTrackOffset

                if (sliderScope.direction.isHorizontal) {
                    clipRect(left = start, right = end) { drawRect(brush = trackFill) }
                } else {
                    clipRect(top = start, bottom = end) { drawRect(brush = trackFill) }
                }
            }

            SliderAlignment.End -> {
                val size = if (sliderScope.direction.isHorizontal) {
                    Size(
                        width = (mainAxisSize * (1 - posMult)) + fullSizeTrackOffset,
                        height = size.crossAxisSize
                    )
                } else {
                    Size(
                        height = (mainAxisSize * posMult) + fullSizeTrackOffset,
                        width = size.crossAxisSize
                    )
                }
                val topLeft = if (sliderScope.direction.isHorizontal) {
                    Offset(x = mainAxisSize - (mainAxisSize * (1 - posMult)) + fullSizeTrackOffset, y = 0f)
                } else {
                    Offset(y = 0f, x = 0f)
                }
                drawRect(brush = trackFill, topLeft = topLeft, size = size)
            }
        }
    }
}

@Composable
fun KmpSliderScope.Thumb(
    key: String,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        modifier = Modifier
            .focusable(isEnabled, interactionSource)
            .onThumbKeyEvent(key, this)
            .fillMaxSize()
            .then(if (styles.thumbShadow != null) Modifier.outerShadow(blur = styles.thumbShadow.blur, color = styles.thumbShadow.color, shape = styles.thumbShadow.shape) else Modifier)
            .border(
                width = .5.dp,
                brush = if (isFocused) styles.trackFill else SolidColor(Color.Transparent),
                shape = styles.thumbShape,
            )
            .background(brush = if (isEnabled) styles.thumbBackground else styles.thumbBackgroundDisabled, shape = styles.thumbShape)
            .then(modifier),
    )
}

@Composable
private fun KmpSliderTrackScope.Ticks() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(styles.ticksZIndex)
            .then(if (sliderScope.direction.isHorizontal) {
                Modifier.padding(horizontal = styles.thumbSize.mainAxisSize / 2f)
            } else {
                Modifier.padding(vertical = styles.thumbSize.mainAxisSize / 2f)
            })
            .drawWithCache {
                onDrawBehind {
                    ticks.forEachIndexed { i, tick ->
                        val posMult = calculatePosMultForValue(tick.value, sliderScope)
                        val shapeMainAxisSizePx = tick.style.shapeSize.mainAxis.toPx() / 2
                        val shapePos = tick.style.shapePosition.calculate(tick.style.shapeSize.crossAxis.roundToPx()) - (tick.style.shapeSize.crossAxis.toPx() / 2)
                        val additionalOffset = if (ticksOffset < 0.dp) abs(ticksOffset.toPx()) else 0f

                        if (tick.style.shape != null) {
                            withTransform(
                                transformBlock = {
                                    if (sliderScope.direction.isHorizontal) {
                                        translate(
                                            left = (posMult * size.mainAxisSize) - shapeMainAxisSizePx,
                                            top = shapePos + additionalOffset,
                                        )
                                    } else {
                                        translate(
                                            top = (posMult * size.mainAxisSize) - shapeMainAxisSizePx,
                                            left = shapePos + additionalOffset,
                                        )
                                    }
                                },
                                drawBlock = {
                                    val outline = tick.style.shape.createOutline(
                                        size = tick.style.shapeSize.toSize(),
                                        layoutDirection = LayoutDirection.Ltr,
                                        this,
                                    )
                                    drawOutline(
                                        outline = outline,
                                        brush = tick.style.shapeBrush,
                                        style = tick.style.shapeDrawStyle,
                                    )
                                }
                            )
                        }

                        if (tick.label == null) return@forEachIndexed
                        val measurement = tickLabelMeasurements[i] ?: return@forEachIndexed

                        val topLeft = if (sliderScope.direction.isHorizontal) {
                            Offset(
                                x = (posMult * size.mainAxisSize) - measurement.size.mainAxisSize / 2,
                                y = tick.style.labelPosition.calculate(measurement.size.crossAxisSize) - (measurement.size.crossAxisSize / 2) + additionalOffset,
                            )
                        } else {
                            Offset(
                                y = (posMult * size.mainAxisSize) - measurement.size.mainAxisSize / 2,
                                x = tick.style.labelPosition.calculate(measurement.size.crossAxisSize) - (measurement.size.crossAxisSize / 2) + additionalOffset,
                            )
                        }

                        drawText(
                            textLayoutResult = measurement,
                            topLeft = topLeft,
                            shadow = tick.style.labelShadow,
                            drawStyle = tick.style.labelDrawStyle,
                            textDecoration = tick.style.labelTextDecoration,
                        )
                    }
                }
            },
    )
}

typealias KmpSliderManualEntrySlot = @Composable KmpSliderScope.(
    isVisible: Boolean,
    valueString: String,
    onTextChange: (String) -> Unit,
    onCancel: () -> Unit,
    onCommit: () -> Unit,
) -> Unit

@Composable
fun KmpSliderScope.ManualEntryModal(
    isVisible: Boolean,
    valueString: String,
    onTextChange: (String) -> Unit,
    onCancel: () -> Unit,
    onCommit: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    Modal(
        isVisible = isVisible,
        onDismissRequest = onCancel,
    ) {
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        var value by remember {
            mutableStateOf(TextFieldValue(valueString, selection = TextRange(start = 0, end = valueString.length)))
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (label != null) {
                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (label != null) {
                Text(
                    text = "${valueFormatter(range.start)}${units ?: ""} \u2014 " +
                            "${valueFormatter(range.endInclusive)}${units ?: ""}",
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onKeyEvent {
                        if (!isEnabled) return@onKeyEvent false
                        if (it.key != Key.Enter || it.type != KeyEventType.KeyUp) return@onKeyEvent false
                        onCommit()
                        return@onKeyEvent true
                    },
                value = value,
                onValueChange = {
                    value = it
                    onTextChange(it.text)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                keyboardActions = KeyboardActions(onDone = { onCommit() }),
                singleLine = true,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End),
            ) {
                TextButton(onClick = onCancel) { Text("Cancel") }
                TextButton(onClick = onCommit) { Text("Ok") }
            }
        }
    }
}

private fun Modifier.onThumbKeyEvent(key: String, scope: KmpSliderScope) = with(scope) {
    this@onThumbKeyEvent.onKeyEvent {
        if (it.type != KeyEventType.KeyDown) return@onKeyEvent false

        val valueRange = calculateValueRange(currentValues.value)
        val multiplier = if (it.isShiftPressed) 10f else 1f
        val distance = when (it.key) {
            Key.DirectionRight, Key.DirectionUp -> step * multiplier
            Key.DirectionLeft, Key.DirectionDown -> -step * multiplier
            else -> return@onKeyEvent false
        }
        val clampedDistance = clampDistance(distance, valueRange)

        if (isGroupThumbMode) {
            onChange(currentValues.value.mapValues { entry -> (entry.value + clampedDistance).coerceIn(range).snapTo(step) })
        } else if (isRangeThumbMode) {
            val value = currentValues.value[key] ?: return@onKeyEvent true
            val newValue = clampToDeadband(key, (value + clampedDistance).coerceIn(range).snapTo(step))
            onChange(mapOf(key to newValue))
        } else {
            val value = currentValues.value[key] ?: return@onKeyEvent true
            onChange(mapOf(key to (value + clampedDistance).coerceIn(range).snapTo(step)))
        }
        true
    }
}

private fun KmpSliderScope.measureTicks(
    ticks: List<SliderTick>,
    textMeasurer: TextMeasurer,
): TicksMeasurement = with(density) {
    var minPos = 0f
    var maxPos = 0f

    val labelMeasurements = MutableList<TextLayoutResult?>(ticks.size) { null }

    ticks.forEachIndexed { i, tick ->
        val labelMeasurement = if (tick.label != null) {
            textMeasurer.measure(text = tick.label, style = tick.style.labelTextStyle, maxLines = 1)
        } else {
            null
        }
        labelMeasurements[i] = labelMeasurement

        val shapeSize = tick.style.shapeSize.crossAxis.toPx()
        val shapeOffset = tick.style.shapePosition.calculate(tick.style.shapeSize.crossAxis.roundToPx())
        val labelSize = tick.label?.let { labelMeasurement?.size?.crossAxisSize?.toFloat() } ?: 0f
        val labelOffset = tick.label?.let { tick.style.labelPosition.calculate(labelMeasurement?.size?.crossAxisSize ?: return@let 0f) } ?: 0f

        minPos = minOf(minPos, labelOffset - (labelSize / 2), shapeOffset - (shapeSize / 2))
        maxPos = maxOf(maxPos, labelOffset + (labelSize / 2), shapeOffset + (shapeSize / 2))
    }

    return@with TicksMeasurement(
        size = (abs(maxPos) + abs(minPos)).toDp(),
        offset = minPos.toDp(),
        labels = labelMeasurements,
    )
}

@Immutable
private data class TicksMeasurement(
    val size: Dp,
    val offset: Dp,
    val labels: List<TextLayoutResult?>,
)

private fun KmpSliderScope.calculatePointerChange(
    position: Offset,
    size: IntSize,
    values: Map<String, Float> = userValues.value,
    valueRange: ClosedRange<Float> = calculateValueRange(values),
    key: String,
): Map<String, Float> = with(density) {
    val thumbSizePx = styles.thumbSize.mainAxisSize.toPx()
    val targetValue = calculateValueForPos(
        pos = position.mainAxis - (thumbSizePx / 2),
        size = size.mainAxisSize.toFloat() - thumbSizePx,
        scope = this@calculatePointerChange,
    )

    buildMap {
        if (isGroupThumbMode) {
            val distance = calculateDistance(targetValue, valueRange)
            values.forEach {
                val newValue = (it.value + distance).snapTo(step).coerceIn(range)
                if (draggingValues.value[it.key] == newValue) return@forEach
                this[it.key] = newValue
            }
        } else if (isRangeThumbMode) {
            val newValue = clampToDeadband(key, targetValue)
            if (draggingValues.value[key] == newValue) return@buildMap
            this[key] = newValue
        } else {
            if (draggingValues.value[key] == targetValue) return@buildMap
            this[key] = targetValue
        }
    }
}

private fun KmpSliderScope.clampToDeadband(key: String, targetValue: Float): Float {
    if (deadband == null) return targetValue
    val sorted = userValues.value.entries.sortedBy { it.value }
    val position = sorted.indexOfFirst { (k, _) -> k == key }
    val min = sorted.getOrNull(position - 1)?.let { it.value + deadband } ?: range.start
    val max = sorted.getOrNull(position + 1)?.let { it.value - deadband } ?: range.endInclusive
    return targetValue.coerceIn(min..max)
}

private fun KmpSliderScope.sliderMeasurePolicy(
    ticksSize: Dp,
    ticksOffset: Dp,
) : MeasurePolicy = MeasurePolicy { measurables, constraints ->
    val track = measurables[0].measure(constraints)
    val ticks = measurables[1].measure(constraints.copyMaxCrossAxis(ticksSize.roundToPx()))
    val thumbMeasurables = measurables.subList(2, measurables.size)
    val valueList = currentValues.value.values.toList()
    val groupPosMultRange = run {
        if (currentValues.value.size == 1 || multiThumbMode != MultiThumbMode.Group) return@run 0f..0f

        var minValue = range.endInclusive
        var maxValue = range.start

        for ((_, value) in currentValues.value) {
            minValue = min(minValue, value)
            maxValue = max(maxValue, value)
        }

        calculatePosMultForValue(minValue, this@sliderMeasurePolicy)..calculatePosMultForValue(maxValue, this@sliderMeasurePolicy)
    }

    val thumbSize = run {
        if (currentValues.value.size == 1 || multiThumbMode != MultiThumbMode.Group) 0

        if (direction.isHorizontal) {
            (((groupPosMultRange.endInclusive - groupPosMultRange.start) * (constraints.maxMainAxis - styles.thumbSize.mainAxisSize.roundToPx())).roundToInt())
        } else {
            ((groupPosMultRange.start - groupPosMultRange.endInclusive) * (constraints.maxMainAxis - styles.thumbSize.mainAxisSize.roundToPx())).roundToInt()
        } + styles.thumbSize.mainAxisSize.roundToPx()
    }.coerceAtLeast(styles.thumbSize.mainAxisSize.roundToPx())

    val thumbs = thumbMeasurables.map {
        it.measure(Constraints.fixed(
            width = if (direction.isHorizontal) thumbSize else styles.thumbSize.width.roundToPx(),
            height = if (direction.isHorizontal) styles.thumbSize.height.roundToPx() else thumbSize,
        ))
    }

    val trackAndThumbSize = maxOf(track.crossAxisSize, thumbs.firstOrNull()?.crossAxisSize ?: 0)
    val tickBottom = ticksOffset.roundToPx() + ticksSize.roundToPx()
    val tickUnderhang = if (min(ticksOffset.roundToPx(), 0).absoluteValue > (trackAndThumbSize / 2)) {
        ((trackAndThumbSize / 2) + ticksOffset.roundToPx()).absoluteValue
    } else {
        0
    }
    val tickOverhang = if (tickBottom > (trackAndThumbSize / 2)) {
        tickBottom - (trackAndThumbSize / 2)
    } else {
        0
    }
    val resolvedCrossAxisSize = trackAndThumbSize + tickUnderhang + tickOverhang

    layout(
        if (direction.isHorizontal) constraints.maxWidth else resolvedCrossAxisSize,
        if (direction.isHorizontal) resolvedCrossAxisSize else constraints.maxHeight,
    ) {
        val centerLine = (trackAndThumbSize / 2) + tickUnderhang

        if (direction.isHorizontal) {
            track.place(x = 0, y = centerLine - (track.crossAxisSize / 2))
            ticks.place(x = 0, y = centerLine + ticksOffset.roundToPx())
        } else {
            track.place(x = centerLine - (track.crossAxisSize / 2), y = 0)
            ticks.place(x = centerLine + ticksOffset.roundToPx(), y = 0)
        }

        thumbs.forEachIndexed { i, thumb ->
            val actualValue = if (draggingKey.value != null && draggingKey.value == thumbMeasurables.getOrNull(i)?.layoutId) draggingValues.value[draggingKey.value] ?: currentValues.value[draggingKey.value] ?: range.start else valueList[i]
            val posMult = if (isGroupThumbMode) {
                if (direction.isHorizontal) groupPosMultRange.start else groupPosMultRange.endInclusive
            } else {
                calculatePosMultForValue(actualValue, this@sliderMeasurePolicy)
            }
            val pos = (posMult * (constraints.maxMainAxis - styles.thumbSize.mainAxisSize.roundToPx())).roundToInt()

            if (direction.isHorizontal) {
                thumb.place(x = pos, y = centerLine - (thumb.crossAxisSize / 2))
            } else {
                thumb.place(x = centerLine - (thumb.crossAxisSize / 2), y = pos)
            }
        }
    }
}

private fun AwaitPointerEventScope.isGestureOnThumb(
    scope: KmpSliderScope,
    down: PointerInputChange,
    valueRange: ClosedRange<Float>,
): Boolean = with(scope) {
    val buffer = 10.dp.toPx()

    if (isGroupThumbMode) {
        val start = calculatePosMultForValue(valueRange.start, scope) * (size.mainAxisSize - styles.thumbSize.mainAxisSize.toPx())
        val end = calculatePosMultForValue(valueRange.endInclusive, scope) * (size.mainAxisSize - styles.thumbSize.mainAxisSize.toPx())

        val thumbRect = Rect(
            mainAxisStart = if (direction.isHorizontal) start - buffer else end - buffer,
            mainAxisEnd = (if (direction.isHorizontal) end else start) + styles.thumbSize.mainAxisSize.toPx() + buffer,
            crossAxisStart = 0f - buffer,
            crossAxisEnd = styles.thumbSize.crossAxisSize.toPx() + buffer,
        )

        return thumbRect.contains(down.position)
    }

    for ((_, value) in currentValues.value) {
        val thumbMainAxisPos = calculatePosMultForValue(value, scope) *
                (size.mainAxisSize - styles.thumbSize.mainAxisSize.toPx())

        val thumbRect = Rect(
            mainAxisStart = thumbMainAxisPos - buffer,
            mainAxisEnd = thumbMainAxisPos + styles.thumbSize.mainAxisSize.toPx() + buffer,
            crossAxisStart = 0f - buffer,
            crossAxisEnd = styles.thumbSize.crossAxisSize.toPx() + buffer,
        )

        if (thumbRect.contains(down.position)) return true
    }
    false
}

private fun KmpSliderScope.findClosestKeyForPos(position: Offset, size: IntSize, density: Density): String {
    if (isGroupThumbMode) return ""
    var closestKey: String = currentValues.value.keys.first()
    var thumbPos = Float.MAX_VALUE

    with(density) {
        for ((key, value) in currentValues.value) {
            val thumbX1 = calculatePosMultForValue(value, this@findClosestKeyForPos) *
                    (size.mainAxisSize - styles.thumbSize.mainAxisSize.toPx())
            val distance = (position.mainAxis - thumbX1).absoluteValue
            if (distance < thumbPos) {
                closestKey = key
                thumbPos = distance
            }
        }
    }

    return closestKey
}

private fun KmpSliderScope.calculateValueRange(values: Map<String, Float>): ClosedRange<Float> {
    var minValue = range.endInclusive
    var maxValue = range.start

    for ((_, value) in values) {
        minValue = min(minValue, value)
        maxValue = max(maxValue, value)
    }

    return minValue..maxValue
}

private fun KmpSliderScope.calculateDistance(targetValue: Float, valueRange: ClosedRange<Float>): Float {
    val center = (valueRange.start + ((valueRange.endInclusive - valueRange.start) / 2)).snapTo(step).coerceIn(range)
    val distance = targetValue - center
    return clampDistance(distance, valueRange)
}

private fun KmpSliderScope.clampDistance(distance: Float, valueRange: ClosedRange<Float>): Float {
    if (valueRange.start + distance < range.start) return range.start - valueRange.start
    if (valueRange.endInclusive + distance > range.endInclusive) return range.endInclusive - valueRange.endInclusive
    return distance
}

private fun calculatePosMultForValue(
    value: Float,
    scope: KmpSliderScope,
): Float {
    val mult = if (scope.logarithmic) {
        ((log10(value.coerceIn(scope.range) / scope.range.start) / log10(scope.range.endInclusive / scope.range.start)))
    } else {
        (scope.range.start - value.coerceIn(scope.range)).absoluteValue / (scope.range.endInclusive - scope.range.start).absoluteValue
    }.let {
        if (it.isNaN()) 1f else it
    }

    return if (scope.direction.isHorizontal) mult else 1f - mult
}

private fun calculateValueForPos(
    pos: Float,
    size: Float,
    scope: KmpSliderScope,
): Float {
    val mult = (pos.coerceIn(0f, size) / size).let { if (scope.direction.isHorizontal) it else 1 - it }
    return if (scope.logarithmic) {
        ((10f.pow(mult * log10(scope.range.endInclusive / scope.range.start))) * scope.range.start).coerceIn(scope.range).snapTo(scope.step)
    } else {
        (scope.range.start + (mult * (scope.range.endInclusive - scope.range.start).absoluteValue)).coerceIn(scope.range).snapTo(scope.step)
    }
}

@Immutable
data class KmpSliderScope(
    val userValues: State<Map<String, Float>>,
    val currentValues: State<Map<String, Float>>,
    val range: ClosedRange<Float>,
    val step: Float,

    val deadband: Float?,
    val multiThumbMode: MultiThumbMode,
    val direction: KmpSliderDirection,
    val units: String?,
    val label: String?,
    val valueFormatter: ((value: Float) -> String),
    val ticks: List<SliderTick>,
    val styles: KmpSliderStyles,

    val isEnabled: Boolean,
    val manualEntryState: MutableState<Boolean>?,
    val manualEntrySlot: KmpSliderManualEntrySlot,
    val thumbSlot: @Composable KmpSliderScope.(key: String) -> Unit,
    val labelSlot: @Composable KmpSliderScope.() -> Unit,
    val valueLabelSlot: @Composable (KmpSliderScope.() -> Unit)?,
    val trackDecoratorSlot: @Composable KmpSliderTrackScope.(@Composable KmpSliderTrackScope.() -> Unit) -> Unit,
    val logarithmic: Boolean,
    val onDragStart: (Map<String, Float>) -> Unit,
    val onDragDone: (Map<String, Float>) -> Unit,
    val onChange: (Map<String, Float>) -> Unit,

    internal val density: Density,
    internal val draggingValues: MutableState<Map<String, Float>>,
    internal val draggingKey: MutableState<String?>,
) {

    internal fun SliderTickPosition.calculate(objectSize: Int): Float = with(density) {
        when (alignment) {
            SliderAlignment.Start -> (-(styles.trackThickness.toPx() / 2) - (objectSize / 2)) + offset.toPx()
            SliderAlignment.Center -> offset.toPx()
            SliderAlignment.End -> ((styles.trackThickness.toPx() / 2) + (objectSize / 2)) + offset.toPx()
        }
    }

    val IntSize.mainAxisSize: Int
        get() = if (direction.isHorizontal) width else height
    val IntSize.crossAxisSize: Int
        get() = if (direction.isHorizontal) height else width

    val DpSize.mainAxisSize: Dp
        get() = if (direction.isHorizontal) width else height
    val DpSize.crossAxisSize: Dp
        get() = if (direction.isHorizontal) height else width

    val Offset.mainAxis: Float
        get() = if (direction.isHorizontal) x else y
    val Offset.crossAxis: Float
        get() = if (direction.isHorizontal) y else x

    val Placeable.crossAxisSize: Int
        get() = if (direction.isHorizontal) height else width

    fun Constraints.copyMaxCrossAxis(value: Int) = if (direction.isHorizontal) {
        copy(maxHeight = value)
    } else {
        copy(maxWidth = value)
    }

    fun DpAxisSize.toSize(): Size = with(density) {
        if (direction.isHorizontal) {
            Size(width = mainAxis.toPx(), height = crossAxis.toPx())
        } else {
            Size(width = crossAxis.toPx(), height = mainAxis.toPx())
        }
    }

    val Constraints.maxMainAxis
        get() = if (direction.isHorizontal) maxWidth else maxHeight

    val isGroupThumbMode: Boolean
        get() = currentValues.value.size > 1 && multiThumbMode == MultiThumbMode.Group

    val isRangeThumbMode: Boolean
        get() = currentValues.value.size > 1 && multiThumbMode == MultiThumbMode.Range

    fun formatCurrentValueLabel(): String {
        if (currentValues.value.size == 1) return "${valueFormatter(currentValues.value.values.firstOrNull() ?: range.start)}${units ?: ""}"

        val valueRange = calculateValueRange(currentValues.value)
        if (valueRange.start == valueRange.endInclusive) return "${valueFormatter(currentValues.value.values.firstOrNull() ?: range.start)}${units ?: ""}"
        return "${valueFormatter(valueRange.start)}${units ?: ""} \u2014 ${valueFormatter(valueRange.endInclusive)}${units ?: ""}"
    }

    fun Rect(
        mainAxisStart: Float,
        mainAxisEnd: Float,
        crossAxisStart: Float,
        crossAxisEnd: Float,
    ): Rect = Rect(
        left = if (direction.isHorizontal) mainAxisStart else crossAxisStart,
        top = if (direction.isHorizontal) crossAxisStart else mainAxisStart,
        right = if (direction.isHorizontal) mainAxisEnd else crossAxisEnd,
        bottom = if (direction.isHorizontal) crossAxisEnd else mainAxisEnd,
    )
}

@Immutable
data class KmpSliderTrackScope(
    val sliderScope: KmpSliderScope,
    val ticksSize: Dp,
    val ticksOffset: Dp,
    val tickLabelMeasurements: List<TextLayoutResult?>,
) {
    val range = sliderScope.range
    val step = sliderScope.step
    val ticks = sliderScope.ticks
    val currentValues = sliderScope.currentValues
    val styles = sliderScope.styles

    internal fun SliderTickPosition.calculate(objectSize: Int): Float = with(sliderScope) { calculate(objectSize) }

    val Size.mainAxisSize: Float
        get() = if (sliderScope.direction.isHorizontal) width else height
    val Size.crossAxisSize: Float
        get() = if (sliderScope.direction.isHorizontal) height else width

    val IntSize.mainAxisSize: Int
        get() = if (sliderScope.direction.isHorizontal) width else height
    val IntSize.crossAxisSize: Int
        get() = if (sliderScope.direction.isHorizontal) height else width

    val DpSize.mainAxisSize: Dp
        get() = if (sliderScope.direction.isHorizontal) width else height
    val DpSize.crossAxisSize: Dp
        get() = if (sliderScope.direction.isHorizontal) height else width

    fun DpAxisSize.toSize(): Size = with(sliderScope) { this@toSize.toSize() }
}

/**
 * Styles for the Slider
 *
 * @param trackFillAlignment The alignment of the track fill.
 *  * Start (default): The fill will go from left-to-right for horizontal and bottom-to-top for vertical sliders
 *  * Center: The fill will start at the center and fill to the left or right for horizontal sliders or top or bottom for vertical sliders.
 *  * End: The fill will go from right-to-left for horizontal sliders and top-to-bottom for vertical sliders
 * @param ticksZIndex Allows adjusting the Z-layering of the ticks (i.e., whether they display above or below their siblings)
 * @param isTrackFullSize If true, the thumb edges align with the track edges. If false (default), the thumb center aligns with the edges of the track.
 */
@Immutable
data class KmpSliderStyles(
    val labelTextStyle: TextStyle = DefaultLabelStyle,
    val labelTextOverflow: TextOverflow = TextOverflow.Ellipsis,

    val valueLabelTextStyle: TextStyle = DefaultValueLabelStyle,
    val valueLabelTextOverflow: TextOverflow = TextOverflow.Clip,
    val valueLabelBackground: Brush = SolidColor(Color(0xFFE5E7EB)),
    val valueLabelBackgroundDisabled: Brush = SolidColor(Color.Transparent),
    val valueLabelShape: Shape = RoundedCornerShape(4.dp),

    val trackBackground: Brush = SolidColor(Color(0xFFE5E7EB)),
    val trackShape: Shape = CircleShape,
    val trackFill: Brush = SolidColor(Color.Black),
    val trackFillDisabled: Brush = SolidColor(Color(0xFF969798)),
    val trackFillAlignment: SliderAlignment = SliderAlignment.Start,
    val trackThickness: Dp = 4.dp,
    val isTrackFullSize: Boolean = false,

    val thumbSize: DpSize = DpSize(24.dp, 24.dp),
    val thumbBackground: Brush = SolidColor(Color.White),
    val thumbBackgroundDisabled: Brush = SolidColor(Color(0xFFF6F6F6)),
    val thumbShape: Shape = CircleShape,
    val thumbShadow: OuterShadow? = OuterShadow(blur = 4.dp, color = Color.Black.copy(alpha = .15f), shape = thumbShape),
    val thumbShadowDisabled: OuterShadow? = null,

    val ticksZIndex: Float = 0f,
) {
    companion object {
        val DefaultLabelStyle = TextStyle.Default.copy(
            fontSize = 12.sp,
        )

        val DefaultValueLabelStyle = TextStyle.Default.copy(
            fontSize = 12.sp,
        )
    }
}

enum class MultiThumbMode {
    Range,
    Group,
}

/**
 * [SliderTick] represents a visual indicator for a particular value in a slider
 *
 * @param value The value the tick should be placed
 * @param label The label for the tick
 * @param style The style for the tick
 */
@Immutable
data class SliderTick(
    val value: Float,
    val label: String? = null,
    val style: SliderTickStyle = SliderTickStyle(),
)

/**
 * The style of a slider [SliderTick]
 */
@Immutable
data class SliderTickStyle(
    val shape: Shape? = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
    val shapeSize: DpAxisSize = DpAxisSize(1.dp, 7.dp),
    val shapeBrush: Brush = SolidColor(Color.Black),
    val shapeDrawStyle: DrawStyle = Fill,
    val shapePosition: SliderTickPosition = SliderTickPosition(offset = 4.dp),
    val labelTextStyle: TextStyle = TextStyle(fontSize = 12.sp),
    val labelTextDecoration: TextDecoration? = null,
    val labelDrawStyle: DrawStyle? = null,
    val labelShadow: Shadow? = null,
    val labelPosition: SliderTickPosition = SliderTickPosition(offset = 15.dp),
) {
    object Line {
        val Short = SliderTickStyle(shapeSize = DpAxisSize(mainAxis = 1.dp, crossAxis = 3.dp))
        val Medium = SliderTickStyle(shapeSize = DpAxisSize(mainAxis = 1.dp, crossAxis = 7.dp))
        val Tall = SliderTickStyle(shapeSize = DpAxisSize(mainAxis = 1.dp, crossAxis = 10.dp))
    }

    companion object Companion {
        val Circle = SliderTickStyle(
            shape = CircleShape,
            shapeSize = DpAxisSize(4.dp, 4.dp),
        )
    }
}

/**
 * @param mainAxis The size of the main-axis. Left-to-right for horizontal, top-to-bottom for vertical
 * @param crossAxis The size of the cross-axis. Top-to-bottom for horizontal, left-to-right for vertical
 */
@Immutable
data class DpAxisSize(
    val mainAxis: Dp,
    val crossAxis: Dp,
)


/**
 * @param offset The offset from the specified alignment
 * @param alignment The alignment of the tick
 *   * Start: The cross-axis end of the tick will align with the cross-axis start of the track.
 *   * Center: The cross-axis center of the tick will align with the cross-axis center of the track.
 *   * End: The cross-axis start of the tick will align with the cross-axis end of the track.
 */
@Immutable
data class SliderTickPosition(
    val offset: Dp = 0.dp,
    val alignment: SliderAlignment = SliderAlignment.End,
)

@Immutable
enum class SliderAlignment {
    Start,
    Center,
    End,
}

@Immutable
enum class KmpSliderDirection {
    Vertical,
    Horizontal,
    ;

    val isHorizontal: Boolean
        get() = this == Horizontal
}

@Preview
@Composable
private fun KmpSliderPreview() {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .background(Color.White)
            .systemBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        val testValues = remember { mutableStateOf(mapOf("0" to -20f, "1" to 0f, "2" to 20f)) }
        var isDisabled by remember { mutableStateOf(false) }

        Row(
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Disabled")
            Checkbox(checked = isDisabled, onCheckedChange = { isDisabled = it })
        }

        KmpSlider(
            label = "Group",
            isEnabled = !isDisabled,
            multiThumbMode = MultiThumbMode.Group,
            values = testValues.value,
            range = -100f..100f,
            styles = KmpSliderStyles().copy(),
            ticks = remember {
                (-100..100 step 10).map {
                    SliderTick(
                        value = it.toFloat(),
                        label = if (it % 20 == 0) it.toString() else null,
                        style = if (it % 20 == 0) SliderTickStyle.Line.Medium else SliderTickStyle.Line.Short,
                    )
                }
            },
            onChange = {
                testValues.value = testValues.value.toMutableMap().apply {
                    it.forEach { (key, value) -> this[key] = value }
                }
            },
        )

        KmpSlider(
            values = remember { derivedStateOf { mapOf("0" to (testValues.value["0"] ?: 0f)) } }.value,
            label = "Start Alignment",
            isEnabled = !isDisabled,
            units = "%",
            range = -100f..100f,
            styles = KmpSliderStyles().copy(),
            ticks = remember {
                (-100..100 step 20).map {
                    SliderTick(
                        value = it.toFloat(),
                        label = it.toString(),
                    )
                }
            },
            onChange = {
                testValues.value = testValues.value.toMutableMap().apply {
                    it.forEach { (key, value) -> this[key] = value }
                }
            },
        )

        KmpSlider(
            values = remember { derivedStateOf { mapOf("1" to (testValues.value["1"] ?: 0f)) } }.value,
            label = "Center Alignment",
            isEnabled = !isDisabled,
            units = "%",
            range = -100f..100f,
            styles = KmpSliderStyles().copy(
                trackFill = Brush.linearGradient(
                    0f to Color.Blue,
                    1f to Color.Red,
                ),
                trackFillAlignment = SliderAlignment.Center,
            ),
            ticks = remember {
                (-100..100 step 20).map {
                    SliderTick(
                        value = it.toFloat(),
                        label = it.toString(),
                        style = SliderTickStyle.Line.Medium,
                    )
                }
            },
            onChange = {
                testValues.value = testValues.value.toMutableMap().apply {
                    it.forEach { (key, value) -> this[key] = value }
                }
            },
        )

        KmpSlider(
            values = remember { derivedStateOf { mapOf("2" to (testValues.value["2"] ?: 0f)) } }.value,
            label = "End Alignment",
            isEnabled = !isDisabled,
            units = "%",
            range = -100f..100f,
            styles = KmpSliderStyles().copy(
                trackFill = Brush.linearGradient(
                    0f to Color.Blue,
                    1f to Color.LightGray,
                ),
                trackFillAlignment = SliderAlignment.End,
            ),
            ticks = remember {
                (-100..100 step 20).map {
                    SliderTick(
                        value = it.toFloat(),
                        label = it.toString(),
                        style = SliderTickStyle.Line.Medium,
                    )
                }
            },
            onChange = {
                testValues.value = testValues.value.toMutableMap().apply {
                    it.forEach { (key, value) -> this[key] = value }
                }
            },
        )

        KmpSlider(
            values = remember { derivedStateOf { mapOf("0" to (testValues.value["0"] ?: 0f), "2" to (testValues.value["2"] ?: 0f)) } }.value,
            label = "Range",
            isEnabled = !isDisabled,
            range = -100f..100f,
            styles = KmpSliderStyles().copy(),
            deadband = 1f,
            ticks = remember {
                buildList {
                    SliderTick(
                        value = -100f,
                        label = "0",
                        style = SliderTickStyle(
                            labelPosition = SliderTickPosition(alignment = SliderAlignment.End, offset = 4.dp),
                            shapeSize = DpAxisSize(0.dp, 0.dp),
                        ),
                    ).let { add(it) }
                    SliderTick(
                        value = 100f,
                        label = "100",
                        style = SliderTickStyle(
                            labelPosition = SliderTickPosition(alignment = SliderAlignment.End, offset = 4.dp),
                            shapeSize = DpAxisSize(0.dp, 0.dp),
                        ),
                    ).let { add(it) }
                    for (i in -90..90 step 10) {
                        SliderTick(
                            value = i.toFloat(),
                            style = SliderTickStyle.Circle.copy(
                                shapeBrush = SolidColor(Color.White.copy(alpha = .75f)),
                                shapePosition = SliderTickPosition(alignment = SliderAlignment.Center),
                            ),
                        ).let { add(it) }
                    }
                }
            },
            onChange = {
                testValues.value = testValues.value.toMutableMap().apply {
                    it.forEach { (key, value) -> this[key] = value }
                }
            },
        )

        val logValues = remember { mutableStateOf(mapOf("0" to 50f)) }
        KmpSlider(
            values = logValues.value,
            isEnabled = !isDisabled,
            label = "Logarithmic",
            range = 20f..20_000f,
            valueFormatter = remember {
                val numberFormatter = KmpNumberFormatter(maximumFractionDigits = 0)
                return@remember { numberFormatter.format(it) }
            },
            units = "hz",
            logarithmic = true,
            styles = KmpSliderStyles().copy(),
            ticks = remember {
                buildList {
                    add(SliderTick(value = 20f, label = "20hz"))
                    add(SliderTick(value = 50f, label = "50"))
                    add(SliderTick(value = 100f, label = "100"))
                    add(SliderTick(value = 200f, label = "200"))
                    add(SliderTick(value = 500f, label = "500"))
                    add(SliderTick(value = 1_000f, label = "1k"))
                    add(SliderTick(value = 2_000f, label = "2k"))
                    add(SliderTick(value = 5_000f, label = "5k"))
                    add(SliderTick(value = 10_000f, label = "10k"))
                    add(SliderTick(value = 20_000f, label = "20k"))
                }
            },
            onChange = remember {
                {
                    logValues.value = logValues.value.toMutableMap().apply {
                        it.forEach { (key, value) -> this[key] = value }
                    }
                }
            },
        )

        KmpSlider(
            modifier = Modifier.height(300.dp),
            values = testValues.value,
            isEnabled = !isDisabled,
            direction = KmpSliderDirection.Vertical,
            multiThumbMode = MultiThumbMode.Group,
            label = "Basic".uppercase(),
            units = "%",
            range = -100f..100f,
            styles = KmpSliderStyles().copy(),
            ticks = remember {
                (-100..100 step 20).map {
                    SliderTick(
                        value = it.toFloat(),
                        label = it.toString(),
                    )
                }
            },
            onChange = {
                testValues.value = testValues.value.toMutableMap().apply {
                    it.forEach { (key, value) -> this[key] = value }
                }
            },
        )
    }
}