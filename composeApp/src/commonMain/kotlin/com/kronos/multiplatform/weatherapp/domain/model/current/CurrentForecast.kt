package com.kronos.multiplatform.weatherapp.domain.model.current

import com.kronos.multiplatform.weatherapp.domain.model.CurrentWeather
import com.kronos.multiplatform.weatherapp.domain.model.Location
import kotlinx.serialization.Serializable

@Serializable
class CurrentForecast(
    val location: Location,
    val current: CurrentWeather
)