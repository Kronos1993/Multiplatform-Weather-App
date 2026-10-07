package com.kronos.multiplatform.weatherapp.domain.usecase.user_custom_location

import com.kronos.multiplatform.weatherapp.domain.model.UserCustomLocation
import com.kronos.multiplatform.weatherapp.domain.repository.UserCustomLocationLocalRepository

class GetSelectedUserLocationUseCaseImpl(
    private val repository: UserCustomLocationLocalRepository,
) : GetSelectedUserLocationUseCase() {
    override suspend fun run(params: Unit): UserCustomLocation? = repository.getSelectedLocation()
}
