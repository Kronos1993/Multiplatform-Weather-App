package com.kronos.multiplatform.weatherapp.core.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

interface IPreference {
    fun createPrefs(): DataStore<Preferences>
}