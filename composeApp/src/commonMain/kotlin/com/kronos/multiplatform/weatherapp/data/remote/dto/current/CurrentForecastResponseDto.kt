package com.kronos.multiplatform.weatherapp.data.remote.dto.current

import com.kronos.multiplatform.weatherapp.data.remote.dto.CurrentWeatherDto
import com.kronos.multiplatform.weatherapp.data.remote.dto.LocationDto
import kotlinx.serialization.Serializable

@Serializable
class CurrentForecastResponseDto(
    val location: LocationDto,
    val current: CurrentWeatherDto
)