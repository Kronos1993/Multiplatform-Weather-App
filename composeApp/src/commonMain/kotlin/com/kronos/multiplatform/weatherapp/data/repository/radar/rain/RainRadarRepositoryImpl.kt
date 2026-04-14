package com.kronos.multiplatform.weatherapp.data.repository.radar.rain

import com.kronos.multiplatform.weatherapp.data.remote.ktor.KtorClientFactory
import com.kronos.multiplatform.weatherapp.data.remote.ktor.KtorEngineFactory
import com.kronos.multiplatform.weatherapp.domain.repository.RainRadarRepository
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class RainRadarRepositoryImpl(
    private val httpClient: KtorClientFactory,
    private val httpEngine: KtorEngineFactory,
): RainRadarRepository {

    override suspend fun getRadarTileUrl(): String {
        val response = httpClient.createKtorClient(httpEngine).get("https://api.rainviewer.com/public/weather-maps.json")
        val json = Json.parseToJsonElement(response.bodyAsText())

        val root = json.jsonObject

        val host = root["host"]!!.jsonPrimitive.content
        val past = root["radar"]!!
            .jsonObject["past"]!!
            .jsonArray

        val lastFrame = past.last().jsonObject
        val path = lastFrame["path"]!!.jsonPrimitive.content

        return "$host$path/256/{z}/{x}/{y}/2/1_1.png"
    }
}