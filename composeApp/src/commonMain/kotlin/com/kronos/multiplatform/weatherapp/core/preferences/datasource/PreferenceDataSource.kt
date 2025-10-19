package com.kronos.multiplatform.weatherapp.core.preferences.datasource

interface PreferenceDataSource {

    suspend fun getPreference(key: String, defaultValue: String): String
    suspend fun getPreference(key: String, defaultValue: Int): Int
    suspend fun getPreference(key: String, defaultValue: Boolean): Boolean
    suspend fun getPreference(key: String, defaultValue: Double): Double

    suspend fun setPreference(key: String, value: String)
    suspend fun setPreference(key: String, value: Int)
    suspend fun setPreference(key: String, value: Boolean)
    suspend fun setPreference(key: String, value: Double)

}