package com.kronos.multiplatform.weatherapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class AirQualityDto(
 val co: Double = 0.0,
 val no2: Double = 0.0,
 val o3: Double = 0.0,
 val so2: Double = 0.0,
 val pm2_5: Double = 0.0,
 val pm10: Double = 0.0,
 @SerialName("us-epa-index")
 val usEpaIndex: Int = 0,
 @SerialName("gb-defra-index")
 val gbDefraIndex: Int = 0
)
