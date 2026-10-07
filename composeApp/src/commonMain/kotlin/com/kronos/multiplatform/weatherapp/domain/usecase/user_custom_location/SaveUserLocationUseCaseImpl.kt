package com.kronos.multiplatform.weatherapp.domain.usecase.user_custom_location

import com.kronos.multiplatform.weatherapp.domain.model.UserCustomLocation
import com.kronos.multiplatform.weatherapp.domain.repository.UserCustomLocationLocalRepository

class SaveUserLocationUseCaseImpl(
    private val repository: UserCustomLocationLocalRepository,
) : SaveUserLocationUseCase() {
    override suspend fun run(params: Params): UserCustomLocation =
        repository.saveLocation(params.userCustomLocation, params.isCurrent)
}
