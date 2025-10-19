package com.kronos.multiplatform.weatherapp.data.local.datasources

import com.kronos.multiplatform.weatherapp.data.local.database.LocalDatabaseFactory
import com.kronos.multiplatform.weatherapp.data.local.database.getRoomDatabase
import com.kronos.multiplatform.weatherapp.data.local.datasources.mapper.toDomain
import com.kronos.multiplatform.weatherapp.data.local.datasources.mapper.toEntity
import com.kronos.multiplatform.weatherapp.domain.model.UserCustomLocation

class UserCustomLocationLocalDataSourceImpl(
    private val databaseFactory: LocalDatabaseFactory,
) : UserCustomLocationLocalDataSource {


    override suspend fun listAll(): List<UserCustomLocation> {
        var result = listOf<UserCustomLocation>()

        try {
            val internalDb = getRoomDatabase(databaseFactory.loadLocalDatabase())
            result = internalDb.userCustomLocationDao().listAll().map {
                it.toDomain()
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return result
    }

    override suspend fun getSelectedLocation(): UserCustomLocation? {
        var result:UserCustomLocation? = UserCustomLocation()

        try {
            val internalDb = getRoomDatabase(databaseFactory.loadLocalDatabase())
            result = internalDb.userCustomLocationDao().getSelectedLocation().let{
                it?.toDomain()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return result
    }

    override suspend fun getCurrentLocation(): UserCustomLocation? {
        var result:UserCustomLocation? = UserCustomLocation()

        try {
            val internalDb = getRoomDatabase(databaseFactory.loadLocalDatabase())
            result = internalDb.userCustomLocationDao().getCurrentLocation().let{
                it?.toDomain()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return result
    }

    override suspend fun saveLocation(
        userCustomLocation: UserCustomLocation,
        isCurrent: Boolean
    ): UserCustomLocation {
        try {
            val entity = userCustomLocation.toEntity()
            val internalDb = getRoomDatabase(databaseFactory.loadLocalDatabase())
            internalDb.userCustomLocationDao().cleanSelectedLocation()
            internalDb.userCustomLocationDao().insertOrUpdate(entity)

        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return userCustomLocation
    }

    override suspend fun delete(userCustomLocation: UserCustomLocation): Boolean {
        var deleted = false
        try {
            val entity = userCustomLocation.toEntity()

            val internalDb = getRoomDatabase(databaseFactory.loadLocalDatabase())
            internalDb.userCustomLocationDao().deleteEvent(entity)
            deleted = true
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return deleted
    }

}
