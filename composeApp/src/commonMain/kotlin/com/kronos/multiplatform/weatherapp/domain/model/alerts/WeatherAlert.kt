package com.kronos.multiplatform.weatherapp.domain.model.alerts

import kotlinx.serialization.Serializable

@Serializable
data class WeatherAlert(
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
    val description: String? = "",
    val instruction: String? = ""
)