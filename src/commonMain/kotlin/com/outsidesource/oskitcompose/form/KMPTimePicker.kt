package com.outsidesource.oskitcompose.form

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.outsidesource.oskitcompose.popup.ModalStyles
import com.outsidesource.oskitkmp.lib.snapTo
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone.Companion.currentSystemDefault
import kotlinx.datetime.toLocalDateTime

@Composable
fun KMPTimePickerModal(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    onDismissRequest: (() -> Unit)? = null,
    isFullScreen: Boolean = true,
    modalStyles: ModalStyles = remember { ModalStyles() },
    datePickerStyles: KMPTimePickerStyles = rememberKmpTimePickerStyles(),
    dismissOnBackPress: Boolean = true,
    dismissOnExternalClick: Boolean = true,
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    time: LocalTime = Clock.System.now().toLocalDateTime(currentSystemDefault()).time,
    onChange: (date: LocalDate) -> Unit,
) {
//    val selectedDate = remember(date) { mutableStateOf(date) }
//
//    Modal(
//        modifier = modifier,
//        isVisible = isVisible,
//        dismissOnExternalClick = dismissOnExternalClick,
//        dismissOnBackPress = dismissOnBackPress,
//        onDismissRequest = onDismissRequest,
//        isFullScreen = isFullScreen,
//        styles = modalStyles,
//        onPreviewKeyEvent = onPreviewKeyEvent,
//        onKeyEvent = onKeyEvent,
//    ) {
//        Column(
//            modifier = Modifier.background(datePickerStyles.backgroundColor)
//        ) {
//            KMPDatePickerInline(
//                date = date,
//                minDate = minDate,
//                maxDate = maxDate,
//                styles = datePickerStyles,
//                onChange = { selectedDate.value = it },
//            )
//
//            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
//                TextButton({
//                    onDismissRequest?.invoke()
//                }) {
//                    Text("CANCEL", style = datePickerStyles.buttonStyle)
//                }
//                TextButton({
//                    onDismissRequest?.invoke()
//                    onChange(selectedDate.value)
//                }) {
//                    Text("OK", style = datePickerStyles.buttonStyle)
//                }
//            }
//        }
//    }
}

//@Composable
//fun KMPDatePickerPopover(
//    isVisible: Boolean,
//    styles: KMPDatePickerStyles = rememberKmpDatePickerStyles(),
//    modifier: Modifier = Modifier
//        .shadow(16.dp, RoundedCornerShape(8.dp))
//        .background(color = styles.backgroundColor)
//        .padding(16.dp),
//    onDismissRequest: (() -> Unit)? = null,
//    anchors: PopoverAnchors = PopoverAnchors.ExternalBottomAlignCenter,
//    popupPositionProvider: PopupPositionProvider? = null,
//    dismissOnBackPress: Boolean = true,
//    onKeyEvent: (KeyEvent) -> Boolean = { false },
//    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
//    offset: DpOffset = DpOffset(0.dp, (-16f).dp),
//    time: LocalTime = Clock.System.now().toLocalDateTime(currentSystemDefault()).time,
//    onChange: (date: LocalDate) -> Unit,
//) {
//    val selectedDate = remember(date) { mutableStateOf(date) }
//
//    Popover(
//        isVisible = isVisible,
//        anchors = anchors,
//        onDismissRequest = onDismissRequest,
//        dismissOnBackKey = dismissOnBackPress,
//        onKeyEvent = onKeyEvent,
//        onPreviewKeyEvent = onPreviewKeyEvent,
//        popupPositionProvider = popupPositionProvider,
//        offset = offset,
//    ) {
//        Column(
//            modifier = modifier
//        ) {
//            KMPDatePickerInline(
//                date = date,
//                minDate = minDate,
//                maxDate = maxDate,
//                styles = styles,
//                onChange = { selectedDate.value = it },
//            )
//
//            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
//                TextButton({
//                    onDismissRequest?.invoke()
//                }) {
//                    Text("CANCEL", style = styles.buttonStyle)
//                }
//                TextButton({
//                    onDismissRequest?.invoke()
//                    onChange(selectedDate.value)
//                }) {
//                    Text("OK", style = styles.buttonStyle)
//                }
//            }
//        }
//    }
//}

