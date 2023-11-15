package com.outsidesource.oskitcompose.date

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

fun Month.lengthInDays(year: Int): Int = when (this) {
    Month.JANUARY, Month.MARCH, Month.MAY, Month.JULY, Month.AUGUST, Month.OCTOBER, Month.DECEMBER -> 31
    Month.FEBRUARY -> if (isLeapYear(year)) 29 else 28
    else -> 30
}

enum class DateTextFormat {
    Short,
    Full,
}

fun Month.getDisplayName(format: DateTextFormat): String = when(this) {
    Month.JANUARY -> when(format) {
        DateTextFormat.Full -> "January"
        DateTextFormat.Short -> "Jan"
    }
    Month.FEBRUARY -> when(format) {
        DateTextFormat.Full -> "February"
        DateTextFormat.Short -> "Feb"
    }
    Month.MARCH -> when(format) {
        DateTextFormat.Full -> "March"
        DateTextFormat.Short -> "Mar"
    }
    Month.APRIL -> when(format) {
        DateTextFormat.Full -> "April"
        DateTextFormat.Short -> "Apr"
    }
    Month.MAY -> when(format) {
        DateTextFormat.Full -> "May"
        DateTextFormat.Short -> "May"
    }
    Month.JUNE -> when(format) {
        DateTextFormat.Full -> "June"
        DateTextFormat.Short -> "Jun"
    }
    Month.JULY -> when(format) {
        DateTextFormat.Full -> "July"
        DateTextFormat.Short -> "Jul"
    }
    Month.AUGUST -> when(format) {
        DateTextFormat.Full -> "August"
        DateTextFormat.Short -> "Aug"
    }
    Month.SEPTEMBER -> when(format) {
        DateTextFormat.Full -> "September"
        DateTextFormat.Short -> "Sep"
    }
    Month.OCTOBER -> when(format) {
        DateTextFormat.Full -> "October"
        DateTextFormat.Short -> "Oct"
    }
    Month.NOVEMBER -> when(format) {
        DateTextFormat.Full -> "November"
        DateTextFormat.Short -> "Nov"
    }
    Month.DECEMBER -> when(format) {
        DateTextFormat.Full -> "December"
        DateTextFormat.Short -> "Dec"
    }
    else -> ""
}

fun DayOfWeek.getDisplayName(format: DateTextFormat): String = when(this) {
    DayOfWeek.SUNDAY -> when(format) {
        DateTextFormat.Full -> "Sunday"
        DateTextFormat.Short -> "Sun"
    }
    DayOfWeek.MONDAY ->  when(format) {
        DateTextFormat.Full -> "Monday"
        DateTextFormat.Short -> "Mon"
    }
    DayOfWeek.TUESDAY ->  when(format) {
        DateTextFormat.Full -> "Tuesday"
        DateTextFormat.Short -> "Tue"
    }
    DayOfWeek.WEDNESDAY ->  when(format) {
        DateTextFormat.Full -> "Wednesday"
        DateTextFormat.Short -> "Wed"
    }
    DayOfWeek.THURSDAY ->  when(format) {
        DateTextFormat.Full -> "Thursday"
        DateTextFormat.Short -> "Thu"
    }
    DayOfWeek.FRIDAY ->  when(format) {
        DateTextFormat.Full -> "Friday"
        DateTextFormat.Short -> "Fri"
    }
    DayOfWeek.SATURDAY ->  when(format) {
        DateTextFormat.Full -> "Saturday"
        DateTextFormat.Short -> "Sat"
    }
    else -> ""
}

fun LocalDate.isLeapYear(): Boolean = isLeapYear(year)

private fun isLeapYear(year: Int): Boolean {
    if (year % 4 != 0) return false
    if (year % 100 != 0) return true
    if (year % 400 != 0) return false
    return true
}