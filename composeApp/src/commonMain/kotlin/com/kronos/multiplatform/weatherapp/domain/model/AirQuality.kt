package com.kronos.multiplatform.weatherapp.domain.model

import kotlinx.serialization.Serializable

@Serializable
class AirQuality(
 val co: Double = 0.0,
 val no2: Double = 0.0,
 val o3: Double = 0.0,
 val so2: Double = 0.0,
 val pm2_5: Double = 0.0,
 val pm10: Double = 0.0,
 val usEpaIndex: Int = 0,
 val gbDefraIndex: Int = 0
)
