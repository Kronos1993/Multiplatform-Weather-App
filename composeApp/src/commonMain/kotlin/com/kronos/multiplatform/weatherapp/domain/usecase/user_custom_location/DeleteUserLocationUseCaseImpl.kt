package com.kronos.multiplatform.weatherapp.domain.usecase.user_custom_location

import com.kronos.multiplatform.weatherapp.domain.repository.UserCustomLocationLocalRepository

class DeleteUserLocationUseCaseImpl(
    private val repository: UserCustomLocationLocalRepository,
) : DeleteUserLocationUseCase() {
    override suspend fun run(params: Params): Boolean = repository.delete(params.userCustomLocation)
}
