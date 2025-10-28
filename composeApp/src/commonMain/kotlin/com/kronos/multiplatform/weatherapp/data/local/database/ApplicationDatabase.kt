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

/*val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        // Recrear la tabla para cambiar nullability
        connection.execSQL("CREATE TABLE USER_CUSTOM_LOCATION_NEW (" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "CITY_NAME TEXT NOT NULL, " +
                "TEMP_C REAL NOT NULL DEFAULT 0.0, " +
                "ICON TEXT NOT NULL DEFAULT '', " +
                "IS_CURRENT INTEGER NOT NULL DEFAULT 0, " +
                "IS_SELECTED INTEGER NOT NULL DEFAULT 0, " +
                "LATITUD REAL NOT NULL DEFAULT 0, " +
                "LONGITUD REAL NOT NULL DEFAULT 0" +
                ")")

        // Copiar datos de la tabla vieja a la nueva
        connection.execSQL("INSERT INTO USER_CUSTOM_LOCATION_NEW (ID, CITY_NAME, TEMP_C, ICON, IS_CURRENT, IS_SELECTED, LATITUD, LONGITUD) " +
                "SELECT ID, CITY_NAME, 0.0, '', IS_CURRENT, IS_SELECTED, LATITUD, LONGITUD FROM USER_CUSTOM_LOCATION")

        // Eliminar tabla vieja
        connection.execSQL("DROP TABLE USER_CUSTOM_LOCATION")

        // Renombrar nueva tabla
        connection.execSQL("ALTER TABLE USER_CUSTOM_LOCATION_NEW RENAME TO USER_CUSTOM_LOCATION")
    }
}*/

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

    /*companion object {
        val MIGRATIONS = arrayOf(
            MIGRATION_1_2
        )
    }*/
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
        //.addMigrations(*MIGRATIONS)
        .fallbackToDestructiveMigrationOnDowngrade(false)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}