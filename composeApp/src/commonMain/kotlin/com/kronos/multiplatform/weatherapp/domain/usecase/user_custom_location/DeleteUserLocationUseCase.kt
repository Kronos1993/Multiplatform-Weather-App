package com.kronos.multiplatform.weatherapp.domain.usecase.user_custom_location

import com.kronos.multiplatform.weatherapp.core.usecase.UseCase
import com.kronos.multiplatform.weatherapp.domain.model.UserCustomLocation

abstract class DeleteUserLocationUseCase : UseCase<DeleteUserLocationUseCase.Params, Boolean>() {
    data class Params(val userCustomLocation: UserCustomLocation)
}
