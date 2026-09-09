package com.kronos.multiplatform.weatherapp.domain.usecase.alerts

import com.kronos.multiplatform.weatherapp.core.result.Error
import com.kronos.multiplatform.weatherapp.core.result.Result
import com.kronos.multiplatform.weatherapp.core.usecase.UseCase
import com.kronos.multiplatform.weatherapp.domain.model.current.CurrentAlertsForecast

abstract class GetWeatherAlertsUseCase : UseCase<GetWeatherAlertsUseCase.Params, Result<CurrentAlertsForecast, Error>>() {
    data class Params(
        val lat: Double,
        val lon: Double,
        val apiKey: String,
    )
}
