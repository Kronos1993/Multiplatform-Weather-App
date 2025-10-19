package com.kronos.multiplatform.weatherapp.data.remote.ktor.util

import com.kronos.multiplatform.weatherapp.core.result.Error


class FullNetworkError(var noInternet: NetworkError, var errorMessage: String, var errorCode: Int) : Error