package com.kronos.multiplatform.weatherapp.data.repository.radar.rain

import com.kronos.multiplatform.weatherapp.components.maps.layers.MapLayerTiles
import com.kronos.multiplatform.weatherapp.core.result.Error
import com.kronos.multiplatform.weatherapp.core.result.Result
import com.kronos.multiplatform.weatherapp.data.remote.ktor.KtorClientFactory
import com.kronos.multiplatform.weatherapp.data.remote.ktor.KtorEngineFactory
import com.kronos.multiplatform.weatherapp.data.remote.ktor.util.FullNetworkError
import com.kronos.multiplatform.weatherapp.data.remote.ktor.util.NetworkError
import com.kronos.multiplatform.weatherapp.domain.repository.MapLayerRepository
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class MapLayerRepositoryImpl(
    private val httpClient: KtorClientFactory,
    private val httpEngine: KtorEngineFactory,
) : MapLayerRepository {

    override suspend fun getLayerTiles(): Result<MapLayerTiles, Error> {
        return try {
            val response = httpClient.createKtorClient(httpEngine)
                .get("https://api.rainviewer.com/public/weather-maps.json")

            val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
            val host = json["host"]!!.jsonPrimitive.content

            // 🌧️ Radar — último frame pasado
            val radarPath = json["radar"]
                ?.jsonObject?.get("past")
                ?.jsonArray
                ?.lastOrNull()
                ?.jsonObject?.get("path")
                ?.jsonPrimitive?.content ?: ""

            // 🔮 Nowcast — primer frame de predicción
            val nowcastPath = json["radar"]
                ?.jsonObject?.get("nowcast")
                ?.jsonArray
                ?.firstOrNull()
                ?.jsonObject?.get("path")
                ?.jsonPrimitive?.content ?: ""

            // 🛰️ Satélite infrarrojo — último frame
            val satellitePath = json["satellite"]
                ?.jsonObject?.get("infrared")
                ?.jsonArray
                ?.lastOrNull()
                ?.jsonObject?.get("path")
                ?.jsonPrimitive?.content ?: ""

            Result.Success(
                MapLayerTiles(
                    radarUrl = if (radarPath.isNotBlank())
                        "$host$radarPath/512/{z}/{x}/{y}/2/1_1.png"
                    else "",
                    nowcastUrl = if (nowcastPath.isNotBlank())
                        "$host$nowcastPath/512/{z}/{x}/{y}/2/1_1.png"
                    else "",
                    satelliteUrl = if (satellitePath.isNotBlank())
                        "$host$satellitePath/512/{z}/{x}/{y}/0/0_0.png"
                    else ""
                )
            )
        } catch (e: Exception) {
            Result.Error(
                FullNetworkError(NetworkError.UNKNOWN, e.message ?: "Error", 0)
            )
        }
    }
}
