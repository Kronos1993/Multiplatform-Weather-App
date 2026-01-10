package com.kronos.multiplatform.weatherapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherAlertDto(
    val identifier: String? = "",
    val headline: String? = "",
    val msgtype: String? = "",
    val severity: String? = "",
    val urgency: String? = "",
    val areas: String? = "",
    val category: String? = "",
    val certainty: String? = "",
    val event: String? = "",
    val note: String? = "",
    val effective: String? = "",
    val expires: String? = "",
    @SerialName("desc")
    val description: String? = "",
    val instruction: String? = ""
)