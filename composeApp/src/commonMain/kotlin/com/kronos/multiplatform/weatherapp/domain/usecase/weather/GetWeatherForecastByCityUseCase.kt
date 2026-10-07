package com.kronos.multiplatform.weatherapp.domain.usecase.weather

import com.kronos.multiplatform.weatherapp.core.result.Error
import com.kronos.multiplatform.weatherapp.core.result.Result
import com.kronos.multiplatform.weatherapp.core.usecase.UseCase
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast

abstract class GetWeatherForecastByCityUseCase :
    UseCase<GetWeatherForecastByCityUseCase.Params, Result<Forecast, Error>>() {
    data class Params(
        val city: String,
        val lang: String,
        val apiKey: String,
        val days: Int = 1,
    )
}
