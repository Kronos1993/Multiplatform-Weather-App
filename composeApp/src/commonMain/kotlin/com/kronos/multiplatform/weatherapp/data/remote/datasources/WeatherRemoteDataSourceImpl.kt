package com.kronos.multiplatform.weatherapp.data.remote.datasources

import com.kronos.multiplatform.weatherapp.core.result.Error
import com.kronos.multiplatform.weatherapp.core.result.Result
import com.kronos.multiplatform.weatherapp.data.mapper.toCurrentForecast
import com.kronos.multiplatform.weatherapp.data.mapper.toForecast
import com.kronos.multiplatform.weatherapp.data.remote.api.WeatherApi
import com.kronos.multiplatform.weatherapp.data.remote.dto.current.CurrentForecastResponseDto
import com.kronos.multiplatform.weatherapp.data.remote.dto.forecast.ForecastResponseDto
import com.kronos.multiplatform.weatherapp.data.remote.ktor.KtorClientFactory
import com.kronos.multiplatform.weatherapp.data.remote.ktor.KtorEngineFactory
import com.kronos.multiplatform.weatherapp.data.remote.ktor.Response
import com.kronos.multiplatform.weatherapp.data.remote.ktor.ResponseError
import com.kronos.multiplatform.weatherapp.data.remote.ktor.UrlProvider
import com.kronos.multiplatform.weatherapp.data.remote.ktor.util.FullNetworkError
import com.kronos.multiplatform.weatherapp.data.remote.ktor.util.NetworkError
import com.kronos.multiplatform.weatherapp.domain.model.current.CurrentForecast
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast
import io.ktor.client.call.body
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class WeatherRemoteDataSourceImpl(
    private val urlProvider: UrlProvider,
    private val httpClient: KtorClientFactory,
    private val httpEngine: KtorEngineFactory,
) : WeatherRemoteDataSource {


    override suspend fun currentWeatherForecast(
        q: String, lang: String, apiKey: String
    ): Result<CurrentForecast, Error> {
        val response =
            try {
                httpClient.createKtorClient(httpEngine)
                    .get(urlProvider.getPrivateApiUrl() + WeatherApi.GET_CURRENT_WEATHER) {
                        parameter("q", q)
                        parameter("lang", lang)
                        parameter("apiKey", apiKey)
                    }
            } catch (e: UnresolvedAddressException) {
                e.printStackTrace()
                return Result.Error(
                    FullNetworkError(
                        NetworkError.NO_INTERNET,
                        "No internet connection",
                        0
                    )
                )
            } catch (e: SerializationException) {
                e.printStackTrace()
                return Result.Error(
                    FullNetworkError(
                        NetworkError.NO_INTERNET,
                        "No internet connection",
                        0
                    )
                )
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
                return Result.Error(
                    FullNetworkError(
                        NetworkError.NO_INTERNET,
                        "No internet connection",
                        0
                    )
                )
            }

        return when (response.status.value) {
            in 200..299 -> {
                val result: String = response.body<String>()
                if (result.isNotEmpty()) {
                    try {
                        val json = Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                            allowSpecialFloatingPointValues = true
                        }
                        val list =
                            json.decodeFromString<Response<CurrentForecastResponseDto, ResponseError>>(
                                result
                            )
                        if (list.response != null) {
                            Result.Success(list.response!!.toCurrentForecast())
                        } else {
                            Result.Error(
                                FullNetworkError(
                                    NetworkError.SERIALIZATION,
                                    if (list.error.isNotEmpty()) list.error.first().message else NetworkError.SERIALIZATION.name,
                                    if (list.error.isNotEmpty()) list.error.first().code else 409
                                )
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Result.Error(
                            FullNetworkError(
                                NetworkError.SERIALIZATION,
                                "Serialization error",
                                0
                            )
                        )
                    }
                } else {
                    Result.Error(
                        FullNetworkError(
                            NetworkError.SERIALIZATION,
                            "Serialization error",
                            0
                        )
                    )
                }

            }

            400 -> {
                val result: String = response.body<String>()
                if (result.isNotEmpty()) {
                    try {
                        val json = Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                            allowSpecialFloatingPointValues = true
                        }
                        val list =
                            json.decodeFromString<Response<CurrentForecastResponseDto, ResponseError>>(
                                result
                            )
                        Result.Error(
                            FullNetworkError(
                                NetworkError.SERIALIZATION,
                                if (list.error.isNotEmpty()) list.error.first().message else NetworkError.SERIALIZATION.name,
                                if (list.error.isNotEmpty()) list.error.first().code else 409
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Result.Error(
                            FullNetworkError(
                                NetworkError.SERIALIZATION,
                                "Serialization error",
                                0
                            )
                        )
                    }
                } else {
                    Result.Error(
                        FullNetworkError(
                            NetworkError.SERIALIZATION,
                            "Serialization error",
                            0
                        )
                    )
                }
            }

            401 -> Result.Error(
                FullNetworkError(
                    NetworkError.UNAUTHORIZED,
                    "UNAUTHORIZED",
                    401
                )
            )

            409 -> Result.Error(
                FullNetworkError(
                    NetworkError.CONFLICT,
                    "CONFLICT",
                    409
                )
            )

            408 -> Result.Error(
                FullNetworkError(
                    NetworkError.REQUEST_TIMEOUT,
                    "CONFLICT",
                    408
                )
            )

            413 -> Result.Error(
                FullNetworkError(
                    NetworkError.PAYLOAD_TOO_LARGE,
                    "PAYLOAD TOO LARGE",
                    413
                )
            )

            in 500..599 -> Result.Error(
                FullNetworkError(
                    NetworkError.SERVER_ERROR,
                    "SERVER ERROR",
                    response.status.value
                )
            )

            else -> Result.Error(
                FullNetworkError(
                    NetworkError.UNKNOWN,
                    "UNKNOWN",
                    response.status.value
                )
            )
        }
    }

    override suspend fun weatherForecast(
        q: String, lang: String, apiKey: String, days: Int
    ): Result<Forecast, Error> {
        val response =
            try {
                httpClient.createKtorClient(httpEngine)
                    .get(urlProvider.getPrivateApiUrl() + WeatherApi.GET_WEATHER_FORECAST) {
                        parameter("q", q)
                        parameter("lang", lang)
                        parameter("apiKey", apiKey)
                        parameter("days", days)
                    }
            } catch (e: UnresolvedAddressException) {
                e.printStackTrace()
                return Result.Error(
                    FullNetworkError(
                        NetworkError.NO_INTERNET,
                        "No internet connection",
                        0
                    )
                )
            } catch (e: SerializationException) {
                e.printStackTrace()
                return Result.Error(
                    FullNetworkError(
                        NetworkError.NO_INTERNET,
                        "No internet connection",
                        0
                    )
                )
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
                return Result.Error(
                    FullNetworkError(
                        NetworkError.NO_INTERNET,
                        "No internet connection",
                        0
                    )
                )
            }

        return when (response.status.value) {
            in 200..299 -> {
                val result: String = response.body<String>()
                if (result.isNotEmpty()) {
                    try {
                        val json = Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                            allowSpecialFloatingPointValues = true
                        }
                        val list =
                            json.decodeFromString<Response<ForecastResponseDto, ResponseError>>(
                                result
                            )
                        if (list.response != null) {
                            Result.Success(list.response!!.toForecast())
                        } else {
                            Result.Error(
                                FullNetworkError(
                                    NetworkError.SERIALIZATION,
                                    if (list.error.isNotEmpty()) list.error.first().message else NetworkError.SERIALIZATION.name,
                                    if (list.error.isNotEmpty()) list.error.first().code else 409
                                )
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Result.Error(
                            FullNetworkError(
                                NetworkError.SERIALIZATION,
                                "Serialization error",
                                0
                            )
                        )
                    }
                } else {
                    Result.Error(
                        FullNetworkError(
                            NetworkError.SERIALIZATION,
                            "Serialization error",
                            0
                        )
                    )
                }

            }

            400 -> {
                val result: String = response.body<String>()
                if (result.isNotEmpty()) {
                    try {
                        val json = Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                            allowSpecialFloatingPointValues = true
                        }
                        val list =
                            json.decodeFromString<Response<ForecastResponseDto, ResponseError>>(
                                result
                            )
                        Result.Error(
                            FullNetworkError(
                                NetworkError.SERIALIZATION,
                                if (list.error.isNotEmpty()) list.error.first().message else NetworkError.SERIALIZATION.name,
                                if (list.error.isNotEmpty()) list.error.first().code else 409
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Result.Error(
                            FullNetworkError(
                                NetworkError.SERIALIZATION,
                                "Serialization error",
                                0
                            )
                        )
                    }
                } else {
                    Result.Error(
                        FullNetworkError(
                            NetworkError.SERIALIZATION,
                            "Serialization error",
                            0
                        )
                    )
                }
            }

            401 -> Result.Error(
                FullNetworkError(
                    NetworkError.UNAUTHORIZED,
                    "UNAUTHORIZED",
                    401
                )
            )

            409 -> Result.Error(
                FullNetworkError(
                    NetworkError.CONFLICT,
                    "CONFLICT",
                    409
                )
            )

            408 -> Result.Error(
                FullNetworkError(
                    NetworkError.REQUEST_TIMEOUT,
                    "CONFLICT",
                    408
                )
            )

            413 -> Result.Error(
                FullNetworkError(
                    NetworkError.PAYLOAD_TOO_LARGE,
                    "PAYLOAD TOO LARGE",
                    413
                )
            )

            in 500..599 -> Result.Error(
                FullNetworkError(
                    NetworkError.SERVER_ERROR,
                    "SERVER ERROR",
                    response.status.value
                )
            )

            else -> Result.Error(
                FullNetworkError(
                    NetworkError.UNKNOWN,
                    "UNKNOWN",
                    response.status.value
                )
            )
        }
    }

    override suspend fun weatherForecast(
        lat: Double,
        lon: Double,
        lang: String,
        apiKey: String,
        days: Int
    ): Result<Forecast, Error> {
        val response =
            try {
                httpClient.createKtorClient(httpEngine)
                    .get(urlProvider.getPrivateApiUrl() + WeatherApi.GET_WEATHER_FORECAST) {
                        parameter("q", "${lat},${lon}")
                        parameter("lang", lang)
                        parameter("key", apiKey)
                        parameter("days", days)

                        timeout {
                            requestTimeoutMillis = 30_000
                            connectTimeoutMillis = 10_000
                            socketTimeoutMillis = 15_000
                        }
                    }
            } catch (e: UnresolvedAddressException) {
                e.printStackTrace()
                return Result.Error(
                    FullNetworkError(
                        NetworkError.NO_INTERNET,
                        "No internet connection",
                        0
                    )
                )
            } catch (e: SerializationException) {
                e.printStackTrace()
                return Result.Error(
                    FullNetworkError(
                        NetworkError.NO_INTERNET,
                        "No internet connection",
                        0
                    )
                )
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
                return Result.Error(
                    FullNetworkError(
                        NetworkError.NO_INTERNET,
                        "No internet connection",
                        0
                    )
                )
            }catch (e: Exception){
                e.printStackTrace()
                return Result.Error(
                    FullNetworkError(
                        NetworkError.NO_INTERNET,
                        "No internet connection",
                        0
                    )
                )
            }

        return when (response.status.value) {
            in 200..299 -> {
                val result: String = response.body<String>()
                if (result.isNotEmpty()) {
                    try {
                        val json = Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                            coerceInputValues = true
                            explicitNulls = false
                            useAlternativeNames = false
                        }
                        val list =
                            json.decodeFromString<ForecastResponseDto>(result)
                        Result.Success(list.toForecast())
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Result.Error(
                            FullNetworkError(
                                NetworkError.SERIALIZATION,
                                "Serialization error",
                                0
                            )
                        )
                    }
                } else {
                    Result.Error(
                        FullNetworkError(
                            NetworkError.SERIALIZATION,
                            "Serialization error",
                            0
                        )
                    )
                }

            }

            400 -> {
                val result: String = response.body<String>()
                if (result.isNotEmpty()) {
                    try {
                        val json = Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                            allowSpecialFloatingPointValues = true
                        }
                        val list =
                            json.decodeFromString<Response<ForecastResponseDto, ResponseError>>(
                                result
                            )
                        Result.Error(
                            FullNetworkError(
                                NetworkError.SERIALIZATION,
                                if (list.error.isNotEmpty()) list.error.first().message else NetworkError.SERIALIZATION.name,
                                if (list.error.isNotEmpty()) list.error.first().code else 409
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Result.Error(
                            FullNetworkError(
                                NetworkError.SERIALIZATION,
                                "Serialization error",
                                0
                            )
                        )
                    }
                } else {
                    Result.Error(
                        FullNetworkError(
                            NetworkError.SERIALIZATION,
                            "Serialization error",
                            0
                        )
                    )
                }
            }

            401 -> Result.Error(
                FullNetworkError(
                    NetworkError.UNAUTHORIZED,
                    "UNAUTHORIZED",
                    401
                )
            )

            409 -> Result.Error(
                FullNetworkError(
                    NetworkError.CONFLICT,
                    "CONFLICT",
                    409
                )
            )

            408 -> Result.Error(
                FullNetworkError(
                    NetworkError.REQUEST_TIMEOUT,
                    "CONFLICT",
                    408
                )
            )

            413 -> Result.Error(
                FullNetworkError(
                    NetworkError.PAYLOAD_TOO_LARGE,
                    "PAYLOAD TOO LARGE",
                    413
                )
            )

            in 500..599 -> Result.Error(
                FullNetworkError(
                    NetworkError.SERVER_ERROR,
                    "SERVER ERROR",
                    response.status.value
                )
            )

            else -> Result.Error(
                FullNetworkError(
                    NetworkError.UNKNOWN,
                    "UNKNOWN",
                    response.status.value
                )
            )
        }
    }


}
