package com.kronos.multiplatform.weatherapp.domain.usecase.location

import com.kronos.multiplatform.weatherapp.domain.repository.LocationRepository

class IsLocationEnabledUseCaseImpl(
    private val repository: LocationRepository,
) : IsLocationEnabledUseCase() {
    override suspend fun run(params: Unit): Boolean = repository.isLocationEnabled()
}
