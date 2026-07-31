package com.kronos.multiplatform.weatherapp.components.maps.layers

enum class MapLayerType(
    val icon: String,
    val labelKey: String
) {
    RAIN_RADAR(icon = "🌧️", labelKey = "map_layer_rain"),
    NOWCAST(icon = "🔮", labelKey = "map_layer_nowcast"),
    SATELLITE(icon = "🛰️", labelKey = "map_layer_satellite"),
    TEMPERATURE(icon = "🌡️", labelKey = "map_layer_temperature"),
    WIND(icon = "💨", labelKey = "map_layer_wind"),
    PRESSURE(icon = "🧭", labelKey = "map_layer_pressure"),
}

data class MapLayerState(
    val type: MapLayerType,
    val enabled: Boolean = false,
    val tileUrl: String = ""
)

data class MapLayerTiles(
    val radarUrl: String,
    val nowcastUrl: String,
    val satelliteUrl: String,
    val temperatureUrl: String,
    val windUrl: String,
    val pressureUrl: String
)