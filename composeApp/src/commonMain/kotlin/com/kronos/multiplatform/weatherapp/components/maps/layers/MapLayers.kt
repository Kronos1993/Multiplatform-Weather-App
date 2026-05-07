package com.kronos.multiplatform.weatherapp.components.maps.layers

enum class MapLayerType(
    val icon: String,
    val labelKey: String
) {
    RAIN_RADAR(    icon = "🌧️", labelKey = "map_layer_rain"),
    NOWCAST(       icon = "🔮", labelKey = "map_layer_nowcast"),
    SATELLITE(     icon = "🛰️", labelKey = "map_layer_satellite"),
}

data class MapLayerState(
    val type: MapLayerType,
    val enabled: Boolean = false,
    val tileUrl: String = ""
)

data class MapLayerTiles(
    val radarUrl: String,
    val nowcastUrl: String,
    val satelliteUrl: String
)