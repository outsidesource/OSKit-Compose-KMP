package com.outsidesource.oskitcompose.form

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.outsidesource.oskitcompose.modifier.defaultMaxSize
import com.outsidesource.oskitcompose.popup.*
import com.outsidesource.oskitkmp.lib.snapTo
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone.Companion.currentSystemDefault
import kotlinx.datetime.toLocalDateTime

@Composable
fun KmpTimePickerModal(
    isVisible: Boolean,
    isEnabled: Boolean = true,
    modifier: Modifier = Modifier,
    onDismissRequest: (() -> Unit)? = null,
    isFullScreen: Boolean = true,
    modalStyles: ModalStyles = remember { ModalStyles() },
    timePickerStyles: KmpTimePickerStyles = rememberKmpTimePickerStyles(),
    dismissOnBackPress: Boolean = true,
    dismissOnExternalClick: Boolean = true,
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    time: LocalTime = Clock.System.now().toLocalDateTime(currentSystemDefault()).time,
    minuteStep: Int = 1,
    onChange: (time: LocalTime) -> Unit,
) {
    val selectedTime = remember(time) { mutableStateOf(time) }

    Modal(
        modifier = modifier.defaultMaxSize(maxWidth = TIME_PICKER_MIN_WIDTH),
        isVisible = isVisible,
        dismissOnExternalClick = dismissOnExternalClick,
        dismissOnBackPress = dismissOnBackPress,
        onDismissRequest = onDismissRequest,
        isFullScreen = isFullScreen,
        styles = modalStyles,
        onPreviewKeyEvent = onPreviewKeyEvent,
        onKeyEvent = onKeyEvent,
    ) {
        Column(
            modifier = Modifier
                .background(timePickerStyles.backgroundColor),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            KmpTimePickerInline(
                modifier = Modifier.fillMaxWidth(),
                isEnabled = isEnabled,
                time = time,
                minuteStep = minuteStep,
                styles = timePickerStyles,
                onChange = { selectedTime.value = it },
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton({
                    onDismissRequest?.invoke()
                }) {
                    Text("CANCEL", style = timePickerStyles.buttonStyle)
                }
                TextButton({
                    onDismissRequest?.invoke()
                    onChange(selectedTime.value)
                }) {
                    Text("OK", style = timePickerStyles.buttonStyle)
                }
            }
        }
    }
}

@Composable
fun KmpTimePickerPopover(
    isVisible: Boolean,
    isEnabled: Boolean = true,
    timePickerStyles: KmpTimePickerStyles = rememberKmpTimePickerStyles(),
    modifier: Modifier = Modifier
        .shadow(16.dp, RoundedCornerShape(8.dp))
        .background(timePickerStyles.backgroundColor)
        .padding(16.dp)
        .defaultMaxSize(maxWidth = TIME_PICKER_MIN_WIDTH),
    onDismissRequest: (() -> Unit)? = null,
    anchors: PopoverAnchors = PopoverAnchors.ExternalBottomAlignCenter,
    popupPositionProvider: PopupPositionProvider? = null,
    dismissOnBackPress: Boolean = true,
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    offset: DpOffset = DpOffset(0.dp, (-16f).dp),
    time: LocalTime = Clock.System.now().toLocalDateTime(currentSystemDefault()).time,
    minuteStep: Int = 1,
    onChange: (time: LocalTime) -> Unit,
) {
    val selectedTime = remember(time) { mutableStateOf(time) }

    Popover(
        isVisible = isVisible,
        anchors = anchors,
        onDismissRequest = onDismissRequest,
        dismissOnBackKey = dismissOnBackPress,
        onKeyEvent = onKeyEvent,
        onPreviewKeyEvent = onPreviewKeyEvent,
        popupPositionProvider = popupPositionProvider,
        offset = offset,
    ) {
        Column(
            modifier = Modifier.background(timePickerStyles.backgroundColor).then(modifier)
        ) {
            KmpTimePickerInline(
                modifier = Modifier.fillMaxWidth(),
                isEnabled = isEnabled,
                time = time,
                minuteStep = minuteStep,
                styles = timePickerStyles,
                onChange = { selectedTime.value = it },
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton({
                    onDismissRequest?.invoke()
                }) {
                    Text("CANCEL", style = timePickerStyles.buttonStyle)
                }
                TextButton({
                    onDismissRequest?.invoke()
                    onChange(selectedTime.value)
                }) {
                    Text("OK", style = timePickerStyles.buttonStyle)
                }
            }
        }
    }
}

