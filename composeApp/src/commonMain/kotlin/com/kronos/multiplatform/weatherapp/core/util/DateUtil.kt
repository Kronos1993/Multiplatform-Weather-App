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