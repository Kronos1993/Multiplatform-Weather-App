package com.kronos.multiplatform.weatherapp.data.remote.ktor.util

import com.kronos.multiplatform.weatherapp.core.result.Error


class FullNetworkError(
    val noInternet: NetworkError,
    override val errorMessage: String,
    override val errorCode: Int
) : Error