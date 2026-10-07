package com.kronos.multiplatform.weatherapp.domain.usecase.weather

import com.kronos.multiplatform.weatherapp.core.result.Error
import com.kronos.multiplatform.weatherapp.core.result.Result
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast
import com.kronos.multiplatform.weatherapp.domain.repository.WeatherRemoteRepository

class GetWeatherForecastByCoordinatesUseCaseImpl(
    private val repository: WeatherRemoteRepository,
) : GetWeatherForecastByCoordinatesUseCase() {
    override suspend fun run(params: Params): Result<Forecast, Error> =
        repository.getWeatherDataForecast(params.lat, params.lon, params.lang, params.apiKey, params.days)
}
