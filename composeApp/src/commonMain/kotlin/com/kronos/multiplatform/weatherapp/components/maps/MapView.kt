package com.kronos.multiplatform.weatherapp.components.maps

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.kronos.multiplatform.weatherapp.components.maps.markers.GeoJsonMapper
import com.kronos.multiplatform.weatherapp.components.maps.markers.MapMarker
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
import org.maplibre.spatialk.geojson.toJson
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
    rainRadarTiles:String,
    darkTheme: Boolean,
    onMapClick: (Position) -> Unit,
    onMapLongClick: (Position) -> Unit,
    modifier: Modifier = Modifier
) {

    val marker = painterResource(Res.drawable.ic_locations)

    val cardBackgroundColor = if (darkTheme) {
        extendedDark.backgroundCardColor.color
    } else {
        extendedLight.backgroundCardColor.color
    }

    val initialPosition = markers.firstOrNull()?.let {
        Position(it.longitude, it.latitude)
    } ?: Position(-79.5199, 8.9824) // Panamá fallback

    val camera = rememberCameraState(
        firstPosition = CameraPosition(
            target = initialPosition,
            zoom = 5.5
        )
    )

    val styleState = rememberStyleState()

    LaunchedEffect(Unit) {
        camera.animateTo(
            finalPosition = camera.position.copy(),
            duration = 3.seconds,
        )
    }

    val radarTileUrl = remember {
        rainRadarTiles
    }

    Card(
        modifier = Modifier.padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
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
                    println("Clicked on ${features[0].toJson()} at $pos")
                    onMapClick(pos)
                    ClickResult.Consume
                } else {
                    onMapClick(pos)
                    ClickResult.Pass
                }
            },
            onMapLongClick = { pos, _ ->
                println("Long click at $pos")
                onMapLongClick(pos)
                ClickResult.Pass
            },
        ) {

            val radarSource = rememberRasterSource(
                tiles = listOf(radarTileUrl),
                options = TileSetOptions(),
                tileSize = 512,
            )

            RasterLayer(
                id = "rainviewer-layer",
                source = radarSource,
                opacity = const(0.4f),
            )

            val myMarkerGeoJson = remember(markers) {
                GeoJsonMapper.markersToJsonString(markers)
            }

            val myMarkerSource = rememberGeoJsonSource(
                data = GeoJsonData.JsonString(myMarkerGeoJson)
            )

            SymbolLayer(
                id = "current-location",
                source = myMarkerSource,
                onClick = {
                    ClickResult.Consume
                },
                iconImage = image(marker),
                iconSize = const(.5f),
                iconColor = const(Color.Black),
                textField = format(
                    span(feature["title"].asString(), textSize = const(1f.em)),
                ),
                textFont = const(listOf("Noto Sans Regular")),
                textColor = const(Color.Black),
                textOffset = offset(0.em, 0.6.em),
            )
        }
    }
}

@Composable
fun MapView(
    markers: List<MapMarker> = listOf(),
    darkTheme: Boolean,
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

    LaunchedEffect(currentLocation) {
        if (currentLocation == null){
            camera.animateTo(
                finalPosition = camera.position.copy(),
                duration = 3.seconds,
            )
        }else{
            val position = CameraPosition(
                target = Position(currentLocation.longitude, currentLocation.latitude),
                zoom = 15.0
            )
            camera.animateTo(
                finalPosition = position.copy(),
                duration = 3.seconds,
            )
        }

    }

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
            if (clickedMarker != null) {
                println("Clicked on existing marker: ${clickedMarker.title} at $pos")
                onMarkerClick(clickedMarker)
                ClickResult.Consume
            } else if (!features.isNullOrEmpty()) {
                val isTooClose =
                    isPositionTooCloseToExistingMarkers(pos, markers, minDistanceBetweenMarkers)
                if (!isTooClose) {
                    println("Click at $pos - Marker created")
                    onMapClick(pos)
                    ClickResult.Consume
                } else {
                    println("Click at $pos - Too close to existing marker, ignoring")
                    onMapToCloseTap()
                    ClickResult.Pass
                }
            } else {
                val isTooClose =
                    isPositionTooCloseToExistingMarkers(pos, markers, minDistanceBetweenMarkers)
                if (!isTooClose) {
                    println("Click at empty space at $pos - Marker created")
                    onMapClick(pos)
                    ClickResult.Consume
                } else {
                    println("Click at empty space at $pos - Too close to existing marker, ignoring")
                    onMapToCloseTap()
                    ClickResult.Pass
                }
            }
        },
        onMapLongClick = { pos, offset ->
            val isTooClose =
                isPositionTooCloseToExistingMarkers(pos, markers, minDistanceBetweenMarkers)
            if (!isTooClose) {
                println("Long click at $pos - Marker created")
                onMapLongClick(pos)
            } else {
                onMapToCloseTap()
                println("Long click at $pos - Too close to existing marker, ignoring")
            }
            ClickResult.Pass
        },
    ) {
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
                    println("SymbolLayer click on marker: ${clickedMarker.title}")
                    onMarkerClick(clickedMarker)
                }
                ClickResult.Consume
            },
            iconImage = image(marker),
            iconSize = const(.8f),
            iconColor = const(Color.Black),
            textField = format(
                span(feature["title"].asString(), textSize = const(1f.em)),
            ),
            textFont = const(listOf("Noto Sans Regular")),
            textColor = const(Color.Black),
            textOffset = offset(0.em, 0.6.em),
        )
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

