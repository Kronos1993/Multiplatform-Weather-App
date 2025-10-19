package com.kronos.multiplatform.weatherapp.core.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual class AppPreference : IPreference {
    @OptIn(ExperimentalForeignApi::class)
    override fun createPrefs(): DataStore<Preferences> {
        return createPrefs {
            val directory = NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null
            )
            requireNotNull(directory).path + "/${DATA_STORE_FILE_NAME}"
        }
    }
}