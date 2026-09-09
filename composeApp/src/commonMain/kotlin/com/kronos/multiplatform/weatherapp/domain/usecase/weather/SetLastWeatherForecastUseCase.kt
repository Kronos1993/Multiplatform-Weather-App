package com.kronos.multiplatform.weatherapp.domain.usecase.weather

import com.kronos.multiplatform.weatherapp.core.result.Error
import com.kronos.multiplatform.weatherapp.core.result.Result
import com.kronos.multiplatform.weatherapp.core.usecase.UseCase
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast

abstract class SetLastWeatherForecastUseCase : UseCase<SetLastWeatherForecastUseCase.Params, Result<Boolean, Error>>() {
    data class Params(
        val prefKey: String,
        val forecast: Forecast,
    )
}
