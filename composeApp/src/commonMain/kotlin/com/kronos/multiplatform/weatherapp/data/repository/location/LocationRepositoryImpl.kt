package com.kronos.multiplatform.weatherapp.data.repository.location

import com.kronos.multiplatform.weatherapp.data.local.location.LocationDataSource
import com.kronos.multiplatform.weatherapp.data.local.location.LocationModel
import com.kronos.multiplatform.weatherapp.domain.repository.LocationRepository

class LocationRepositoryImpl(
    private val locationDataSource: LocationDataSource
) : LocationRepository {

    override suspend fun getCurrentLocation(): LocationModel? {
        return locationDataSource.getCurrentLocation()
    }

    override suspend fun isLocationEnabled(): Boolean {
        return locationDataSource.isLocationEnabled()
    }

}