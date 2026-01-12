package com.kronos.multiplatform.weatherapp.data.repository.alerts

import com.kronos.multiplatform.weatherapp.core.result.Error
import com.kronos.multiplatform.weatherapp.core.result.Result
import com.kronos.multiplatform.weatherapp.data.remote.datasources.WeatherAlertsRemoteDataSource
import com.kronos.multiplatform.weatherapp.domain.model.current.CurrentAlertsForecast
import com.kronos.multiplatform.weatherapp.domain.repository.WeatherAlertsRemoteRepository

class WeatherAlertsRemoteRepositoryImpl(
    private val weatherAlertsRemoteDataSource: WeatherAlertsRemoteDataSource
) : WeatherAlertsRemoteRepository {

    override suspend fun getWeatherAlertsData(
        lat: Double,
        lon: Double,
        apiKey: String
    ): Result<CurrentAlertsForecast, Error> {
        return weatherAlertsRemoteDataSource.getWeatherAlerts(lat,lon, apiKey)
    }
}