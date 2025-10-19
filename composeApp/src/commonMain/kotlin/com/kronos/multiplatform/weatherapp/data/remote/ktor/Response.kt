package com.kronos.multiplatform.weatherapp.data.remote.ktor

import kotlinx.serialization.Serializable

@Serializable
data class Response<I, O>(
    var response: I? = null,
    var code: Int = -1,
    var error: List<O> = listOf(),
    var limit: Int? = 25,
    var offset: Int? = 0,
    val total_count: Long? = 0
)
