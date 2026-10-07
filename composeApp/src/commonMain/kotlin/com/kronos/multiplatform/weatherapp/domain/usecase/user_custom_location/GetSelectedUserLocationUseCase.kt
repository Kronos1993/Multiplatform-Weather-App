package com.kronos.multiplatform.weatherapp.domain.usecase.user_custom_location

import com.kronos.multiplatform.weatherapp.core.usecase.UseCase
import com.kronos.multiplatform.weatherapp.domain.model.UserCustomLocation

abstract class GetSelectedUserLocationUseCase : UseCase<Unit, UserCustomLocation?>()
