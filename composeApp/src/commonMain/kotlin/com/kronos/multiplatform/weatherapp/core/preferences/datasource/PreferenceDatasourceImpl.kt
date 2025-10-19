package com.kronos.multiplatform.weatherapp.core.preferences.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kronos.multiplatform.weatherapp.core.preferences.IPreference
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PreferenceDatasourceImpl(
    private val dataStore: IPreference,
) : PreferenceDataSource {

    private val dataStoreInstance: DataStore<Preferences> by lazy { dataStore.createPrefs() }

    override suspend fun getPreference(key: String, defaultValue: String): String {
        return dataStoreInstance.data
            .map { preferences ->
                preferences[stringPreferencesKey(key)] ?: defaultValue
            }.first()
    }

    override suspend fun getPreference(key: String, defaultValue: Int): Int {
        return dataStoreInstance.data
            .map { preferences ->
                preferences[intPreferencesKey(key)] ?: defaultValue
            }.first()
    }

    override suspend fun getPreference(key: String, defaultValue: Boolean): Boolean {
        return dataStoreInstance.data
            .map { preferences ->
                preferences[booleanPreferencesKey(key)] ?: defaultValue
            }.first()
    }

    override suspend fun getPreference(key: String, defaultValue: Double): Double {
        // Assuming you have a doublePreferencesKey function
        return dataStoreInstance.data
            .map { preferences ->
                preferences[doublePreferencesKey(key)] ?: defaultValue
            }.first()
    }

    override suspend fun setPreference(key: String, value: String) {
        dataStoreInstance.edit { preferences ->
            preferences[stringPreferencesKey(key)] = value
        }
    }

    override suspend fun setPreference(key: String, value: Int) {
        dataStoreInstance.edit { preferences ->
            preferences[intPreferencesKey(key)] = value
        }
    }

    override suspend fun setPreference(key: String, value: Boolean) {
        dataStoreInstance.edit { preferences ->
            preferences[booleanPreferencesKey(key)] = value
        }
    }

    override suspend fun setPreference(key: String, value: Double) {
        dataStoreInstance.edit { preferences ->
            preferences[doublePreferencesKey(key)] = value
        }
    }
}
