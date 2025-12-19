package com.kronos.multiplatform.weatherapp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Astro(
    val sunrise: String,
    val sunset: String,
    val moonrise: String,
    val moonset: String,
    val moonPhase: MoonPhase,
    val moonIllumination: String,
    val isMoonUp: Boolean,
    val isSunUp: Boolean,
)
