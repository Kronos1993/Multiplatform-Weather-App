package com.kronos.multiplatform.weatherapp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DailyForecast(
    val date: String,
    val dateEpoch: Long,
    val day: Day,
    val astro: Astro,
    val hours: List<Hour>,
)
