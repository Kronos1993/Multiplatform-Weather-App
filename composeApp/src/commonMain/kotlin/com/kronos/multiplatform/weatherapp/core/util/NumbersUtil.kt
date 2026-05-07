package com.kronos.multiplatform.weatherapp.core.util

import kotlin.math.round

fun Double.roundTo2Decimals(): Double {
    val factor = 100.0
    return round((this + 1e-10) * factor) / factor
}
