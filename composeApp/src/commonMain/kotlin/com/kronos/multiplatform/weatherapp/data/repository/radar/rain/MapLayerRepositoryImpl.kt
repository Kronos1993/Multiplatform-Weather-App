package com.kronos.multiplatform.weatherapp.data.repository.radar.rain

import com.kronos.multiplatform.weatherapp.components.maps.layers.MapLayerTiles
import com.kronos.multiplatform.weatherapp.core.result.Error
import com.kronos.multiplatform.weatherapp.core.result.Result
import com.kronos.multiplatform.weatherapp.core.util.format
import com.kronos.multiplatform.weatherapp.data.remote.ktor.KtorClientFactory
import com.kronos.multiplatform.weatherapp.data.remote.ktor.KtorEngineFactory
import com.kronos.multiplatform.weatherapp.data.remote.ktor.util.FullNetworkError
import com.kronos.multiplatform.weatherapp.data.remote.ktor.util.NetworkError
import com.kronos.multiplatform.weatherapp.domain.repository.MapLayerRepository
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Clock

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

            val radarPath = json["radar"]
                ?.jsonObject?.get("past")
                ?.jsonArray?.lastOrNull()
                ?.jsonObject?.get("path")?.jsonPrimitive?.content ?: ""

            val nowcastPath = json["radar"]
                ?.jsonObject?.get("nowcast")
                ?.jsonArray?.firstOrNull()
                ?.jsonObject?.get("path")?.jsonPrimitive?.content ?: ""

            val satellitePath = json["satellite"]
                ?.jsonObject?.get("infrared")
                ?.jsonArray?.lastOrNull()
                ?.jsonObject?.get("path")?.jsonPrimitive?.content ?: ""

            val (date, hour) = getCurrentDateAndHour()

            Result.Success(
                MapLayerTiles(
                    radarUrl = if (radarPath.isNotBlank())
                        "$host$radarPath/512/{z}/{x}/{y}/2/1_1.png"
                    else weatherApiTileUrl("precip", date, hour),

                    nowcastUrl = if (nowcastPath.isNotBlank())
                        "$host$nowcastPath/512/{z}/{x}/{y}/2/1_1.png"
                    else "",

                    satelliteUrl = if (satellitePath.isNotBlank())
                        "$host$satellitePath/512/{z}/{x}/{y}/0/0_0.png"
                    else "",

                    temperatureUrl = weatherApiTileUrl("tmp2m", date, hour),
                    windUrl = weatherApiTileUrl("wind", date, hour),
                    pressureUrl = weatherApiTileUrl("pressure", date, hour)
                )
            )
        } catch (e: Exception) {
            Result.Error(FullNetworkError(NetworkError.UNKNOWN, e.message ?: "Error", 0))
        }
    }

    private fun weatherApiTileUrl(layerPath: String, date: String, hour: String): String =
        "https://weathermaps.weatherapi.com/$layerPath/tiles/$date$hour/{z}/{x}/{y}.png"

    private fun getCurrentDateAndHour(): Pair<String, String> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        val year = now.year.toString().padStart(4, '0')
        val month = now.month.number.toString().padStart(2, '0')
        val day = now.day.toString().padStart(2, '0')
        val hour = now.hour.toString().padStart(2, '0')

        val date = "$year$month$day"
        return date to hour
    }
}
