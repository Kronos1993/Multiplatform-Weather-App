package com.kronos.multiplatform.weatherapp.data.remote.ktor

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Named

@Named("PrivateKtorClientFactory")
class PrivateKtorClientFactoryImpl: KtorClientFactory {
    override fun createKtorClient(engine: KtorEngineFactory, token: String): HttpClient {
        return HttpClient(engine.createKtorEngine()){
            install(Logging){
                level = LogLevel.ALL
            }
            install(ContentNegotiation){
                json(
                    json = Json {
                        ignoreUnknownKeys = true
                        isLenient = true // Allow for lenient parsing
                        allowSpecialFloatingPointValues = true // Allow NaN and Infinity
                    }
                )
            }
            install(Auth){
                bearer {
                    // Configure bearer authentication
                    BearerTokens(
                        accessToken = token,
                        refreshToken = token
                    )
                }

            }
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000 // Tiempo de espera de solicitud en milisegundos(30 seg)
                connectTimeoutMillis = 10_000 // Tiempo de espera de conexión en milisegundos(10 seg)
                socketTimeoutMillis = 15_000 // Tiempo de espera de socket en milisegundos(15 seg)
            }
        }
    }
}