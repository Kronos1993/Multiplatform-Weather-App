package com.kronos.multiplatform.weatherapp.domain.repository

import com.kronos.multiplatform.weatherapp.core.result.Error
import com.kronos.multiplatform.weatherapp.core.result.Result
import com.kronos.multiplatform.weatherapp.domain.model.current.CurrentAlertsForecast

interface WeatherAlertsRemoteRepository {
    suspend fun getWeatherAlertsData(
        lat: Double,
        lon: Double,
        apiKey: String
    ): Result<CurrentAlertsForecast, Error>

}
