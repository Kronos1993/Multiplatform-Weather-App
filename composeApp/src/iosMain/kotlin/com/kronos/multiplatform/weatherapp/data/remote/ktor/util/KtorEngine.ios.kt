package com.kronos.multiplatform.weatherapp.data.remote.ktor.util

import com.kronos.multiplatform.weatherapp.data.remote.ktor.KtorEngineFactory
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

actual class KtorEngine : KtorEngineFactory {
    override fun createKtorEngine(): HttpClientEngine {
        return Darwin.create()
    }
}