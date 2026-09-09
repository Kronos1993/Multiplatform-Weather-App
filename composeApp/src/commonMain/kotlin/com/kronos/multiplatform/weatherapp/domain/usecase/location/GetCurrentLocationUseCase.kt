package com.kronos.multiplatform.weatherapp.domain.usecase.location

import com.kronos.multiplatform.weatherapp.core.usecase.UseCase
import com.kronos.multiplatform.weatherapp.data.local.location.LocationModel

abstract class GetCurrentLocationUseCase : UseCase<Unit, LocationModel?>()
