package com.kronos.multiplatform.weatherapp.components.maps.markers

import kotlinx.serialization.Serializable

@Serializable
data class GeoJsonFeatureCollection(
    val type: String = "FeatureCollection",
    val features: List<GeoJsonFeature>
)

@Serializable
data class GeoJsonFeature(
    val type: String = "Feature",
    val geometry: GeoJsonGeometry,
    val properties: Map<String, String?> = emptyMap(),
    val id: String? = null
)

@Serializable
data class GeoJsonGeometry(
    val type: String = "Point",
    val coordinates: List<Double>
)

@Serializable
data class MapMarker(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val description: String? = null,
    val customProperties: Map<String, String?> = emptyMap(),
    val iconUrl: String? = null
)