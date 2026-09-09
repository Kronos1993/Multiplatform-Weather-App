package com.kronos.multiplatform.weatherapp.domain.usecase.weather

import com.kronos.multiplatform.weatherapp.core.result.Error
import com.kronos.multiplatform.weatherapp.core.result.Result
import com.kronos.multiplatform.weatherapp.domain.model.current.CurrentForecast
import com.kronos.multiplatform.weatherapp.domain.repository.WeatherRemoteRepository

class GetWeatherUseCaseImpl(
    private val repository: WeatherRemoteRepository,
) : GetWeatherUseCase() {
    override suspend fun run(params: Params): Result<CurrentForecast, Error> =
        repository.getWeatherData(params.city, params.lang, params.apiKey)
}
