package com.kronos.multiplatform.weatherapp.domain.model.current

import com.kronos.multiplatform.weatherapp.domain.model.Location
import com.kronos.multiplatform.weatherapp.domain.model.alerts.WeatherAlert
import kotlinx.serialization.Serializable

@Serializable
class CurrentAlertsForecast(
    val location: Location,
    val alerts: List<WeatherAlert>
)