package com.kronos.multiplatform.weatherapp.domain.usecase.weather

import com.kronos.multiplatform.weatherapp.core.result.Error
import com.kronos.multiplatform.weatherapp.core.result.Result
import com.kronos.multiplatform.weatherapp.domain.repository.WeatherRemoteRepository

class SetLastWeatherForecastUseCaseImpl(
    private val repository: WeatherRemoteRepository,
) : SetLastWeatherForecastUseCase() {
    override suspend fun run(params: Params): Result<Boolean, Error> =
        repository.setLastWeatherForecast(params.prefKey, params.forecast)
}
