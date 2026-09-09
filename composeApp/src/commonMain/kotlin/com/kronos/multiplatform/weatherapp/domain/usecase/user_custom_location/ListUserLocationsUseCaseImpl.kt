package com.kronos.multiplatform.weatherapp.domain.usecase.user_custom_location

import com.kronos.multiplatform.weatherapp.domain.model.UserCustomLocation
import com.kronos.multiplatform.weatherapp.domain.repository.UserCustomLocationLocalRepository

class ListUserLocationsUseCaseImpl(
    private val repository: UserCustomLocationLocalRepository,
) : ListUserLocationsUseCase() {
    override suspend fun run(params: Unit): List<UserCustomLocation> = repository.listAll()
}
