package com.kronos.multiplatform.weatherapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlertsResponseDto(
    @SerialName("alert")
    val alertList: List<WeatherAlertDto>
)