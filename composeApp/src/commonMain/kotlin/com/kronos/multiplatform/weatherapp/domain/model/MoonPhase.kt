package com.kronos.multiplatform.weatherapp.domain.model

enum class MoonPhase {

    NEW_MOON,
    WAXING_CRESCENT,
    FIRST_QUARTER,
    WAXING_GIBBOUS,
    FULL_MOON,
    WANING_GIBBOUS,
    LAST_QUARTER,
    WANING_CRESCENT;

    companion object {
        fun fromText(text: String?): MoonPhase =
            when (text?.trim()?.lowercase()) {
                "new moon" -> NEW_MOON
                "waxing crescent" -> WAXING_CRESCENT
                "first quarter" -> FIRST_QUARTER
                "waxing gibbous" -> WAXING_GIBBOUS
                "full moon" -> FULL_MOON
                "waning gibbous" -> WANING_GIBBOUS
                "last quarter" -> LAST_QUARTER
                "waning crescent" -> WANING_CRESCENT
                else -> NEW_MOON
            }
    }
}
