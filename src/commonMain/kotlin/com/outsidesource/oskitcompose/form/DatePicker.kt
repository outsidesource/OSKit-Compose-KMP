package com.outsidesource.oskitcompose.form

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.outsidesource.oskitcompose.date.DateTextFormat
import com.outsidesource.oskitcompose.date.getDisplayName
import com.outsidesource.oskitcompose.date.lengthInDays
import com.outsidesource.oskitcompose.popup.*
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
    centerInWindow: Boolean = false,
    isFullScreen: Boolean = true,
    onChange: (date: LocalDate) -> Unit,
) {
    val viewType = remember { mutableStateOf(DatePickerViewType.Month) }
    val currentDate = remember(date) { mutableStateOf(date) }
    val selectedDate = remember(date) { mutableStateOf(date) }

    if (centerInWindow) {
        DatePickerModal(isVisible, onDismissRequest, isFullScreen, date, onChange, viewType, currentDate, selectedDate)
    } else {
        DatePickerPopover(isVisible, onDismissRequest, date, onChange, viewType, currentDate, selectedDate)
    }
}

@Composable
private fun DatePickerModal(
    isVisible: Boolean,
    onDismissRequest: (() -> Unit)? = null,
    isFullScreen: Boolean = true,
    date: LocalDate = Clock.System.now().toLocalDateTime(currentSystemDefault()).date,
    onChange: (date: LocalDate) -> Unit,
    viewType: MutableState<DatePickerViewType>,
    currentDate: MutableState<LocalDate>,
    selectedDate: MutableState<LocalDate>,
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
        DatePickerContent(onDismissRequest, date, onChange, viewType, currentDate, selectedDate)
    }
}

@Composable
private fun DatePickerPopover(
    isVisible: Boolean,
    onDismissRequest: (() -> Unit)? = null,
    date: LocalDate = Clock.System.now().toLocalDateTime(currentSystemDefault()).date,
    onChange: (date: LocalDate) -> Unit,
    viewType: MutableState<DatePickerViewType>,
    currentDate: MutableState<LocalDate>,
    selectedDate: MutableState<LocalDate>,
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
        DatePickerContent(onDismissRequest, date, onChange, viewType, currentDate, selectedDate)
    }
}

@Composable
private fun DatePickerContent(
    onDismissRequest: (() -> Unit)? = null,
    date: LocalDate = Clock.System.now().toLocalDateTime(currentSystemDefault()).date,
    onChange: (date: LocalDate) -> Unit,
    viewType: MutableState<DatePickerViewType>,
    currentDate: MutableState<LocalDate>,
    selectedDate: MutableState<LocalDate>,
) {
    DisposableEffect(Unit) {
        onDispose {
            viewType.value = DatePickerViewType.Month
            currentDate.value = date
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.primary)
                .padding(top = 12.dp, bottom = 12.dp, start = 8.dp, end = 16.dp),
        ) {
            Text(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { viewType.value = DatePickerViewType.Year }
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                text = selectedDate.value.year.toString(),
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 16.sp,
                    color = MaterialTheme.colors.onPrimary
                )
            )
            Text(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { viewType.value = DatePickerViewType.Month }
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                text = "${
                    selectedDate.value.dayOfWeek.getDisplayName(DateTextFormat.Short)
                }, " +
                        "${selectedDate.value.month.getDisplayName(DateTextFormat.Full)} " +
                        "${selectedDate.value.dayOfMonth}",
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 24.sp,
                    color = MaterialTheme.colors.onPrimary
                )
            )
        }

        Box {
            DatePickerMonthView(viewType.value, currentDate, selectedDate)
            DatePickerYearView(viewType, currentDate, selectedDate)
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
    currentDate: MutableState<LocalDate>,
    selectedDate: MutableState<LocalDate>,
) {
    Column {
        AnimatedVisibility(viewType == DatePickerViewType.Month, enter = fadeIn(), exit = fadeOut()) {
            Column(
                modifier = Modifier
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { currentDate.value -= DatePeriod(months = 1) }
                            .size(daySize),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(imageVector = Icons.Filled.KeyboardArrowLeft, contentDescription = "Previous month")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(currentDate.value.month.getDisplayName(DateTextFormat.Full))
                        Text(currentDate.value.year.toString())
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { currentDate.value += DatePeriod(months = 1) }
                            .size(daySize),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(imageVector = Icons.Filled.KeyboardArrowRight, contentDescription = "Next month")
                    }
                }

                Row {
                    val dayStyle = remember {
                        androidx.compose.ui.text.TextStyle(color = Color.Black.copy(alpha = .5f))
                    }
                    DatePickerDay("S", style = dayStyle)
                    DatePickerDay("M", style = dayStyle)
                    DatePickerDay("T", style = dayStyle)
                    DatePickerDay("W", style = dayStyle)
                    DatePickerDay("T", style = dayStyle)
                    DatePickerDay("F", style = dayStyle)
                    DatePickerDay("S", style = dayStyle)
                }

                AnimatedContent(
                    targetState = currentDate.value,
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
                                        DatePickerDay(
                                            label = day.toString(),
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
}

@Composable
private fun DatePickerYearView(
    viewType: MutableState<DatePickerViewType>,
    currentDate: MutableState<LocalDate>,
    selectedDate: MutableState<LocalDate>,
) {
    Column {
        AnimatedVisibility(viewType.value == DatePickerViewType.Year, enter = fadeIn(), exit = fadeOut()) {
            val count = 200

            KMPWheelPicker(
                modifier = Modifier
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 0.dp)
                    .height(daySize * 8)
                    .fillMaxWidth(),
                selectedIndex = 100,
                items = ((currentDate.value.year - count / 2)..(currentDate.value.year + count / 2)).toList(),
                onChange = { year ->
                    selectedDate.value = LocalDate(
                        month = selectedDate.value.month,
                        dayOfMonth = min(
                            selectedDate.value.dayOfMonth,
                            selectedDate.value.month.lengthInDays(year)
                        ),
                        year = year
                    )
                    currentDate.value = LocalDate(
                        month = currentDate.value.month,
                        dayOfMonth = min(
                            currentDate.value.dayOfMonth,
                            currentDate.value.month.lengthInDays(year)
                        ),
                        year = year
                    )
                }
            ) { year ->
                val isSelectedYear = year == selectedDate.value.year

                Box(
                    modifier = Modifier.height(daySize),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = year.toString(),
                        style = androidx.compose.ui.text.TextStyle(
                            color = if (isSelectedYear) MaterialTheme.colors.primary else Color.Black,
                            fontSize = 18.sp,
                        ),
                        textAlign = TextAlign.Center,
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
    style: androidx.compose.ui.text.TextStyle = androidx.compose.ui.text.TextStyle(),
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .run { return@run if (onClick != null) clickable(onClick = onClick) else this }
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