package com.kronos.multiplatform.weatherapp.data.remote.ktor.util

import com.kronos.multiplatform.weatherapp.core.result.Error

enum class NetworkError(
    override val errorMessage: String,
    override val errorCode: Int
) : Error {
    REQUEST_TIMEOUT("Request Timeout", 408),
    UNAUTHORIZED("Unauthorized", 401),
    CONFLICT("Conflict", 409),
    TOO_MANY_REQUESTS("Too many requests", 429),
    NO_INTERNET("No internet connection", 0),
    PAYLOAD_TOO_LARGE("Payload too large", 413),
    SERVER_ERROR("Server error", 500),
    SERIALIZATION("Serialization error", 422),
    UNKNOWN("Unknown error", 520);
}
