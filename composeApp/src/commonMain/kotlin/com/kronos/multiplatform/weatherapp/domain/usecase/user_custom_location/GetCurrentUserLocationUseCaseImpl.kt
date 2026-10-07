package com.kronos.multiplatform.weatherapp.domain.usecase.user_custom_location

import com.kronos.multiplatform.weatherapp.domain.model.UserCustomLocation
import com.kronos.multiplatform.weatherapp.domain.repository.UserCustomLocationLocalRepository

class GetCurrentUserLocationUseCaseImpl(
    private val repository: UserCustomLocationLocalRepository,
) : GetCurrentUserLocationUseCase() {
    override suspend fun run(params: Unit): UserCustomLocation? = repository.getCurrentLocation()
}
