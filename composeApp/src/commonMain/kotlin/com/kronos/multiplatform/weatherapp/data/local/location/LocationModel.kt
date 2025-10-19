package com.kronos.multiplatform.weatherapp.data.local.location

data class LocationModel(
    val latitude: Double,
    val longitude: Double,
    val cityName: String? = null
)