package com.kronos.multiplatform.weatherapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LocationDto(
    val name: String,
    val region: String?,
    val country: String,
    val lat: Double,
    val lon: Double,
    val tz_id: String,
    val localtime_epoch: Int,
    val localtime: String,
)
