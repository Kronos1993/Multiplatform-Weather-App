package com.kronos.multiplatform.weatherapp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val name: String = "",
    val region: String? = "",
    val country: String= "",
    val lat: Double= 0.0,
    val lon: Double= 0.0,
    val tzId: String= "",
    val localtimeEpoch: Int= -1,
    val localtime: String= "",
)
