package com.kronos.multiplatform.weatherapp.data.local.location

data class LocationModel(
    val latitude: Double,
    val longitude: Double,
    val cityName: String? = null,
    val temp: Double? = null,
    val icon: String? = null,
    val current: Boolean = false,
)