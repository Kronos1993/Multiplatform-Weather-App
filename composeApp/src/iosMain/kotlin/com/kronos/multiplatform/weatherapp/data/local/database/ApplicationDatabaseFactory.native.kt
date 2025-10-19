package com.kronos.multiplatform.weatherapp.data.local.database

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask


class ApplicationDatabaseFactory : LocalDatabaseFactory {
    override fun loadLocalDatabase(): RoomDatabase.Builder<ApplicationDatabase> {
        val dbFilePath = documentDirectory() + "/$DATABASE_NAME"
        return Room.databaseBuilder<ApplicationDatabase>(
            name = dbFilePath
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun documentDirectory(): String {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        return requireNotNull(documentDirectory?.path)
    }
}