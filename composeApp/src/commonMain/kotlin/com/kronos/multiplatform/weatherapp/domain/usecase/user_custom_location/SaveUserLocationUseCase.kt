package com.kronos.multiplatform.weatherapp.domain.usecase.user_custom_location

import com.kronos.multiplatform.weatherapp.core.usecase.UseCase
import com.kronos.multiplatform.weatherapp.domain.model.UserCustomLocation

abstract class SaveUserLocationUseCase : UseCase<SaveUserLocationUseCase.Params, UserCustomLocation>() {
    data class Params(
        val userCustomLocation: UserCustomLocation,
        val isCurrent: Boolean = false,
    )
}
