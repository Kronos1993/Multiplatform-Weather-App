package com.kronos.multiplatform.weatherapp.data.remote.ktor

import io.ktor.client.HttpClient

interface KtorClientFactory {
    fun createKtorClient(engine: KtorEngineFactory, token:String = ""): HttpClient
}