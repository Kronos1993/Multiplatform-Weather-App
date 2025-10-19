package com.kronos.multiplatform.weatherapp.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

actual class AppPreference(private val context: Context) : IPreference {
    override fun createPrefs(): DataStore<Preferences> {
        return createPrefs {
            context.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath
        }
    }
}