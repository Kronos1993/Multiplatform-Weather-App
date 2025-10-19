package com.kronos.multiplatform.weatherapp.data.local.datasources.dao

import androidx.room.*
import com.kronos.multiplatform.weatherapp.data.local.datasources.entity.UserCustomLocationEntity

@Dao
interface UserCustomLocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(userCustomLocationEntity: UserCustomLocationEntity)

    @Query("SELECT * FROM USER_CUSTOM_LOCATION")
    suspend fun listAll(): List<UserCustomLocationEntity>

    @Query("SELECT * FROM USER_CUSTOM_LOCATION WHERE IS_CURRENT = 1")
    suspend fun getCurrentLocation(): UserCustomLocationEntity?

    @Query("SELECT * FROM USER_CUSTOM_LOCATION WHERE IS_SELECTED = 1")
    suspend fun getSelectedLocation(): UserCustomLocationEntity?

    @Query("UPDATE USER_CUSTOM_LOCATION SET IS_SELECTED = 0 WHERE IS_SELECTED = 1")
    suspend fun cleanSelectedLocation()

    @Delete
    suspend fun deleteEvent(userCustomLocationEntity: UserCustomLocationEntity)


}