package com.kronos.multiplatform.weatherapp.domain.usecase.location

import com.kronos.multiplatform.weatherapp.data.local.location.LocationModel
import com.kronos.multiplatform.weatherapp.domain.repository.LocationRepository

class GetCurrentLocationUseCaseImpl(
    private val repository: LocationRepository,
) : GetCurrentLocationUseCase() {
    override suspend fun run(params: Unit): LocationModel? = repository.getCurrentLocation()
}
