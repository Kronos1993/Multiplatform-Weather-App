package com.kronos.multiplatform.weatherapp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserCustomLocation(
    var id:Long = 0,
    var cityName:String = "",
    var tempC:Double = 0.0,
    var icon:String = "",
    var isCurrent:Boolean = false,
    var isSelected:Boolean = false,
    var lat:Double? = 0.0,
    var lon:Double? = 0.0
)
