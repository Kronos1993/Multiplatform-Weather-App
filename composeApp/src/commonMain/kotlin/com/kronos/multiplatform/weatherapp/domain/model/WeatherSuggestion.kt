package com.kronos.multiplatform.weatherapp.domain.model


enum class SuggestionPriority { HIGH, MEDIUM, LOW }

enum class SuggestionType {
    RAIN, UV, HEAT, WIND, COLD, VISIBILITY, HUMIDITY, TOMORROW_FORECAST, MORNING_SUMMARY
}
data class WeatherSuggestionModel(
    val type: SuggestionType,
    val priority: SuggestionPriority,
    val icon: String,
    val args: List<SuggestionArg> = emptyList()
)

sealed class SuggestionArg {

    data class Temperature(val value: Int) : SuggestionArg()

    data class Percentage(val value: Int) : SuggestionArg()

    data class Uv(val level: UvIndexLevel) : SuggestionArg()

    data class WindSpeed(val value: Int) : SuggestionArg()

    data class Distance(val value: Int) : SuggestionArg()

    data class Text(val value: String) : SuggestionArg()
}