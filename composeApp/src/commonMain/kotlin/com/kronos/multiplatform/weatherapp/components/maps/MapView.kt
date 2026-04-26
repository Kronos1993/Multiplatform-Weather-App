package com.kronos.multiplatform.weatherapp.components.maps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.kronos.multiplatform.weatherapp.components.maps.layers.MapLayerState
import com.kronos.multiplatform.weatherapp.components.maps.layers.MapLayerType
import com.kronos.multiplatform.weatherapp.components.maps.markers.GeoJsonMapper
import com.kronos.multiplatform.weatherapp.components.maps.markers.MapMarker
import com.kronos.multiplatform.weatherapp.core.ui.components.BodyText
import com.kronos.multiplatform.weatherapp.core.ui.components.ExpressiveBaseCardView
import com.kronos.multiplatform.weatherapp.core.ui.components.theme.extendedDark
import com.kronos.multiplatform.weatherapp.core.ui.components.theme.extendedLight
import com.kronos.multiplatform.weatherapp.data.local.location.LocationModel
import kotlinx.serialization.json.JsonObject
import org.jetbrains.compose.resources.painterResource
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.asString
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.format
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.dsl.offset
import org.maplibre.compose.expressions.dsl.span
import org.maplibre.compose.layers.RasterLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.TileSetOptions
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.sources.rememberRasterSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.style.rememberStyleState
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.Position
import weather_app.composeapp.generated.resources.Res
import weather_app.composeapp.generated.resources.ic_locations
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.seconds

