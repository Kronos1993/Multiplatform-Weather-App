package com.kronos.multiplatform.weatherapp.domain.usecase.weather

import com.kronos.multiplatform.weatherapp.core.result.Error
import com.kronos.multiplatform.weatherapp.core.result.Result
import com.kronos.multiplatform.weatherapp.core.usecase.UseCase
import com.kronos.multiplatform.weatherapp.domain.model.current.CurrentForecast

abstract class GetWeatherUseCase : UseCase<GetWeatherUseCase.Params, Result<CurrentForecast, Error>>() {
    data class Params(
        val city: String,
        val lang: String,
        val apiKey: String,
    )
}
