package com.kronos.multiplatform.weatherapp.core.util

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
fun Instant.dateFormat(format: String, timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
    return try {
        val localDateTime = this.toLocalDateTime(timeZone)
        formatDateTime(this, format, timeZone)
    } catch (e: Exception) {
        ""
    }
}

// Extensión para verificar si es hoy
@OptIn(ExperimentalTime::class)
fun Instant.isToday(timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean {
    val today = Clock.System.now().toLocalDateTime(timeZone).date
    val thisDate = this.toLocalDateTime(timeZone).date
    return today == thisDate
}

// Función para crear Instant desde string (similar a Date.of())
@OptIn(ExperimentalTime::class)
fun Instant.Companion.of(
    value: String,
    includeHours: Boolean = false,
    timezone: TimeZone? = null
): Instant? {
    return try {
        val timeZoneToUse = timezone ?: TimeZone.currentSystemDefault()

        if (!includeHours) {
            // Formato: "yyyy-MM-dd"
            val parts = value.split("-")
            if (parts.size == 3) {
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                val day = parts[2].toInt()
                LocalDateTime(year, month, day, 0, 0, 0).toInstant(timeZoneToUse)
            } else {
                null
            }
        } else {
            // Formato: "yyyy-MM-dd HH:mm"
            val dateTimeParts = value.split(" ")
            if (dateTimeParts.size == 2) {
                val dateParts = dateTimeParts[0].split("-")
                val timeParts = dateTimeParts[1].split(":")

                if (dateParts.size == 3 && timeParts.size == 2) {
                    val year = dateParts[0].toInt()
                    val month = dateParts[1].toInt()
                    val day = dateParts[2].toInt()
                    val hour = timeParts[0].toInt()
                    val minute = timeParts[1].toInt()

                    LocalDateTime(year, month, day, hour, minute, 0).toInstant(timeZoneToUse)
                } else {
                    null
                }
            } else {
                null
            }
        }
    } catch (e: Exception) {
        println("Exception occurred: ${e.message}")
        null
    }
}

// Extensión para obtener la hora en formato 12h (similar a Date.getHour())
@OptIn(ExperimentalTime::class)
fun Instant.getHour(timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
    val localDateTime = this.toLocalDateTime(timeZone)
    val hour24 = localDateTime.hour
    val hour12 = if (hour24 % 12 == 0) 12 else hour24 % 12
    val amPm = if (hour24 < 12) "a.m." else "p.m."

    return "$hour12 $amPm"
}

// Función para transformar fecha a "hoy"/"ayer" (similar a Date.transformDateToTodayOrYesterday())
@OptIn(ExperimentalTime::class)
fun Instant.transformDateToTodayOrYesterday(
    inputDate: String,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): String {
    val inputInstant = Instant.of(inputDate, includeHours = true, timezone = timeZone) ?: return inputDate

    val today = Clock.System.now().toLocalDateTime(timeZone).date
    val yesterday = today.minus(1, DateTimeUnit.DAY)
    val inputDateLocal = inputInstant.toLocalDateTime(timeZone).date

    val timeFormatter = { instant: Instant ->
        instant.toLocalDateTime(timeZone).let {
            val hour12 = if (it.hour % 12 == 0) 12 else it.hour % 12
            val minute = it.minute.toString().padStart(2, '0')
            val amPm = if (it.hour < 12) "aa" else "aa" // AM/PM
            "$hour12:$minute $amPm"
        }
    }

    return when {
        inputDateLocal == today -> "today ${timeFormatter(inputInstant)}"
        inputDateLocal == yesterday -> "yesterday ${timeFormatter(inputInstant)}"
        else -> {
            val dateFormatter = { instant: Instant ->
                instant.toLocalDateTime(timeZone).let {
                    val day = it.dayOfMonth.toString().padStart(2, '0')
                    val month = it.monthNumber.toString().padStart(2, '0')
                    val year = it.year.toString().takeLast(2)
                    val hour12 = if (it.hour % 12 == 0) 12 else it.hour % 12
                    val minute = it.minute.toString().padStart(2, '0')
                    val amPm = if (it.hour < 12) "aa" else "aa"
                    "$day/$month/$year $hour12:$minute $amPm"
                }
            }
            dateFormatter(inputInstant)
        }
    }
}

// Función auxiliar para verificar si dos Instant son el mismo día
@OptIn(ExperimentalTime::class)
fun Instant.isSameDay(
    other: Instant,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): Boolean {
    val thisDate = this.toLocalDateTime(timeZone).date
    val otherDate = other.toLocalDateTime(timeZone).date
    return thisDate == otherDate
}

// Función auxiliar sobrecargada para compatibilidad con la función original
@OptIn(ExperimentalTime::class)
fun Instant.isSameDay(
    date1: Instant,
    date2: Instant,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): Boolean {
    return date1.isSameDay(date2, timeZone)
}

// Manteniendo tus funciones existentes y añadiendo mejoras
@OptIn(ExperimentalTime::class)
fun formatDateTime(
    instant: Instant,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): String {
    return instant.dateFormat("dd/MM/yyyy hh:mm:ss a", timeZone)
}

@OptIn(ExperimentalTime::class)
fun formatDateTime(
    instant: Instant,
    format: String,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): String {
    val localDateTime = instant.toLocalDateTime(timeZone)

    return when (format) {
        "dd/MM/yyyy hh:mm:ss a" -> {
            val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
            val month = localDateTime.monthNumber.toString().padStart(2, '0')
            val year = localDateTime.year.toString()

            val hour24 = localDateTime.hour
            val hour12 = if (hour24 % 12 == 0) 12 else hour24 % 12
            val minute = localDateTime.minute.toString().padStart(2, '0')
            val second = localDateTime.second.toString().padStart(2, '0')

            val amPm = if (hour24 < 12) "AM" else "PM"

            "$day/$month/$year $hour12:$minute:$second $amPm"
        }

        "yyyy-MM-dd HH:mm:ss" -> {
            val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
            val month = localDateTime.monthNumber.toString().padStart(2, '0')
            val year = localDateTime.year.toString()

            val hour = localDateTime.hour.toString().padStart(2, '0')
            val minute = localDateTime.minute.toString().padStart(2, '0')
            val second = localDateTime.second.toString().padStart(2, '0')

            "$year-$month-$day $hour:$minute:$second"
        }

        "MMM dd, yyyy" -> {
            val month = localDateTime.month.name.take(3)
            val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
            val year = localDateTime.year.toString()

            "$month $day, $year"
        }

        "hh:mm aa" -> {
            val hour24 = localDateTime.hour
            val hour12 = if (hour24 % 12 == 0) 12 else hour24 % 12
            val minute = localDateTime.minute.toString().padStart(2, '0')
            val amPm = if (hour24 < 12) "AM" else "PM"
            "$hour12:$minute $amPm"
        }

        "dd/MM/yy hh:mm aa" -> {
            val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
            val month = localDateTime.monthNumber.toString().padStart(2, '0')
            val year = localDateTime.year.toString().takeLast(2)
            val hour24 = localDateTime.hour
            val hour12 = if (hour24 % 12 == 0) 12 else hour24 % 12
            val minute = localDateTime.minute.toString().padStart(2, '0')
            val amPm = if (hour24 < 12) "AM" else "PM"
            "$day/$month/$year $hour12:$minute $amPm"
        }

        else -> {
            localDateTime.toString()
        }
    }
}

@OptIn(ExperimentalTime::class)
fun formatDate(
    instant: Instant,
    format: String,
    timeZone: TimeZone = TimeZone.UTC
): String {
    val localDateTime = instant.toLocalDateTime(timeZone)
    val localDate = localDateTime.date

    return when (format) {
        "yyyy-MM-dd" -> {
            val day = localDate.dayOfMonth.toString().padStart(2, '0')
            val month = localDate.monthNumber.toString().padStart(2, '0')
            val year = localDate.year.toString()
            "$year-$month-$day"
        }
        "dd-MM" -> {
            val day = localDate.dayOfMonth.toString().padStart(2, '0')
            val month = localDate.monthNumber.toString().padStart(2, '0')
            "$day-$month"
        }
        else -> {
            localDateTime.toString()
        }
    }
}

// Las funciones calculateTimeBetweenDate y getLastSixMonths se mantienen igual
@OptIn(ExperimentalTime::class)
fun calculateTimeBetweenDate(
    date: Instant,
    currentDate: Instant = Clock.System.now(),
    lessThanMinuteText: String = "less than a minute ago",
    oneMinuteText: String = "a minute ago",
    minutesText: String = "%d minutes ago",
    oneHourText: String = "an hour ago",
    hoursText: String = "%d hours ago",
    oneDayText: String = "a day ago",
    daysText: String = "%d days ago",
    oneYearText: String = "a year ago",
    yearsText: String = "%d years ago"
): String {
    val duration = currentDate - date
    val seconds = duration.inWholeSeconds
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val years = days / 365

    return when {
        seconds < 60 -> lessThanMinuteText
        seconds < 120 -> oneMinuteText
        minutes < 60 -> minutesText.format(minutes.toInt())
        minutes < 120 -> oneHourText
        hours < 24 -> hoursText.format(hours.toInt())
        hours < 48 -> oneDayText
        days < 365 -> daysText.format(days.toInt())
        days < 730 -> oneYearText
        else -> yearsText.format(years.toInt())
    }
}

@OptIn(ExperimentalTime::class)
fun getLastSixMonths(): List<String> {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val months = mutableListOf<String>()

    var monthValue = now.monthNumber
    var year = now.year

    repeat(6) {
        val monthName = Month.entries[monthValue - 1].name
            .lowercase()
            .replaceFirstChar { it.uppercase() }

        months.add(0, "${monthName.substring(0..2)} $year")

        monthValue -= 1
        if (monthValue == 0) {
            monthValue = 12
            year -= 1
        }
    }

    return months
}