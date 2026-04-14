package com.kronos.multiplatform.weatherapp.domain.model


enum class SuggestionPriority { HIGH, MEDIUM, LOW }

enum class SuggestionType {
    RAIN, UV, HEAT, WIND, HUMIDITY, TOMORROW_FORECAST, MORNING_SUMMARY
}
data class WeatherSuggestionModel(
    val type: SuggestionType,
    val priority: SuggestionPriority,
    val icon: String,
    val args: List<String> = emptyList()
)