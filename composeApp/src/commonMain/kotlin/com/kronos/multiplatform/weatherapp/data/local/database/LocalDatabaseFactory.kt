package com.kronos.multiplatform.weatherapp.data.local.database

import androidx.room.RoomDatabase


interface LocalDatabaseFactory {
    fun  loadLocalDatabase(): RoomDatabase.Builder<ApplicationDatabase>
}