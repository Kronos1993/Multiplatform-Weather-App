package com.kronos.multiplatform.weatherapp.core.ui.components.maps.markers

// GeoJsonMapper.kt
import kotlinx.serialization.json.Json

object GeoJsonMapper {

    fun mapMarkersToGeoJson(markers: List<MapMarker>): GeoJsonFeatureCollection {
        return GeoJsonFeatureCollection(
            type = "FeatureCollection",
            features = markers.map { marker ->
                GeoJsonFeature(
                    geometry = GeoJsonGeometry(
                        type = "Point",
                        coordinates = listOf(marker.longitude, marker.latitude)
                    ),
                    properties = buildProperties(marker),
                    id = marker.id
                )
            }
        )
    }

    private fun buildProperties(marker: MapMarker): Map<String, String?> {
        val properties = mutableMapOf<String, String?>(
            "title" to marker.title,
            "description" to marker.description,
            "iconUrl" to marker.iconUrl
        )
        // Agregar propiedades custom
        properties.putAll(marker.customProperties)
        return properties
    }

    // Serializer a JSON string
    fun toJsonString(featureCollection: GeoJsonFeatureCollection): String {
        val jsonConfig = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
            explicitNulls = false
        }
        return jsonConfig.encodeToString(GeoJsonFeatureCollection.serializer(), featureCollection)
    }

    // Función de conveniencia: directamente de List<MapMarker> a JSON string
    fun markersToJsonString(markers: List<MapMarker>): String {
        val geoJson = mapMarkersToGeoJson(markers)
        val jsonConfig = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
            explicitNulls = false
        }
        return jsonConfig.encodeToString(GeoJsonFeatureCollection.serializer(), geoJson)
    }
}