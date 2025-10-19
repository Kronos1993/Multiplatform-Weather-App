package com.kronos.multiplatform.weatherapp.core.preferences.repository

import com.kronos.multiplatform.weatherapp.core.preferences.datasource.PreferenceDataSource

class PreferenceRepositoryImpl (
    private val preferenceDataSource: PreferenceDataSource
) : PreferenceRepository {
    override suspend fun getPreference(key: String, defaultValue: String): String {
        return preferenceDataSource.getPreference(key, defaultValue)
    }

    override suspend fun getPreference(key: String, defaultValue: Int): Int {
        return preferenceDataSource.getPreference(key, defaultValue)
    }

    override suspend fun getPreference(key: String, defaultValue: Boolean): Boolean {
        return preferenceDataSource.getPreference(key, defaultValue)
    }

    override suspend fun getPreference(key: String, defaultValue: Double): Double {
        return preferenceDataSource.getPreference(key, defaultValue)
    }

    override suspend fun setPreference(key: String, value: String) {
        preferenceDataSource.setPreference(key, value)
    }

    override suspend fun setPreference(key: String, value: Int) {
        preferenceDataSource.setPreference(key, value)
    }

    override suspend fun setPreference(key: String, value: Boolean) {
        preferenceDataSource.setPreference(key, value)
    }

    override suspend fun setPreference(key: String, value: Double) {
        preferenceDataSource.setPreference(key, value)
    }

}