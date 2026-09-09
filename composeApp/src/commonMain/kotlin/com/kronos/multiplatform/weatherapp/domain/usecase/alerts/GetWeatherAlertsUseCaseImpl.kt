package com.kronos.multiplatform.weatherapp.domain.usecase.alerts

import com.kronos.multiplatform.weatherapp.core.result.Error
import com.kronos.multiplatform.weatherapp.core.result.Result
import com.kronos.multiplatform.weatherapp.domain.model.current.CurrentAlertsForecast
import com.kronos.multiplatform.weatherapp.domain.repository.WeatherAlertsRemoteRepository

class GetWeatherAlertsUseCaseImpl(
    private val repository: WeatherAlertsRemoteRepository,
) : GetWeatherAlertsUseCase() {
    override suspend fun run(params: Params): Result<CurrentAlertsForecast, Error> =
        repository.getWeatherAlertsData(params.lat, params.lon, params.apiKey)
}
