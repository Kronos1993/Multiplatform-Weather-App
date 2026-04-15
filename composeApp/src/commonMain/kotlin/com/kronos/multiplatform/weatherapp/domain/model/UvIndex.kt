package com.kronos.multiplatform.weatherapp.domain.model

enum class UvIndexLevel {
    LOW, MEDIUM, HIGH, VERY_HIGH, EXTREME
}

fun uvIndexLevel(index: Double): UvIndexLevel = when (index) {
    in 0.0..2.9  -> UvIndexLevel.LOW
    in 3.0..5.9  -> UvIndexLevel.MEDIUM
    in 6.0..7.9  -> UvIndexLevel.HIGH
    in 8.0..10.9 -> UvIndexLevel.VERY_HIGH
    else         -> UvIndexLevel.EXTREME
}