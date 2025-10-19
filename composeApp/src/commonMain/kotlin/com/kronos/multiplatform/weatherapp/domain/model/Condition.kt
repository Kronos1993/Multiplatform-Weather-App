package com.kronos.multiplatform.weatherapp.domain.model

import kotlinx.serialization.Serializable

@Serializable
class Condition(
    val description:String = "",
    val icon:String = "",
    val code:String = ""
)