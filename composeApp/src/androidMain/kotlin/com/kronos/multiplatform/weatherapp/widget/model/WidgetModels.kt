package com.kronos.multiplatform.weatherapp.widget.model

data class WeatherWidgetData(
    val location: String,
    val time: String,
    val currentTemp: String,
    val currentCondition: String,
    val humidity: String,
    val windSpeed: String,
    val windDirection: String,
    val uvIndex: String,
    val currentIconUrl: String,
    val day1Name: String,
    val day1IconUrl: String,
    val day2Name: String,
    val day2IconUrl: String
)

data class WeatherParams(
    val lang: String,
    val apiKey: String,
    val days: Int,
    val imageQuality: String
)