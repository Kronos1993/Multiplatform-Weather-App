package com.kronos.multiplatform.weatherapp.data.local.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.kronos.multiplatform.weatherapp.data.local.database.ApplicationDatabase.Companion.MIGRATIONS
import com.kronos.multiplatform.weatherapp.data.local.datasources.dao.UserCustomLocationDao
import com.kronos.multiplatform.weatherapp.data.local.datasources.entity.UserCustomLocationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO


const val DATABASE_NAME = "weather.db"

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SQLiteConnection) {
        database.execSQL(
            """
            ALTER TABLE USER_CUSTOM_LOCATION 
            ADD COLUMN TEMP_F REAL NOT NULL DEFAULT 0.0
            """.trimIndent()
        )
    }
}

@Database(
    entities = [UserCustomLocationEntity::class],
    version = 2,
    exportSchema = false
)
@ConstructedBy(ApplicationDatabaseConstructor::class)
abstract class ApplicationDatabase : RoomDatabase(),DB {
    abstract fun userCustomLocationDao(): UserCustomLocationDao

    override fun clearAllTables() {
        super.clearAllTables()
    }

    companion object {
        val MIGRATIONS = arrayOf(
            MIGRATION_1_2
        )
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
        .addMigrations(*MIGRATIONS)
        .fallbackToDestructiveMigrationOnDowngrade(false)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}