@Composable
fun FixMapView(
    markers: List<MapMarker> = listOf(),
    mapLayers: List<MapLayerState> = emptyList(),
    onLayerToggled: ((MapLayerType) -> Unit)? = null,
    darkTheme: Boolean,
    onMapClick: (Position) -> Unit,
    onMapLongClick: (Position) -> Unit,
    modifier: Modifier = Modifier
) {
    val marker = painterResource(Res.drawable.ic_locations)

    val cardBackgroundColor = if (darkTheme)
        extendedDark.backgroundCardColor.color
    else
        extendedLight.backgroundCardColor.color

    val initialPosition = markers.firstOrNull()?.let {
        Position(it.longitude, it.latitude)
    } ?: Position(-79.5199, 8.9824)

    val camera = rememberCameraState(
        firstPosition = CameraPosition(target = initialPosition, zoom = 5.5)
    )
    val styleState = rememberStyleState()

    LaunchedEffect(Unit) {
        camera.animateTo(
            finalPosition = camera.position.copy(),
            duration = 3.seconds,
        )
    }

    Card(
        modifier = Modifier.padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = modifier) {

            MaplibreMap(
                baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty"),
                styleState = styleState,
                modifier = modifier,
                cameraState = camera,
                options = MapOptions(
                    gestureOptions = GestureOptions.AllDisabled,
                    ornamentOptions = OrnamentOptions.AllDisabled,
                ),
                onMapClick = { pos, offset ->
                    val features = camera.projection?.queryRenderedFeatures(offset)
                    if (!features.isNullOrEmpty()) {
                        onMapClick(pos)
                        ClickResult.Consume
                    } else {
                        onMapClick(pos)
                        ClickResult.Pass
                    }
                },
                onMapLongClick = { pos, _ ->
                    onMapLongClick(pos)
                    ClickResult.Pass
                },
            ) {
                mapLayers.find {
                    it.type == MapLayerType.SATELLITE && it.enabled && it.tileUrl.isNotBlank()
                }?.let { layer ->
                    val source = rememberRasterSource(
                        tiles = listOf(layer.tileUrl),
                        options = TileSetOptions(),
                        tileSize = 256,
                    )
                    RasterLayer(
                        id = "satellite-layer",
                        source = source,
                        opacity = const(0.5f),
                    )
                }

                mapLayers.find {
                    it.type == MapLayerType.NOWCAST && it.enabled && it.tileUrl.isNotBlank()
                }?.let { layer ->
                    val source = rememberRasterSource(
                        tiles = listOf(layer.tileUrl),
                        options = TileSetOptions(),
                        tileSize = 256,
                    )
                    RasterLayer(
                        id = "nowcast-layer",
                        source = source,
                        opacity = const(0.5f),
                    )
                }

                mapLayers.find {
                    it.type == MapLayerType.RAIN_RADAR && it.enabled && it.tileUrl.isNotBlank()
                }?.let { layer ->
                    val source = rememberRasterSource(
                        tiles = listOf(layer.tileUrl),
                        options = TileSetOptions(),
                        tileSize = 256,
                    )
                    RasterLayer(
                        id = "rainviewer-layer",
                        source = source,
                        opacity = const(0.4f),
                    )
                }

                val myMarkerGeoJson = remember(markers) {
                    GeoJsonMapper.markersToJsonString(markers)
                }
                val myMarkerSource = rememberGeoJsonSource(
                    data = GeoJsonData.JsonString(myMarkerGeoJson)
                )
                SymbolLayer(
                    id = "current-location",
                    source = myMarkerSource,
                    onClick = { ClickResult.Consume },
                    iconImage = image(marker),
                    iconSize = const(.5f),
                    iconColor = const(Color.Black),
                    textField = format(
                        span(feature["title"].asString(), textSize = const(1f.em))
                    ),
                    textFont = const(listOf("Noto Sans Regular")),
                    textColor = const(Color.Black),
                    textOffset = offset(0.em, 0.6.em),
                )
            }

            MapLayerToggleButtons(
                layers = mapLayers,
                onToggle = onLayerToggled,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun MapView(
    markers: List<MapMarker> = listOf(),
    darkTheme: Boolean,
    mapLayers: List<MapLayerState> = emptyList(),
    onLayerToggled: ((MapLayerType) -> Unit)? = null,
    onMarkerClick: (MapMarker) -> Unit,
    onMapClick: (Position) -> Unit,
    onMapLongClick: (Position) -> Unit,
    onMapToCloseTap: () -> Unit,
    modifier: Modifier = Modifier,
    minDistanceBetweenMarkers: Double = 100.0,
    currentLocation: LocationModel? = null
) {
    val camera = rememberCameraState()
    val styleState = rememberStyleState()
    val marker = painterResource(Res.drawable.ic_locations)

    val currentZoom by remember { derivedStateOf { camera.position.zoom } }
    val radarLayersVisible = currentZoom <= 7.5

    LaunchedEffect(currentLocation) {
        if (currentLocation == null) {
            camera.animateTo(
                finalPosition = camera.position.copy(),
                duration = 3.seconds,
            )
        } else {
            camera.animateTo(
                finalPosition = CameraPosition(
                    target = Position(currentLocation.longitude, currentLocation.latitude),
                    zoom = 15.0
                ).copy(),
                duration = 3.seconds,
            )
        }
    }

    Box(modifier = modifier) {

        MaplibreMap(
            baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty"),
            styleState = styleState,
            modifier = modifier,
            cameraState = camera,
            options = MapOptions(
                gestureOptions = GestureOptions.Standard,
                ornamentOptions = OrnamentOptions(
                    isLogoEnabled = true,
                    logoAlignment = Alignment.BottomStart,
                    isCompassEnabled = true,
                    compassAlignment = Alignment.BottomEnd,
                    isScaleBarEnabled = false,
                    isAttributionEnabled = false
                )
            ),
            onMapClick = { pos, offset ->
                val features = camera.projection?.queryRenderedFeatures(offset)
                val clickedMarker = getMarkerFromFeatures(features.orEmpty(), markers)
                when {
                    clickedMarker != null -> {
                        onMarkerClick(clickedMarker)
                        ClickResult.Consume
                    }

                    !features.isNullOrEmpty() -> {
                        if (!isPositionTooCloseToExistingMarkers(
                                pos,
                                markers,
                                minDistanceBetweenMarkers
                            )
                        ) {
                            onMapClick(pos)
                            ClickResult.Consume
                        } else {
                            onMapToCloseTap()
                            ClickResult.Pass
                        }
                    }

                    else -> {
                        if (!isPositionTooCloseToExistingMarkers(
                                pos,
                                markers,
                                minDistanceBetweenMarkers
                            )
                        ) {
                            onMapClick(pos)
                            ClickResult.Consume
                        } else {
                            onMapToCloseTap()
                            ClickResult.Pass
                        }
                    }
                }
            },
            onMapLongClick = { pos, _ ->
                if (!isPositionTooCloseToExistingMarkers(pos, markers, minDistanceBetweenMarkers)) {
                    onMapLongClick(pos)
                } else {
                    onMapToCloseTap()
                }
                ClickResult.Pass
            },
        ) {

            if (radarLayersVisible) {
                mapLayers.find {
                    it.type == MapLayerType.SATELLITE && it.enabled && it.tileUrl.isNotBlank()
                }?.let { layer ->
                    val source = rememberRasterSource(
                        tiles = listOf(layer.tileUrl),
                        options = TileSetOptions(),
                        tileSize = 512,
                    )
                    RasterLayer(
                        id = "satellite-layer",
                        source = source,
                        opacity = const(0.5f),
                    )
                }

                mapLayers.find {
                    it.type == MapLayerType.NOWCAST && it.enabled && it.tileUrl.isNotBlank()
                }?.let { layer ->
                    val source = rememberRasterSource(
                        tiles = listOf(layer.tileUrl),
                        options = TileSetOptions(),
                        tileSize = 512,
                    )
                    RasterLayer(
                        id = "nowcast-layer",
                        source = source,
                        opacity = const(0.5f),
                    )
                }

                mapLayers.find {
                    it.type == MapLayerType.RAIN_RADAR && it.enabled && it.tileUrl.isNotBlank()
                }?.let { layer ->
                    val source = rememberRasterSource(
                        tiles = listOf(layer.tileUrl),
                        options = TileSetOptions(),
                        tileSize = 512,
                    )
                    RasterLayer(
                        id = "rainviewer-layer",
                        source = source,
                        opacity = const(0.4f),
                    )
                }
            }

            val myMarkerGeoJson = remember(markers) {
                GeoJsonMapper.markersToJsonString(markers)
            }
            val myMarkerSource = rememberGeoJsonSource(
                data = GeoJsonData.JsonString(myMarkerGeoJson)
            )
            SymbolLayer(
                id = "current-locations",
                source = myMarkerSource,
                onClick = { features ->
                    val clickedMarker = getMarkerFromFeatures(features, markers)
                    if (clickedMarker != null) {
                        onMarkerClick(clickedMarker)
                    }
                    ClickResult.Consume
                },
                iconImage = image(marker),
                iconSize = const(.8f),
                iconColor = const(Color.Black),
                textField = format(
                    span(feature["title"].asString(), textSize = const(1f.em))
                ),
                textFont = const(listOf("Noto Sans Regular")),
                textColor = const(Color.Black),
                textOffset = offset(0.em, 0.6.em),
            )
        }

        MapLayerToggleButtons(
            layers = mapLayers,
            onToggle = onLayerToggled,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        )
    }
}

@Composable
fun MapLayerToggleButtons(
    layers: List<MapLayerState> = emptyList(),
    onToggle: ((MapLayerType) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.End
    ) {
        layers.forEach { layer ->
            MapLayerButton(
                layer = layer,
                onClick = {
                    onToggle?.invoke(layer.type)
                }
            )
        }
    }
}

@Composable
fun MapLayerButton(
    layer: MapLayerState,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (layer.enabled) Color(0xFF1565C0) else Color.Black.copy(alpha = 0.55f),
        animationSpec = tween(200),
        label = "layer_bg_${layer.type.name}"
    )

    ExpressiveBaseCardView(
        cardBackgroundColor = backgroundColor,
        elevation = 2.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            BodyText(text = layer.type.icon)
            AnimatedVisibility(visible = layer.enabled) {
                BodyText(
                    text = layer.type.name
                        .lowercase()
                        .replaceFirstChar { it.uppercase() }
                        .replace("_", " "),
                    maxLines = 1
                )
            }
        }
    }
}


/**
 * Encuentra el marker correspondiente a las features clickeadas
 */
private fun getMarkerFromFeatures(
    features: List<Feature<Geometry, JsonObject?>>,
    markers: List<MapMarker>
): MapMarker? {
    return features.firstOrNull()?.let { feature ->
        val featureId = feature.id?.content
        markers.find { it.id == featureId }
    }
}

/**
 * Verifica si una posición está demasiado cerca de markers existentes
 * Solo se usa para validar NUEVOS markers antes de crearlos
 */
private fun isPositionTooCloseToExistingMarkers(
    position: Position,
    existingMarkers: List<MapMarker>,
    minDistance: Double
): Boolean {
    return existingMarkers.any { marker ->
        calculateDistance(
            position.latitude,
            position.longitude,
            marker.latitude,
            marker.longitude
        ) < minDistance
    }
}

/**
 * Calcula la distancia entre dos puntos en metros usando fórmula Haversine
 */
private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371000.0 // Earth radius in meters
    val dLat = (lat2 - lat1) * PI / 180.0
    val dLon = (lon2 - lon1) * PI / 180.0
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1 * PI / 180.0) * cos(lat2 * PI / 180.0) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}

