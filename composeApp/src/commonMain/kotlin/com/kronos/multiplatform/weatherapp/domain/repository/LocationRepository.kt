package com.kronos.multiplatform.weatherapp.domain.repository

import com.kronos.multiplatform.weatherapp.data.local.location.LocationModel

interface LocationRepository {

    suspend fun getCurrentLocation(): LocationModel?

    suspend fun isLocationEnabled(): Boolean
}