@Composable
fun KmpTimePickerInline(
    modifier: Modifier = Modifier,
    time: LocalTime = Clock.System.now().toLocalDateTime(currentSystemDefault()).time,
    minuteStep: Int = 1,
    styles: KmpTimePickerStyles = rememberKmpTimePickerStyles(),
    isEnabled: Boolean = true,
    onChange: (time: LocalTime) -> Unit,
) {
    val selectedTime = remember(time) { mutableStateOf(time) }

    val selectedHourIndex = remember(time) {
        if (time.hour == 0) return@remember 11
        if (time.hour == 12) return@remember 11
        (time.hour % 12) - 1
    }

    val selectedMinuteIndex = remember(time, minuteStep) {
        time.minute.snapTo(minuteStep)
    }

    val selectedMeridiem = remember(time) {
        if (time.hour >= 12) TimeMeridiem.PM.ordinal else TimeMeridiem.AM.ordinal
    }

    CompositionLocalProvider(LocalKmpTimePickerStyles provides styles) {
        val state = rememberKmpWheelPickerState(isInfinite = true, initiallySelectedItemIndex = selectedHourIndex)
        val pickerIndicator = remember { KmpWheelPickerIndicators.window() }

        Row(
            modifier = Modifier
                .widthIn(min = TIME_PICKER_MIN_WIDTH)
                .drawWithContent { pickerIndicator(this, state) }
                .padding(horizontal = pickerHPadding / 2)
                .then(modifier)
                .defaultMaxSize(maxWidth = TIME_PICKER_MIN_WIDTH),
            horizontalArrangement = Arrangement.Center,
        ) {
            KmpWheelPicker(
                modifier = Modifier
                    .height(pickerSize)
                    .padding(horizontal = pickerHPadding / 2),
                isEnabled = isEnabled,
                selectedIndex = selectedHourIndex,
                items = remember { (1..12).toList() },
                state = state,
                horizontalAlignment = Alignment.End,
                indicator = KmpWheelPickerIndicators.none,
                onChange = { hour ->
                    val currentHour = selectedTime.value.hour
                    val newHour = if (currentHour >= 12) {
                        if (hour == 12) 12 else 12 + hour
                    } else {
                        hour
                    }

                    val newTime = LocalTime(newHour, selectedTime.value.minute)
                    selectedTime.value = newTime
                    onChange(newTime)
                }
            ) { hour ->
                val isSelectedHour = when (hour) {
                    12 -> selectedTime.value.hour == 0 || selectedTime.value.hour == 12
                    else -> hour == selectedTime.value.hour % 12
                }

                Box(
                    modifier = Modifier
                        .height(optionSize)
                        .width(24.dp),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Text(
                        text = hour.toString(),
                        style = TextStyle(
                            color = if (isSelectedHour) styles.accentColor else styles.fontColor,
                            fontSize = 18.sp,
                        ),
                    )
                }
            }

            KmpWheelPicker(
                modifier = Modifier
                    .height(pickerSize)
                    .padding(horizontal = pickerHPadding / 2),
                isEnabled = isEnabled,
                selectedIndex = selectedMinuteIndex,
                state = rememberKmpWheelPickerState(isInfinite = true, initiallySelectedItemIndex = selectedMinuteIndex),
                items = remember(minuteStep) { (0..59 step minuteStep).toList() },
                horizontalAlignment = Alignment.CenterHorizontally,
                indicator = KmpWheelPickerIndicators.none,
                onChange = { minute ->
                    selectedTime.value = LocalTime(selectedTime.value.hour, minute)
                    onChange(selectedTime.value)
                }
            ) { minute ->
                val isSelectedMinute = minute == selectedTime.value.minute

                Box(
                    modifier = Modifier
                        .height(optionSize)
                        .width(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = minute.toString().padStart(2, '0'),
                        style = TextStyle(
                            color = if (isSelectedMinute) styles.accentColor else styles.fontColor,
                            fontSize = 18.sp,
                        ),
                    )
                }
            }

            KmpWheelPicker(
                modifier = Modifier
                    .height(pickerSize)
                    .padding(horizontal = pickerHPadding / 2),
                isEnabled = isEnabled,
                selectedIndex = selectedMeridiem,
                state = rememberKmpWheelPickerState(isInfinite = false, initiallySelectedItemIndex = selectedMeridiem),
                items = remember(minuteStep) { TimeMeridiem.entries },
                horizontalAlignment = Alignment.Start,
                indicator = KmpWheelPickerIndicators.none,
                onChange = { meridiem ->
                    val currentHour = selectedTime.value.hour
                    val newHour = when (meridiem) {
                        TimeMeridiem.AM -> if (currentHour >= 12) currentHour - 12 else currentHour
                        TimeMeridiem.PM -> if (currentHour < 12) currentHour + 12 else currentHour
                    }
                    selectedTime.value = LocalTime(newHour, selectedTime.value.minute)
                    onChange(selectedTime.value)
                }
            ) { meridiem ->
                val isSelectedMeridiem = when (meridiem) {
                    TimeMeridiem.AM -> selectedTime.value.hour < 12
                    TimeMeridiem.PM -> selectedTime.value.hour >= 12
                }

                Box(
                    modifier = Modifier
                        .height(optionSize)
                        .width(32.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = meridiem.toString(),
                        style = TextStyle(
                            color = if (isSelectedMeridiem) styles.accentColor else styles.fontColor,
                            fontSize = 18.sp,
                        ),
                    )
                }
            }
        }
    }
}

@Immutable
data class KmpTimePickerStyles(
    val accentColor: Color,
    val fontColor: Color,
    val backgroundColor: Color,
    val fontColorOnAccent: Color,
    val buttonStyle: TextStyle,
)

@Composable
fun rememberKmpTimePickerStyles(): KmpTimePickerStyles {
    val colors = MaterialTheme.colors
    val typography = MaterialTheme.typography

    return remember {
        KmpTimePickerStyles(
            accentColor = colors.primary,
            fontColor = typography.body1.color,
            fontColorOnAccent = colors.onPrimary,
            backgroundColor = Color.White,
            buttonStyle = typography.button,
        )
    }
}

val LocalKmpTimePickerStyles = staticCompositionLocalOf {
    KmpTimePickerStyles(
        accentColor = Color.Black,
        fontColor = Color.Black,
        backgroundColor = Color.White,
        fontColorOnAccent = Color.Black,
        buttonStyle = TextStyle(),
    )
}

enum class TimeMeridiem {
    AM,
    PM,
}

private val optionSize = 40.dp
private val pickerSize = 200.dp
private val pickerHPadding = 32.dp
val TIME_PICKER_MIN_WIDTH = 220.dp