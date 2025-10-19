package com.kronos.multiplatform.weatherapp.data.repository.weather

import com.kronos.multiplatform.weatherapp.core.result.Error
import com.kronos.multiplatform.weatherapp.core.result.Result
import com.kronos.multiplatform.weatherapp.data.remote.datasources.WeatherRemoteDataSource
import com.kronos.multiplatform.weatherapp.domain.model.current.CurrentForecast
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast
import com.kronos.multiplatform.weatherapp.domain.repository.WeatherRemoteRepository

class WeatherRemoteRepositoryImpl(
    private val weatherRemoteDataSource: WeatherRemoteDataSource
) : WeatherRemoteRepository {

    override suspend fun getWeatherData(
        city: String,
        lang: String,
        apiKey: String
    ): Result<CurrentForecast, Error> {
        return weatherRemoteDataSource.currentWeatherForecast(city, lang, apiKey)
    }

    override suspend fun getWeatherDataForecast(
        city: String,
        lang: String,
        apiKey: String,
        days: Int
    ): Result<Forecast, Error> {
        return weatherRemoteDataSource.weatherForecast(
            city,
            lang,
            apiKey,
            days
        )
    }

    override suspend fun getWeatherDataForecast(
        lat: Double,
        lon: Double,
        lang: String,
        apiKey: String,
        days: Int
    ): Result<Forecast, Error> {
        return weatherRemoteDataSource.weatherForecast(
            lat,
            lon,
            lang,
            apiKey,
            days
        )
    }

}