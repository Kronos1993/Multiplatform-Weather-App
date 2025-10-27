package com.kronos.multiplatform.weatherapp.data.remote.datasources

import com.kronos.multiplatform.weatherapp.domain.model.current.CurrentForecast
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast
import com.kronos.multiplatform.weatherapp.core.result.Result
import com.kronos.multiplatform.weatherapp.core.result.Error

interface WeatherRemoteDataSource {

    suspend fun currentWeatherForecast(
        q: String, lang: String, apiKey: String
    ): Result<CurrentForecast, Error>


    suspend fun weatherForecast(
        q: String, lang: String, apiKey: String, days: Int = 1
    ): Result<Forecast, Error>

    suspend fun weatherForecast(
        lat: Double,
        lon: Double,
        lang: String,
        apiKey: String,
        days: Int = 1
    ): Result<Forecast, Error>

    suspend fun getLastWeatherForecast(
        prefKey: String,
    ): Result<Forecast, Error>

    suspend fun setLastWeatherForecast(
        prefKey: String,
        forecast: Forecast,
    ): Result<Boolean, Error>
}
