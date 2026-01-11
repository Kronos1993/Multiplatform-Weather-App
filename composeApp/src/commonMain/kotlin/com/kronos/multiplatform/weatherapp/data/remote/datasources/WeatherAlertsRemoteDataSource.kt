package com.kronos.multiplatform.weatherapp.data.remote.datasources

import com.kronos.multiplatform.weatherapp.core.result.Error
import com.kronos.multiplatform.weatherapp.core.result.Result
import com.kronos.multiplatform.weatherapp.domain.model.current.CurrentAlertsForecast

interface WeatherAlertsRemoteDataSource {

    suspend fun getWeatherAlerts(
        cityName: String, apiKey: String
    ): Result<CurrentAlertsForecast, Error>
}
