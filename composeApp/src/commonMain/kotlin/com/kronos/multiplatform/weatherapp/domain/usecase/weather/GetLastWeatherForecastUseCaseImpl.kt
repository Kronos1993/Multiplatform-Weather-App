package com.kronos.multiplatform.weatherapp.domain.usecase.weather

import com.kronos.multiplatform.weatherapp.core.result.Error
import com.kronos.multiplatform.weatherapp.core.result.Result
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast
import com.kronos.multiplatform.weatherapp.domain.repository.WeatherRemoteRepository

class GetLastWeatherForecastUseCaseImpl(
    private val repository: WeatherRemoteRepository,
) : GetLastWeatherForecastUseCase() {
    override suspend fun run(params: Params): Result<Forecast, Error> =
        repository.getLastWeatherForecast(params.prefKey)
}
