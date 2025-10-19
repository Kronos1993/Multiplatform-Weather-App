package com.kronos.multiplatform.weatherapp.data.repository.user_custom_location

import com.kronos.multiplatform.weatherapp.data.local.datasources.UserCustomLocationLocalDataSource
import com.kronos.multiplatform.weatherapp.domain.model.UserCustomLocation
import com.kronos.multiplatform.weatherapp.domain.repository.UserCustomLocationLocalRepository

class UserCustomLocationLocalRepositoryImpl(
    private val userCustomLocationLocalDataSource: UserCustomLocationLocalDataSource
) : UserCustomLocationLocalRepository {

    override suspend fun getSelectedLocation(): UserCustomLocation? {
        return userCustomLocationLocalDataSource.getSelectedLocation()
    }

    override suspend fun getCurrentLocation(): UserCustomLocation? {
        return userCustomLocationLocalDataSource.getCurrentLocation()
    }

    override suspend fun saveLocation(
        userCustomLocation: UserCustomLocation,
        isCurrent: Boolean
    ): UserCustomLocation {
        return userCustomLocationLocalDataSource.saveLocation(userCustomLocation, isCurrent)
    }

    override suspend fun listAll(): List<UserCustomLocation> {
        return userCustomLocationLocalDataSource.listAll()
    }

    override suspend fun delete(userCustomLocation: UserCustomLocation): Boolean {
        return userCustomLocationLocalDataSource.delete(userCustomLocation)
    }

}