package com.kronos.multiplatform.weatherapp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ForecastDay(
    val forecastDay : List<DailyForecast> = listOf()
)
