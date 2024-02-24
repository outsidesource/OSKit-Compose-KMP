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
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.outsidesource.oskitcompose.date.DateTextFormat
import com.outsidesource.oskitcompose.date.getDisplayName
import com.outsidesource.oskitcompose.date.lengthInDays
import com.outsidesource.oskitcompose.popup.Modal
import com.outsidesource.oskitcompose.popup.ModalStyles
import com.outsidesource.oskitcompose.popup.Popover
import com.outsidesource.oskitcompose.popup.PopoverAnchors
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone.Companion.currentSystemDefault
import kotlin.math.min

private val hPadding = 16.dp
private val daySize = 40.dp

private enum class DatePickerViewType {
    Month,
    Year,
}

@Composable
fun DatePicker(
    isVisible: Boolean,
    onDismissRequest: (() -> Unit)? = null,
    date: LocalDate = Clock.System.now().toLocalDateTime(currentSystemDefault()).date,
    minDate: LocalDate = LocalDate(0, Month.JANUARY, 1),
    maxDate: LocalDate = LocalDate(3000, Month.DECEMBER, 31),
    centerInWindow: Boolean = false,
    isFullScreen: Boolean = true,
    onChange: (date: LocalDate) -> Unit,
) {
    val viewType = remember { mutableStateOf(DatePickerViewType.Month) }

    if (centerInWindow) {
        DatePickerModal(isVisible, onDismissRequest, isFullScreen, date, minDate, maxDate, onChange, viewType)
    } else {
        DatePickerPopover(isVisible, onDismissRequest, date, minDate, maxDate, onChange, viewType)
    }
}

@Composable
private fun DatePickerModal(
    isVisible: Boolean,
    onDismissRequest: (() -> Unit)? = null,
    isFullScreen: Boolean = true,
    date: LocalDate = Clock.System.now().toLocalDateTime(currentSystemDefault()).date,
    minDate: LocalDate = LocalDate(0, Month.JANUARY, 1),
    maxDate: LocalDate = LocalDate(3000, Month.DECEMBER, 31),
    onChange: (date: LocalDate) -> Unit,
    viewType: MutableState<DatePickerViewType>,
) {
    Modal(
        isVisible = isVisible,
        dismissOnExternalClick = true,
        onDismissRequest = onDismissRequest,
        styles = ModalStyles.UserDefinedContent,
        isFullScreen = isFullScreen,
        onKeyEvent = {
            if (it.key == Key.Escape || it.key == Key.Back) onDismissRequest?.invoke()
            false
        },
    ) {
        DatePickerContent(onDismissRequest, date, minDate, maxDate, onChange, viewType)
    }
}

@Composable
private fun DatePickerPopover(
    isVisible: Boolean,
    onDismissRequest: (() -> Unit)? = null,
    date: LocalDate = Clock.System.now().toLocalDateTime(currentSystemDefault()).date,
    minDate: LocalDate = LocalDate(0, Month.JANUARY, 1),
    maxDate: LocalDate = LocalDate(3000, Month.DECEMBER, 31),
    onChange: (date: LocalDate) -> Unit,
    viewType: MutableState<DatePickerViewType>,
) {
    Popover(
        isVisible = isVisible,
        anchors = PopoverAnchors.ExternalBottomAlignCenter,
        onDismissRequest = onDismissRequest,
        onKeyEvent = {
            if (it.key == Key.Escape) onDismissRequest?.invoke()
            false
        },
        offset = DpOffset(0.dp, (-16f).dp),
    ) {
        DatePickerContent(onDismissRequest, date, minDate, maxDate, onChange, viewType)
    }
}

@Composable
private fun DatePickerContent(
    onDismissRequest: (() -> Unit)? = null,
    date: LocalDate = Clock.System.now().toLocalDateTime(currentSystemDefault()).date,
    minDate: LocalDate = LocalDate(0, Month.JANUARY, 1),
    maxDate: LocalDate = LocalDate(3000, Month.DECEMBER, 31),
    onChange: (date: LocalDate) -> Unit,
    viewType: MutableState<DatePickerViewType>,
) {
    val viewDate = remember(date) { mutableStateOf(date) }
    val selectedDate = remember(date) { mutableStateOf(date) }

    DisposableEffect(Unit) {
        onDispose {
            viewType.value = DatePickerViewType.Month
            viewDate.value = date
            selectedDate.value = date
        }
    }

    Column(
        modifier = Modifier
            .width(((hPadding * 2) * 2) + (daySize * 7))
            .padding(16.dp)
            .shadow(16.dp, RoundedCornerShape(8.dp))
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.primary)
                .padding(top = 4.dp, bottom = 4.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier
                    .height(daySize)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable {
                        viewType.value = when (viewType.value) {
                            DatePickerViewType.Month -> DatePickerViewType.Year
                            else -> DatePickerViewType.Month
                        }
                    }
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "${viewDate.value.month.getDisplayName(DateTextFormat.Full)} ${viewDate.value.year}",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = MaterialTheme.colors.onPrimary,
                        fontWeight = FontWeight.Medium,
                    )
                )

                val rotation by animateFloatAsState(if (viewType.value == DatePickerViewType.Year) 90f else 0f, tween())

                Image(
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer { rotationZ = rotation },
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    colorFilter = ColorFilter.tint(Color.White),
                    contentDescription = "Change View",
                )
            }

            AnimatedVisibility(visible = viewType.value == DatePickerViewType.Month, enter = fadeIn(), exit = fadeOut()) {
                Row {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { viewDate.value = (viewDate.value - DatePeriod(months = 1)).coerceIn(minDate, maxDate) }
                            .size(daySize),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowLeft,
                            tint = Color.White,
                            contentDescription = "Previous month"
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { viewDate.value = (viewDate.value + DatePeriod(months = 1)).coerceIn(minDate, maxDate) }
                            .size(daySize),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowRight,
                            tint = Color.White,
                            contentDescription = "Next month"
                        )
                    }
                }
            }
        }

        Box {
            DatePickerMonthView(viewType.value, viewDate, selectedDate, minDate, maxDate)
            DatePickerYearView(viewType, viewDate, selectedDate, minDate, maxDate)
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton({
                onDismissRequest?.invoke()
            }) {
                Text("CANCEL", style = MaterialTheme.typography.button)
            }
            TextButton({
                onDismissRequest?.invoke()
                onChange(selectedDate.value)
            }) {
                Text("OK", style = MaterialTheme.typography.button)
            }
        }
    }
}

