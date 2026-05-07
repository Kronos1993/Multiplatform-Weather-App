package com.kronos.multiplatform.weatherapp.core.util

import androidx.compose.runtime.Composable
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import weather_app.composeapp.generated.resources.Res
import weather_app.composeapp.generated.resources.month_apr
import weather_app.composeapp.generated.resources.month_aug
import weather_app.composeapp.generated.resources.month_dec
import weather_app.composeapp.generated.resources.month_feb
import weather_app.composeapp.generated.resources.month_jan
import weather_app.composeapp.generated.resources.month_jul
import weather_app.composeapp.generated.resources.month_jun
import weather_app.composeapp.generated.resources.month_mar
import weather_app.composeapp.generated.resources.month_may
import weather_app.composeapp.generated.resources.month_nov
import weather_app.composeapp.generated.resources.month_oct
import weather_app.composeapp.generated.resources.month_sep
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
fun formatDateTime(
    instant: Instant,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    language: String = "en"
): String {
    val localDateTime = instant.toLocalDateTime(timeZone)

    val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
    val month = localDateTime.monthNumber.toString().padStart(2, '0')
    val year = localDateTime.year.toString()

    val hour24 = localDateTime.hour
    val hour12 = if (hour24 % 12 == 0) 12 else hour24 % 12
    val minute = localDateTime.minute.toString().padStart(2, '0')
    val second = localDateTime.second.toString().padStart(2, '0')

    val amPm = if (hour24 < 12) getAmPmText(language, true) else getAmPmText(language, false)

    return "$day/$month/$year $hour12:$minute:$second $amPm"
}

@OptIn(ExperimentalTime::class)
fun formatDateTime(
    instant: Instant,
    format: String,
    language: String = "en",
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
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

            val amPm =
                if (hour24 < 12) getAmPmText(language, true) else getAmPmText(language, false)

            "$day/$month/$year $hour12:$minute:$second $amPm"
        }

        "dd-MMM hh:mm aa" -> {
            val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
            val month = getMonthName(localDateTime.month, language, short = true)
            val hour24 = localDateTime.hour
            val hour12 = if (hour24 % 12 == 0) 12 else hour24 % 12
            val minute = localDateTime.minute.toString().padStart(2, '0')
            val amPm =
                if (hour24 < 12) getAmPmText(language, true) else getAmPmText(language, false)

            "$day-$month $hour12:$minute $amPm"
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
            val month = getMonthName(localDateTime.month, language, short = true)
            val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
            val year = localDateTime.year.toString()

            "$month $day, $year"
        }

        "EEE MMM d | h:mm aa" -> {
            val dayOfWeek = getDayOfWeekName(localDateTime.dayOfWeek, language, short = true)
            val month = getMonthName(localDateTime.month, language, short = false)
            val day = localDateTime.dayOfMonth
            val hour = (localDateTime.hour % 12).let { if (it == 0) 12 else it }
            val minute = localDateTime.minute.toString().padStart(2, '0')
            val amPm = if (localDateTime.hour < 12) getAmPmText(language, true) else getAmPmText(
                language,
                false
            )

            "$dayOfWeek $month $day | $hour:$minute $amPm"
        }

        "HH:mm" -> {
            val hour = localDateTime.hour.toString().padStart(2, '0')
            val minute = localDateTime.minute.toString().padStart(2, '0')
            "$hour:$minute"
        }

        "dd/MM" -> {
            val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
            val month = localDateTime.monthNumber.toString().padStart(2, '0')
            "$day/$month"
        }

        else -> {
            localDateTime.toString()
        }
    }
}

// Función auxiliar para obtener texto AM/PM según el idioma
private fun getAmPmText(language: String, isAm: Boolean): String {
    return when (language) {
        "es" -> if (isAm) "a.m." else "p.m."
        else -> if (isAm) "AM" else "PM"
    }
}

