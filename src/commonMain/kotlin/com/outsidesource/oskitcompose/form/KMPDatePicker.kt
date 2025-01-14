package com.outsidesource.oskitcompose.form

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.outsidesource.oskitcompose.date.DateTextFormat
import com.outsidesource.oskitcompose.date.getDisplayName
import com.outsidesource.oskitcompose.date.lengthInDays
import com.outsidesource.oskitcompose.modifier.defaultMaxSize
import com.outsidesource.oskitcompose.popup.*
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone.Companion.currentSystemDefault
import kotlin.math.min

private val daySize = 40.dp
val DATE_PICKER_MIN_WIDTH = daySize * 7

private enum class DatePickerViewType {
    Month,
    Year,
}

@Composable
fun KmpDatePickerModal(
    isVisible: Boolean,
    isEnabled: Boolean = true,
    modifier: Modifier = Modifier,
    onDismissRequest: (() -> Unit)? = null,
    isFullScreen: Boolean = true,
    modalStyles: ModalStyles = remember { ModalStyles() },
    datePickerStyles: KmpDatePickerStyles = rememberKmpDatePickerStyles(),
    dismissOnBackPress: Boolean = true,
    dismissOnExternalClick: Boolean = true,
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    date: LocalDate = Clock.System.now().toLocalDateTime(currentSystemDefault()).date,
    minDate: LocalDate = remember { LocalDate(0, Month.JANUARY, 1) },
    maxDate: LocalDate = remember { LocalDate(3000, Month.DECEMBER, 31) },
    onChange: (date: LocalDate) -> Unit,
) {
    val selectedDate = remember(date) { mutableStateOf(date) }

    Modal(
        modifier = modifier.defaultMaxSize(maxWidth = DATE_PICKER_MIN_WIDTH),
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
                .background(datePickerStyles.backgroundColor),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            KmpDatePickerInline(
                modifier = Modifier.fillMaxWidth(),
                isEnabled = isEnabled,
                date = date,
                minDate = minDate,
                maxDate = maxDate,
                styles = datePickerStyles,
                onChange = { selectedDate.value = it },
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton({
                    onDismissRequest?.invoke()
                }) {
                    Text("CANCEL", style = datePickerStyles.buttonStyle)
                }
                TextButton({
                    onDismissRequest?.invoke()
                    onChange(selectedDate.value)
                }) {
                    Text("OK", style = datePickerStyles.buttonStyle)
                }
            }
        }
    }
}

@Composable
fun KmpDatePickerPopover(
    isVisible: Boolean,
    isEnabled: Boolean = true,
    datePickerStyles: KmpDatePickerStyles = rememberKmpDatePickerStyles(),
    modifier: Modifier = Modifier
        .shadow(16.dp, RoundedCornerShape(8.dp))
        .background(datePickerStyles.backgroundColor)
        .padding(16.dp)
        .defaultMaxSize(maxWidth = DATE_PICKER_MIN_WIDTH),
    onDismissRequest: (() -> Unit)? = null,
    anchors: PopoverAnchors = PopoverAnchors.ExternalBottomAlignCenter,
    popupPositionProvider: PopupPositionProvider? = null,
    dismissOnBackPress: Boolean = true,
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    offset: DpOffset = DpOffset(0.dp, (-16f).dp),
    date: LocalDate = Clock.System.now().toLocalDateTime(currentSystemDefault()).date,
    minDate: LocalDate = remember { LocalDate(0, Month.JANUARY, 1) },
    maxDate: LocalDate = remember { LocalDate(3000, Month.DECEMBER, 31) },
    onChange: (date: LocalDate) -> Unit,
) {
    val selectedDate = remember(date) { mutableStateOf(date) }

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
            modifier = Modifier.background(datePickerStyles.backgroundColor).then(modifier)
        ) {
            KmpDatePickerInline(
                modifier = Modifier.fillMaxWidth(),
                isEnabled = isEnabled,
                date = date,
                minDate = minDate,
                maxDate = maxDate,
                styles = datePickerStyles,
                onChange = { selectedDate.value = it },
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton({
                    onDismissRequest?.invoke()
                }) {
                    Text("CANCEL", style = datePickerStyles.buttonStyle)
                }
                TextButton({
                    onDismissRequest?.invoke()
                    onChange(selectedDate.value)
                }) {
                    Text("OK", style = datePickerStyles.buttonStyle)
                }
            }
        }
    }
}

