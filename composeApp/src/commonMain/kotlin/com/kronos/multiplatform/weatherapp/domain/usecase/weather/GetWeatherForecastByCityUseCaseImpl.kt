package com.kronos.multiplatform.weatherapp.domain.usecase.weather

import com.kronos.multiplatform.weatherapp.core.result.Error
import com.kronos.multiplatform.weatherapp.core.result.Result
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast
import com.kronos.multiplatform.weatherapp.domain.repository.WeatherRemoteRepository

class GetWeatherForecastByCityUseCaseImpl(
    private val repository: WeatherRemoteRepository,
) : GetWeatherForecastByCityUseCase() {
    override suspend fun run(params: Params): Result<Forecast, Error> =
        repository.getWeatherDataForecast(params.city, params.lang, params.apiKey, params.days)
}
