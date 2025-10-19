package com.kronos.multiplatform.weatherapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
class ConditionDto(
    val text:String,
    val icon:String,
    val code:String
)