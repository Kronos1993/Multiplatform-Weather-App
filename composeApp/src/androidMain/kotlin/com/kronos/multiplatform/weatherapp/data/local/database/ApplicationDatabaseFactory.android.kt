package com.kronos.multiplatform.weatherapp.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

class ApplicationDatabaseFactory(
    private val appContext: Context
) : LocalDatabaseFactory {
    override fun loadLocalDatabase(): RoomDatabase.Builder<ApplicationDatabase> {
        val dataDir = appContext.getExternalFilesDir(null) // Ruta: Android/data/[package_name]/files
        val dbFile = File(dataDir, DATABASE_NAME) // Define la ruta completa de la base de datos

        // Construye la base de datos en la ruta especificada
        return Room.databaseBuilder<ApplicationDatabase>(
            appContext,
            dbFile.absolutePath
        )
    }
}