@Composable
fun KmpDatePickerInline(
    modifier: Modifier = Modifier,
    date: LocalDate = Clock.System.now().toLocalDateTime(currentSystemDefault()).date,
    minDate: LocalDate = remember { LocalDate(0, Month.JANUARY, 1) },
    maxDate: LocalDate = remember { LocalDate(3000, Month.DECEMBER, 31) },
    styles: KmpDatePickerStyles = rememberKmpDatePickerStyles(),
    isEnabled: Boolean = true,
    onChange: (date: LocalDate) -> Unit,
) {
    val viewType = remember { mutableStateOf(DatePickerViewType.Month) }
    val viewDate = remember(date) { mutableStateOf(date) }
    val selectedDate = remember(date) { mutableStateOf(date) }

    CompositionLocalProvider(LocalKmpDatePickerStyles provides styles) {
        Column(
            modifier = Modifier
                .widthIn(min = DATE_PICKER_MIN_WIDTH)
                .then(modifier)
                .defaultMaxSize(maxWidth = DATE_PICKER_MIN_WIDTH)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable {
                            viewType.value = when (viewType.value) {
                                DatePickerViewType.Month -> DatePickerViewType.Year
                                else -> DatePickerViewType.Month
                            }
                        }
                        .padding(horizontal = 4.dp)
                        .height(40.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "${viewDate.value.month.getDisplayName(DateTextFormat.Full)} ${viewDate.value.year}",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = styles.fontColor,
                        )
                    )

                    val rotation by animateFloatAsState(
                        if (viewType.value == DatePickerViewType.Year) 90f else 0f,
                        tween()
                    )

                    Image(
                        modifier = Modifier
                            .size(18.dp)
                            .graphicsLayer { rotationZ = rotation },
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        colorFilter = ColorFilter.tint(styles.accentColor),
                        contentDescription = "Change View",
                    )
                }

                AnimatedVisibility(
                    visible = viewType.value == DatePickerViewType.Month,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    viewDate.value =
                                        (viewDate.value - DatePeriod(months = 1)).coerceIn(minDate, maxDate)
                                }
                                .size(daySize),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                tint = styles.accentColor,
                                contentDescription = "Previous month"
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    viewDate.value =
                                        (viewDate.value + DatePeriod(months = 1)).coerceIn(minDate, maxDate)
                                }
                                .size(daySize),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                tint = styles.accentColor,
                                contentDescription = "Next month"
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.padding(top = 4.dp)) {
                DatePickerMonthView(isEnabled, viewType.value, viewDate, selectedDate, minDate, maxDate, onChange)
                DatePickerYearView(isEnabled, viewType, viewDate, selectedDate, minDate, maxDate, onChange)
            }
        }
    }
}