@Composable
fun KMPTimePickerInline(
    modifier: Modifier = Modifier,
    time: LocalTime = Clock.System.now().toLocalDateTime(currentSystemDefault()).time,
    minuteStep: Int = 1,
    styles: KMPTimePickerStyles = rememberKmpTimePickerStyles(),
    onChange: (date: LocalTime) -> Unit,
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

    val selectedMeridian = remember(time) {
        if (time.hour >= 12) TimeMeridian.PM.ordinal else TimeMeridian.AM.ordinal
    }

    CompositionLocalProvider(LocalKMPTimePickerStyles provides styles) {
        val state = rememberKmpWheelPickerState(isInfinite = true, initiallySelectedItemIndex = selectedHourIndex)
        val pickerIndicator = remember { KMPWheelPickerIndicators.window() }

        Row(
            modifier = Modifier
                .widthIn(min = TIME_PICKER_MIN_WIDTH)
                .drawWithContent { pickerIndicator(this, state) }
                .padding(horizontal = pickerHPadding / 2)
                .then(modifier),
            horizontalArrangement = Arrangement.Center,
        ) {
            KMPWheelPicker(
                modifier = Modifier
                    .height(pickerSize)
                    .padding(horizontal = pickerHPadding / 2),
                selectedIndex = selectedHourIndex,
                items = remember { (1..12).toList() },
                state = state,
                horizontalAlignment = Alignment.End,
                indicator = KMPWheelPickerIndicators.none,
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

            KMPWheelPicker(
                modifier = Modifier
                    .height(pickerSize)
                    .padding(horizontal = pickerHPadding / 2),
                selectedIndex = selectedMinuteIndex,
                state = rememberKmpWheelPickerState(isInfinite = true, initiallySelectedItemIndex = selectedMinuteIndex),
                items = remember(minuteStep) { (0..59 step minuteStep).toList() },
                horizontalAlignment = Alignment.CenterHorizontally,
                indicator = KMPWheelPickerIndicators.none,
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

            KMPWheelPicker(
                modifier = Modifier
                    .height(pickerSize)
                    .padding(horizontal = pickerHPadding / 2),
                selectedIndex = selectedMeridian,
                state = rememberKmpWheelPickerState(isInfinite = false, initiallySelectedItemIndex = selectedMeridian),
                items = remember(minuteStep) { TimeMeridian.entries },
                horizontalAlignment = Alignment.Start,
                indicator = KMPWheelPickerIndicators.none,
                onChange = { meridian ->
                    val currentHour = selectedTime.value.hour
                    val newHour = when (meridian) {
                        TimeMeridian.AM -> if (currentHour >= 12) currentHour - 12 else currentHour
                        TimeMeridian.PM -> if (currentHour < 12) currentHour + 12 else currentHour
                    }
                    val newTime = LocalTime(newHour, selectedTime.value.minute)
                    onChange(newTime)
                }
            ) { meridian ->
                val isSelectedMeridian = when (meridian) {
                    TimeMeridian.AM -> selectedTime.value.hour < 12
                    TimeMeridian.PM -> selectedTime.value.hour >= 12
                }

                Box(
                    modifier = Modifier
                        .height(optionSize)
                        .width(32.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = meridian.toString(),
                        style = TextStyle(
                            color = if (isSelectedMeridian) styles.accentColor else styles.fontColor,
                            fontSize = 18.sp,
                        ),
                    )
                }
            }
        }
    }
}

@Immutable
data class KMPTimePickerStyles(
    val accentColor: Color,
    val fontColor: Color,
    val backgroundColor: Color,
    val fontColorOnAccent: Color,
    val buttonStyle: TextStyle,
)

@Composable
fun rememberKmpTimePickerStyles(): KMPTimePickerStyles {
    val colors = MaterialTheme.colors
    val typography = MaterialTheme.typography

    return remember {
        KMPTimePickerStyles(
            accentColor = colors.primary,
            fontColor = typography.body1.color,
            fontColorOnAccent = colors.onPrimary,
            backgroundColor = Color.White,
            buttonStyle = typography.button,
        )
    }
}

val LocalKMPTimePickerStyles = staticCompositionLocalOf {
    KMPTimePickerStyles(
        accentColor = Color.Black,
        fontColor = Color.Black,
        backgroundColor = Color.White,
        fontColorOnAccent = Color.Black,
        buttonStyle = TextStyle(),
    )
}

enum class TimeMeridian {
    AM,
    PM,
}

private val optionSize = 40.dp
private val pickerSize = 200.dp
private val pickerHPadding = 32.dp
val TIME_PICKER_MIN_WIDTH = 220.dp