package com.kronos.multiplatform.weatherapp.core.util

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
fun formatDateTime(
    instant: Instant,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): String {
    val localDateTime = instant.toLocalDateTime(timeZone)

    val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
    val month = localDateTime.monthNumber.toString().padStart(2, '0')
    val year = localDateTime.year.toString()

    val hour24 = localDateTime.hour
    val hour12 = if (hour24 % 12 == 0) 12 else hour24 % 12
    val minute = localDateTime.minute.toString().padStart(2, '0')
    val second = localDateTime.second.toString().padStart(2, '0')

    val amPm = if (hour24 < 12) "AM" else "PM"

    return "$day/$month/$year $hour12:$minute:$second $amPm"
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

        "EEE MMM d | h:mm aa" -> {
            val dayOfWeek = localDateTime.dayOfWeek.name.take(3)
            val month = localDateTime.month.name
            val day = localDateTime.dayOfMonth
            val hour = localDateTime.hour % 12.let { if (it == 0) 12 else it }
            val minute = localDateTime.minute.toString().padStart(2, '0')
            val amPm = if (localDateTime.hour < 12) "AM" else "PM"

            "$dayOfWeek $month $day | $hour:$minute $amPm"
        }

        else -> {
            localDateTime.toString()
        }
    }
}

// Extensión para verificar si es hoy
@OptIn(ExperimentalTime::class)
fun Instant.isToday(timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean {
    val today = Clock.System.now().toLocalDateTime(timeZone).date
    val thisDate = this.toLocalDateTime(timeZone).date
    return today == thisDate
}

@OptIn(ExperimentalTime::class)
fun Instant.isTomorrow(timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean {
    val today = Clock.System.now().toLocalDateTime(timeZone).date
    val tomorrow = today.plus(1, DateTimeUnit.DAY)
    val thisDate = this.toLocalDateTime(timeZone).date
    return thisDate == tomorrow
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


@OptIn(ExperimentalTime::class)
fun Instant.toDayOfWeekText(timeZone: TimeZone = TimeZone.currentSystemDefault()): DayOfWeek {
    val localDateTime = this.toLocalDateTime(timeZone)
    return localDateTime.dayOfWeek
}