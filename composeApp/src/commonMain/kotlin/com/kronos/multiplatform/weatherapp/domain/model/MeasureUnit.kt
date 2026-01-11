package com.kronos.multiplatform.weatherapp.domain.model

enum class MeasureUnit(val value: String) {

    INTERNATIONAL("1"),
    IMPERIAL("2");

    companion object {
        fun fromInt(value: Int): MeasureUnit =
            when (value){
                1 -> INTERNATIONAL
                else -> IMPERIAL
            }

        fun from(value: String): MeasureUnit =
            when (value){
                "1" -> INTERNATIONAL
                else -> IMPERIAL
            }
    }
}