@Composable
private fun DatePickerMonthView(
    isEnabled: Boolean,
    viewType: DatePickerViewType,
    viewDate: MutableState<LocalDate>,
    selectedDate: MutableState<LocalDate>,
    minDate: LocalDate,
    maxDate: LocalDate,
    onChange: (date: LocalDate) -> Unit,
) {
    AnimatedVisibility(viewType == DatePickerViewType.Month, enter = fadeIn(), exit = fadeOut()) {
        Column {
            Row(
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                DayName("SUN")
                DayName("MON")
                DayName("TUE")
                DayName("WED")
                DayName("THU")
                DayName("FRI")
                DayName("SAT")
            }

            AnimatedContent(
                targetState = viewDate.value,
                contentKey = { it.month },
                transitionSpec = {
                    slideInHorizontally {
                        if (initialState.month == Month.JANUARY && targetState.month == Month.DECEMBER) return@slideInHorizontally -it
                        if (targetState.month > initialState.month || (initialState.month == Month.DECEMBER && targetState.month == Month.JANUARY)) it else -it
                    } togetherWith slideOutHorizontally {
                        if (initialState.month == Month.JANUARY && targetState.month == Month.DECEMBER) return@slideOutHorizontally it
                        if (targetState.month > initialState.month || (initialState.month == Month.DECEMBER && targetState.month == Month.JANUARY)) -it else it
                    }
                },
            ) {currentDateValue ->
                val dayOne = currentDateValue - DatePeriod(days = currentDateValue.dayOfMonth - 1)
                val startIndex = dayOne.dayOfWeek.sundayFirstOrdinal() + 1
                val maxIndex = (dayOne.month.lengthInDays(dayOne.year) + startIndex)

                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.height(daySize * 6)
                ) {
                    for (i in 0 until 6) {
                        if (startIndex == 7 && i == 0) continue

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            var hasOneInRow = false

                            for (j in 0..< 7) {
                                val index = (i * 7) + j

                                if (index < startIndex) {
                                    DatePickerDay("")
                                } else if (index < maxIndex) {
                                    hasOneInRow = true
                                    val day = index - startIndex + 1
                                    val isDayEnabled = run {
                                        if (currentDateValue.year < minDate.year) return@run false
                                        if (currentDateValue.year == minDate.year && currentDateValue.month < minDate.month) return@run false
                                        if (currentDateValue.year == minDate.year && currentDateValue.month == minDate.month && day < minDate.dayOfMonth) return@run false
                                        if (currentDateValue.year > maxDate.year) return@run false
                                        if (currentDateValue.year == maxDate.year && currentDateValue.month > maxDate.month) return@run false
                                        if (currentDateValue.year == maxDate.year && currentDateValue.month == maxDate.month && day > maxDate.dayOfMonth) return@run false
                                        true
                                    }

                                    DatePickerDay(
                                        label = day.toString(),
                                        isEnabled = isEnabled && isDayEnabled,
                                        isSelected = currentDateValue.year == selectedDate.value.year &&
                                                currentDateValue.month == selectedDate.value.month &&
                                                selectedDate.value.dayOfMonth == day,
                                        onClick = {
                                            selectedDate.value =
                                                LocalDate(currentDateValue.year, currentDateValue.month, day)
                                            onChange(selectedDate.value)
                                        }
                                    )
                                } else if (hasOneInRow) {
                                    DatePickerDay("")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayName(text: String) {
    val styles = LocalKmpDatePickerStyles.current
    val dayNameTextStyle = remember {
        TextStyle(color = styles.fontColor.copy(alpha = .5f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
    Text(modifier = Modifier.width(daySize), text = text, style = dayNameTextStyle, textAlign = TextAlign.Center)
}

@Composable
private fun DatePickerYearView(
    isEnabled: Boolean,
    viewType: MutableState<DatePickerViewType>,
    viewDate: MutableState<LocalDate>,
    selectedDate: MutableState<LocalDate>,
    minDate: LocalDate,
    maxDate: LocalDate,
    onChange: (date: LocalDate) -> Unit,
) {
    val styles = LocalKmpDatePickerStyles.current

    AnimatedVisibility(viewType.value == DatePickerViewType.Year, enter = fadeIn(), exit = fadeOut()) {
        val pickerHPadding = 40.dp

        Row(
            modifier = Modifier
                .height(daySize * 6 + 20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val monthItems = remember(minDate, maxDate, viewDate.value.year) {
                if (viewDate.value.year > minDate.year && viewDate.value.year < maxDate.year) {
                    return@remember Month.entries
                }

                Month.entries.mapNotNull {
                    if (viewDate.value.year == minDate.year && it < minDate.month) return@mapNotNull null
                    if (viewDate.value.year == maxDate.year && it > maxDate.month) return@mapNotNull null
                    it
                }
            }

            val selectedMonth = remember(viewDate.value, monthItems) {
                monthItems.indexOfFirst { it == viewDate.value.month }.coerceAtLeast(0)
            }

            val yearItems = remember(minDate, maxDate) {
                (minDate.year..maxDate.year).toList()
            }

            val selectedYear = remember(viewDate.value, yearItems, minDate) {
                viewDate.value.year - minDate.year
            }

            KmpWheelPicker(
                modifier = Modifier
                    .height(daySize * 5)
                    .weight(1f),
                isEnabled = isEnabled,
                selectedIndex = selectedMonth,
                items = monthItems,
                state = rememberKmpWheelPickerState(isInfinite = true, initiallySelectedItemIndex = selectedMonth),
                indicator = remember { KmpWheelPickerIndicators.window(shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)) },
                scrollEffect = remember { KmpWheelPickerScrollEffects.magnify(alignment = Alignment.Start, itemHorizontalPadding = pickerHPadding) },
                onChange = { month ->
                    selectedDate.value = LocalDate(
                        month = month,
                        dayOfMonth = min(
                            selectedDate.value.dayOfMonth,
                            month.lengthInDays(selectedDate.value.year)
                        ),
                        year = selectedDate.value.year
                    ).coerceIn(minDate, maxDate)

                    viewDate.value = LocalDate(
                        month = month,
                        dayOfMonth = min(
                            viewDate.value.dayOfMonth,
                            month.lengthInDays(viewDate.value.year)
                        ),
                        year = viewDate.value.year
                    ).coerceIn(minDate, maxDate)

                    onChange(selectedDate.value)
                }
            ) { month ->
                val isSelectedMonth = month == selectedDate.value.month

                Box(
                    modifier = Modifier
                        .height(daySize)
                        .padding(start = pickerHPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = month.getDisplayName(DateTextFormat.Full),
                        style = TextStyle(
                            color = if (isSelectedMonth) styles.accentColor else styles.fontColor,
                            fontSize = 18.sp,
                        ),
                    )
                }
            }

            KmpWheelPicker(
                modifier = Modifier.height(daySize * 5),
                isEnabled = isEnabled,
                selectedIndex = selectedYear,
                state = rememberKmpWheelPickerState(isInfinite = false, initiallySelectedItemIndex = selectedYear),
                items = yearItems,
                indicator = remember { KMPWheelPickerIndicators.window(shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)) },
                onChange = { year ->
                    selectedDate.value = LocalDate(
                        month = selectedDate.value.month,
                        dayOfMonth = min(
                            selectedDate.value.dayOfMonth,
                            selectedDate.value.month.lengthInDays(year)
                        ),
                        year = year
                    ).coerceIn(minDate, maxDate)

                    viewDate.value = LocalDate(
                        month = viewDate.value.month,
                        dayOfMonth = min(
                            viewDate.value.dayOfMonth,
                            viewDate.value.month.lengthInDays(year)
                        ),
                        year = year
                    ).coerceIn(minDate, maxDate)

                    onChange(selectedDate.value)
                }
            ) { year ->
                val isSelectedYear = year == selectedDate.value.year

                Box(
                    modifier = Modifier
                        .height(daySize)
                        .padding(horizontal = pickerHPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = year.toString(),
                        style = TextStyle(
                            color = if (isSelectedYear) styles.accentColor else styles.fontColor,
                            fontSize = 18.sp,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun DatePickerDay(
    label: String,
    isSelected: Boolean = false,
    isEnabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val styles = LocalKMPDatePickerStyles.current

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .graphicsLayer { alpha = if (isEnabled) 1f else .25f }
            .run { if (onClick != null) clickable(onClick = onClick, enabled = isEnabled) else this }
            .size(daySize)
            .background(if (isSelected) MaterialTheme.colors.primary else Color.Transparent, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, style = TextStyle(color = if (isSelected) styles.fontColorOnAccent else styles.fontColor))
    }
}

private fun DayOfWeek.sundayFirstOrdinal(): Int = when (this.ordinal) {
    7 -> 0
    else -> this.ordinal
}

@Immutable
data class KMPDatePickerStyles(
    val accentColor: Color,
    val fontColor: Color,
    val backgroundColor: Color,
    val fontColorOnAccent: Color,
    val buttonStyle: TextStyle,
)

@Composable
fun rememberKmpDatePickerStyles(): KMPDatePickerStyles {
    val colors = MaterialTheme.colors
    val typography = MaterialTheme.typography

    return remember {
        KMPDatePickerStyles(
            accentColor = colors.primary,
            fontColor = typography.body1.color,
            fontColorOnAccent = colors.onPrimary,
            backgroundColor = Color.White,
            buttonStyle = typography.button,
        )
    }
}

val LocalKMPDatePickerStyles = staticCompositionLocalOf {
    KMPDatePickerStyles(
        accentColor = Color.Black,
        fontColor = Color.Black,
        backgroundColor = Color.White,
        fontColorOnAccent = Color.Black,
        buttonStyle = TextStyle(),
    )
}