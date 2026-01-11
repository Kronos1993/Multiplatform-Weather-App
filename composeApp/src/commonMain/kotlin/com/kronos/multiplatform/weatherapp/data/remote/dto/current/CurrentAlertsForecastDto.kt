package com.kronos.multiplatform.weatherapp.data.remote.dto.current

import com.kronos.multiplatform.weatherapp.data.remote.dto.AlertsResponseDto
import com.kronos.multiplatform.weatherapp.data.remote.dto.LocationDto
import kotlinx.serialization.Serializable

@Serializable
class CurrentAlertsForecastDto(
    val location: LocationDto,
    val alerts: AlertsResponseDto
)