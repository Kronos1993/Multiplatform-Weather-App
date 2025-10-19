package com.kronos.multiplatform.weatherapp.data.remote.ktor

import io.ktor.client.engine.HttpClientEngine

interface KtorEngineFactory {
    fun createKtorEngine(): HttpClientEngine
}