package com.kronos.multiplatform.weatherapp.data.remote.ktor

import kotlinx.serialization.Serializable

@Serializable
data class ResponseError(
    var code:Int = 0,
    var message:String = ""
)
