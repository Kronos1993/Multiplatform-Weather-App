package com.kronos.multiplatform.weatherapp.data.remote.ktor

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Named

@Named("PublicKtorClientFactory")
class PublicKtorClientFactoryImpl: KtorClientFactory {
    override fun createKtorClient(engine: KtorEngineFactory, token:String): HttpClient {
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
        }
    }
}