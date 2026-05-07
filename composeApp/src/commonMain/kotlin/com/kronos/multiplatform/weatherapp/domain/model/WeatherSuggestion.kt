package com.kronos.multiplatform.weatherapp.domain.model

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock


enum class SuggestionPriority { HIGH, MEDIUM, LOW }

enum class SuggestionType {
    RAIN, UV, HEAT, WIND, COLD, VISIBILITY, HUMIDITY, TOMORROW_FORECAST, MORNING_SUMMARY
}

enum class DayMoment {
    MORNING,    // 6:00 - 11:59
    AFTERNOON,  // 12:00 - 17:59
    EVENING,    // 18:00 - 20:59
    NIGHT       // 21:00 - 5:59
}

data class WeatherSuggestionModel(
    val type: SuggestionType,
    val priority: SuggestionPriority,
    val icon: String,
    val args: List<SuggestionArg> = emptyList(),
    val moment: DayMoment = DayMoment.AFTERNOON
)

sealed class SuggestionArg {

    data class Temperature(val value: Int) : SuggestionArg()

    data class Percentage(val value: Int) : SuggestionArg()

    data class Uv(val level: UvIndexLevel) : SuggestionArg()

    data class WindSpeed(val value: Int) : SuggestionArg()

    data class Distance(val value: Int) : SuggestionArg()

    data class Text(val value: String) : SuggestionArg()
}


fun getCurrentDayMoment(timeZone: String): DayMoment {
    val now = Clock.System.now()
    val localHour = now.toLocalDateTime(TimeZone.of(timeZone)).hour
    return when (localHour) {
        in 6..11  -> DayMoment.MORNING
        in 12..17 -> DayMoment.AFTERNOON
        in 18..20 -> DayMoment.EVENING
        else      -> DayMoment.NIGHT
    }
}