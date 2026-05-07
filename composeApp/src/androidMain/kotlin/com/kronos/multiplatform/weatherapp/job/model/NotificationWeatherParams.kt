package com.kronos.multiplatform.weatherapp.job.model

import com.kronos.multiplatform.weatherapp.domain.model.MeasureUnit

data class NotificationWeatherParams(
    val lang: String,
    val apiKey: String,
    val days: Int,
    val measureUnit: MeasureUnit
)