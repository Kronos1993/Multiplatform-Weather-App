package com.kronos.multiplatform.weatherapp.data.remote.dto.forecast

import com.kronos.multiplatform.weatherapp.data.remote.dto.CurrentWeatherDto
import com.kronos.multiplatform.weatherapp.data.remote.dto.ForecastDayDto
import com.kronos.multiplatform.weatherapp.data.remote.dto.LocationDto
import kotlinx.serialization.Serializable

@Serializable
data class ForecastResponseDto(
    val location: LocationDto,
    val current: CurrentWeatherDto,
    val forecast: ForecastDayDto,
)
