package com.kronos.multiplatform.weatherapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ForecastDayDto(
    val forecastday : List<DailyForecastDto>
)
