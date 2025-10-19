package com.kronos.multiplatform.weatherapp.data.local.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.kronos.multiplatform.weatherapp.data.local.datasources.dao.UserCustomLocationDao
import com.kronos.multiplatform.weatherapp.data.local.datasources.entity.UserCustomLocationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO


const val DATABASE_NAME = "weather.db"

@Database(
    entities = [UserCustomLocationEntity::class],
    version = 1,
    exportSchema = false
)
@ConstructedBy(ApplicationDatabaseConstructor::class)
abstract class ApplicationDatabase : RoomDatabase(),DB {
    abstract fun userCustomLocationDao(): UserCustomLocationDao

    override fun clearAllTables() {
        super.clearAllTables()
    }
}

interface DB{
    fun clearAllTables():Unit{}
}

// The Room compiler generates the `actual` implementations.
expect object ApplicationDatabaseConstructor : RoomDatabaseConstructor<ApplicationDatabase> {
    override fun initialize(): ApplicationDatabase
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<ApplicationDatabase>
): ApplicationDatabase {
    return builder
        //.addMigrations(MIGRATIONS)
        .fallbackToDestructiveMigrationOnDowngrade(false)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}