package com.kronos.multiplatform.weatherapp.domain.repository

import com.kronos.multiplatform.weatherapp.core.result.Error
import com.kronos.multiplatform.weatherapp.core.result.Result
import com.kronos.multiplatform.weatherapp.domain.model.current.CurrentForecast
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast

interface WeatherRemoteRepository {
    suspend fun getWeatherData(
        city: String,
        lang: String,
        apiKey: String
    ): Result<CurrentForecast, Error>

    suspend fun getWeatherDataForecast(
        city: String,
        lang: String,
        apiKey: String,
        days: Int = 1
    ): Result<Forecast, Error>

    suspend fun getWeatherDataForecast(
        lat: Double,
        lon: Double,
        lang: String,
        apiKey: String,
        days: Int = 1
    ): Result<Forecast, Error>
}
