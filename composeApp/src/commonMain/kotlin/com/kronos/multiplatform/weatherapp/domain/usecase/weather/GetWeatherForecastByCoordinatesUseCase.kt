package com.kronos.multiplatform.weatherapp.domain.usecase.weather

import com.kronos.multiplatform.weatherapp.core.result.Error
import com.kronos.multiplatform.weatherapp.core.result.Result
import com.kronos.multiplatform.weatherapp.core.usecase.UseCase
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast

abstract class GetWeatherForecastByCoordinatesUseCase :
    UseCase<GetWeatherForecastByCoordinatesUseCase.Params, Result<Forecast, Error>>() {
    data class Params(
        val lat: Double,
        val lon: Double,
        val lang: String,
        val apiKey: String,
        val days: Int = 1,
    )
}