@Composable
private fun DatePickerMonthView(
    viewType: DatePickerViewType,
    viewDate: MutableState<LocalDate>,
    selectedDate: MutableState<LocalDate>,
    minDate: LocalDate,
    maxDate: LocalDate,
) {
    AnimatedVisibility(viewType == DatePickerViewType.Month, enter = fadeIn(), exit = fadeOut()) {
        Column(
            modifier = Modifier
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(bottom = 8.dp)
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
                transitionSpec = {
                    slideInHorizontally {
                        if (initialState.month == Month.JANUARY && targetState.month == Month.DECEMBER) return@slideInHorizontally -it
                        if (targetState.month > initialState.month || (initialState.month == Month.DECEMBER && targetState.month == Month.JANUARY)) it else -it
                    } togetherWith slideOutHorizontally {
                        if (initialState.month == Month.JANUARY && targetState.month == Month.DECEMBER) return@slideOutHorizontally it
                        if (targetState.month > initialState.month || (initialState.month == Month.DECEMBER && targetState.month == Month.JANUARY)) -it else it
                    }
                }
            ) {currentDateValue ->
                val dayOne = currentDateValue - DatePeriod(days = currentDateValue.dayOfMonth - 1)
                val startIndex = dayOne.dayOfWeek.sundayFirstOrdinal() + 1
                val maxIndex = (dayOne.month.lengthInDays(dayOne.year) + startIndex)

                Column(
                    modifier = Modifier.height(daySize * 6),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    for (i in 0 until 6) {
                        Row {
                            for (j in 0..7) {
                                val index = (i * 7) + j

                                if (index < startIndex) {
                                    DatePickerDay("")
                                } else if (index < maxIndex) {
                                    val day = index - startIndex + 1
                                    val isEnabled = run {
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
                                        isEnabled = isEnabled,
                                        isSelected = currentDateValue.year == selectedDate.value.year &&
                                                currentDateValue.month == selectedDate.value.month &&
                                                selectedDate.value.dayOfMonth == day,
                                        onClick = {
                                            selectedDate.value =
                                                LocalDate(currentDateValue.year, currentDateValue.month, day)
                                        }
                                    )
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
    val dayNameTextStyle = remember {
        TextStyle(color = Color.Black.copy(alpha = .5f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
    Text(modifier = Modifier.width(daySize), text = text, style = dayNameTextStyle, textAlign = TextAlign.Center)
}

@Composable
private fun DatePickerYearView(
    viewType: MutableState<DatePickerViewType>,
    viewDate: MutableState<LocalDate>,
    selectedDate: MutableState<LocalDate>,
    minDate: LocalDate,
    maxDate: LocalDate,
) {
    AnimatedVisibility(viewType.value == DatePickerViewType.Year, enter = fadeIn(), exit = fadeOut()) {
        val pickerHPadding = 40.dp

        Row(
            modifier = Modifier
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 0.dp)
                .height(daySize * 6 + 22.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val monthItems = remember(minDate, maxDate, viewDate.value.year) {
                if (viewDate.value.year > minDate.year && viewDate.value.year < maxDate.year) {
                    return@remember Month.values().toList()
                }

                Month.values().toList().mapNotNull {
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

            KMPWheelPicker(
                modifier = Modifier
                    .height(daySize * 5)
                    .weight(1f),
                selectedIndex = selectedMonth,
                items = monthItems,
                state = rememberKmpWheelPickerState(isInfinite = true, initiallySelectedItemIndex = selectedMonth),
                indicator = remember { KMPWheelPickerIndicators.window(shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)) },
                scrollEffect = remember { KMPWheelPickerScrollEffects.magnify(alignment = Alignment.Start, horizontalPadding = pickerHPadding) },
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
                            color = if (isSelectedMonth) MaterialTheme.colors.primary else Color.Black,
                            fontSize = 18.sp,
                        ),
                    )
                }
            }

            KMPWheelPicker(
                modifier = Modifier.height(daySize * 5),
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
                            color = if (isSelectedYear) MaterialTheme.colors.primary else Color.Black,
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
    style: TextStyle = TextStyle(),
    onClick: (() -> Unit) = { },
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .graphicsLayer { alpha = if (isEnabled) 1f else .25f }
            .clickable(onClick = onClick, enabled = isEnabled)
            .size(daySize)
            .background(if (isSelected) MaterialTheme.colors.primary else Color.Transparent, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, style = style.copy(if (isSelected) MaterialTheme.colors.onPrimary else style.color))
    }
}

private fun DayOfWeek.sundayFirstOrdinal(): Int = when (this.ordinal) {
    7 -> 0
    else -> this.ordinal
}