// Función auxiliar para obtener nombre del mes según el idioma
private fun getMonthName(month: Month, language: String, short: Boolean = false): String {
    return when (language) {
        "es" -> when (month) {
            Month.JANUARY -> if (short) "Ene" else "Enero"
            Month.FEBRUARY -> if (short) "Feb" else "Febrero"
            Month.MARCH -> if (short) "Mar" else "Marzo"
            Month.APRIL -> if (short) "Abr" else "Abril"
            Month.MAY -> if (short) "May" else "Mayo"
            Month.JUNE -> if (short) "Jun" else "Junio"
            Month.JULY -> if (short) "Jul" else "Julio"
            Month.AUGUST -> if (short) "Ago" else "Agosto"
            Month.SEPTEMBER -> if (short) "Sep" else "Septiembre"
            Month.OCTOBER -> if (short) "Oct" else "Octubre"
            Month.NOVEMBER -> if (short) "Nov" else "Noviembre"
            Month.DECEMBER -> if (short) "Dic" else "Diciembre"
        }

        else -> if (short) month.name.take(3) else month.name
    }
}

// Función auxiliar para obtener nombre del día de la semana según el idioma
private fun getDayOfWeekName(
    dayOfWeek: DayOfWeek,
    language: String,
    short: Boolean = false
): String {
    return when (language) {
        "es" -> when (dayOfWeek) {
            DayOfWeek.MONDAY -> if (short) "Lun" else "Lunes"
            DayOfWeek.TUESDAY -> if (short) "Mar" else "Martes"
            DayOfWeek.WEDNESDAY -> if (short) "Mié" else "Miércoles"
            DayOfWeek.THURSDAY -> if (short) "Jue" else "Jueves"
            DayOfWeek.FRIDAY -> if (short) "Vie" else "Viernes"
            DayOfWeek.SATURDAY -> if (short) "Sáb" else "Sábado"
            DayOfWeek.SUNDAY -> if (short) "Dom" else "Domingo"
        }

        else -> if (short) dayOfWeek.name.take(3) else dayOfWeek.name
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
fun Instant.getHour(
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    language: String = "en"
): String {
    val localDateTime = this.toLocalDateTime(timeZone)
    val hour24 = localDateTime.hour
    val hour12 = if (hour24 % 12 == 0) 12 else hour24 % 12
    val amPm = if (hour24 < 12) getAmPmText(language, true) else getAmPmText(language, false)

    return "$hour12 $amPm"
}


@OptIn(ExperimentalTime::class)
fun Instant.toDayOfWeekText(timeZone: TimeZone = TimeZone.currentSystemDefault()): DayOfWeek {
    val localDateTime = this.toLocalDateTime(timeZone)
    return localDateTime.dayOfWeek
}

@OptIn(ExperimentalTime::class)
fun Instant.format(
    pattern: String,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): String {
    val dt = toLocalDateTime(timeZone)

    return pattern
        .replace("MM", dt.monthNumber.toString().padStart(2, '0'))
        .replace("dd", dt.dayOfMonth.toString().padStart(2, '0'))
        .replace("yyyy", dt.year.toString())
        .replace("hh", ((dt.hour % 12).takeIf { it != 0 } ?: 12).toString().padStart(2, '0'))
        .replace("mm", dt.minute.toString().padStart(2, '0'))
        .replace("a", if (dt.hour < 12) "AM" else "PM")
}


@Composable
fun monthLabel(month: Int): String {
    return stringResource(when (month) {
        1  -> Res.string.month_jan
        2  -> Res.string.month_feb
        3  -> Res.string.month_mar
        4  -> Res.string.month_apr
        5  -> Res.string.month_may
        6  -> Res.string.month_jun
        7  -> Res.string.month_jul
        8  -> Res.string.month_aug
        9  -> Res.string.month_sep
        10 -> Res.string.month_oct
        11 -> Res.string.month_nov
        else -> Res.string.month_dec
    })
}