package com.kronos.multiplatform.weatherapp.domain.model.forecast

import com.kronos.multiplatform.weatherapp.domain.model.CurrentWeather
import com.kronos.multiplatform.weatherapp.domain.model.ForecastDay
import com.kronos.multiplatform.weatherapp.domain.model.Location
import kotlinx.serialization.Serializable

@Serializable
data class Forecast(
    val location: Location = Location(),
    val current: CurrentWeather = CurrentWeather(),
    val forecast: ForecastDay = ForecastDay(),
)
