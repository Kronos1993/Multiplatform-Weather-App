package com.kronos.multiplatform.weatherapp.core.notification

enum class NotificationType {
    WEATHER_UPDATED,
    WEATHER_ALERT,
    FROM_FIREBASE,
    WEATHER_SUGGESTION_MORNING,
    WEATHER_SUGGESTION_MIDDAY,
    WEATHER_SUGGESTION_EVENING;

    companion object {
        fun from(value: String): NotificationType? =
            when (value) {
                WEATHER_UPDATED.name -> WEATHER_UPDATED
                WEATHER_ALERT.name -> WEATHER_ALERT
                FROM_FIREBASE.name -> FROM_FIREBASE
                WEATHER_SUGGESTION_MORNING.name -> WEATHER_SUGGESTION_MORNING
                WEATHER_SUGGESTION_MIDDAY.name -> WEATHER_SUGGESTION_MIDDAY
                WEATHER_SUGGESTION_EVENING.name -> WEATHER_SUGGESTION_EVENING
                else -> null
            }
    }
}