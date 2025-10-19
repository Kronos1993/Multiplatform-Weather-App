package com.kronos.multiplatform.weatherapp.data.local.location

interface LocationDataSource {
    suspend fun getCurrentLocation(): LocationModel?
    suspend fun isLocationEnabled(): Boolean
}