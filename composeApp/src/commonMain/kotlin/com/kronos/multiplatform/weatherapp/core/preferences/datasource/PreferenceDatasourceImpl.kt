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
import kotlin.coroutines.cancellation.CancellationException

class PreferenceDatasourceImpl(
    private val dataStore: IPreference,
) : PreferenceDataSource {

    private val dataStoreInstance: DataStore<Preferences> by lazy { dataStore.createPrefs() }

    override suspend fun getPreference(key: String, defaultValue: String): String {
        return dataStoreInstance.data
            .map { preferences ->
                try {
                    preferences[stringPreferencesKey(key)] ?: defaultValue
                } catch (e: Exception) {
                    e.printStackTrace()
                    if (e is CancellationException) throw e
                    defaultValue
                }
            }.first()
    }

    override suspend fun getPreference(key: String, defaultValue: Int): Int {
        return dataStoreInstance.data.map { preferences ->
            try {
                preferences[intPreferencesKey(key)] ?: defaultValue
            } catch (e: ClassCastException) {
                e.printStackTrace()
                val stringValue = preferences[stringPreferencesKey(key)]
                stringValue?.toIntOrNull() ?: defaultValue
            } catch (e: Exception) {
                e.printStackTrace()
                if (e is CancellationException) throw e
                defaultValue
            }
        }.first()
    }

    override suspend fun getPreference(key: String, defaultValue: Boolean): Boolean {
        return dataStoreInstance.data
            .map { preferences ->
                try {
                    preferences[booleanPreferencesKey(key)] ?: defaultValue
                } catch (e: ClassCastException) {
                    e.printStackTrace()
                    val stringValue = preferences[stringPreferencesKey(key)]
                    if (stringValue != null) {
                        stringValue.toBoolean()
                    } else {
                        defaultValue
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    if (e is CancellationException) throw e
                    defaultValue
                }
            }.first()
    }

    override suspend fun getPreference(key: String, defaultValue: Double): Double {
        return dataStoreInstance.data
            .map { preferences ->
                try {
                    preferences[doublePreferencesKey(key)] ?: defaultValue
                } catch (e: ClassCastException) {
                    e.printStackTrace()
                    val stringValue = preferences[stringPreferencesKey(key)]
                    stringValue?.toDoubleOrNull() ?: defaultValue
                } catch (e: Exception) {
                    e.printStackTrace()
                    if (e is CancellationException) throw e
                    defaultValue
                }
            }.first()
    }

    override suspend fun setPreference(key: String, value: String) {
        dataStoreInstance.edit { preferences ->
            preferences[stringPreferencesKey(key)] = value
        }
    }

    override suspend fun setPreference(key: String, value: Int) {
        dataStoreInstance.edit { preferences ->
            preferences.remove(stringPreferencesKey(key))
            preferences[intPreferencesKey(key)] = value
        }
    }

    override suspend fun setPreference(key: String, value: Boolean) {
        dataStoreInstance.edit { preferences ->
            preferences.remove(stringPreferencesKey(key))
            preferences[booleanPreferencesKey(key)] = value
        }
    }

    override suspend fun setPreference(key: String, value: Double) {
        dataStoreInstance.edit { preferences ->
            preferences.remove(stringPreferencesKey(key))
            preferences[doublePreferencesKey(key)] = value
        }
    }
}
