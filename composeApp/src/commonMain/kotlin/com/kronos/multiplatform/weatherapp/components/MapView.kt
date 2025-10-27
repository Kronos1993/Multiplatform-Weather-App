package com.kronos.multiplatform.weatherapp.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kronos.multiplatform.weatherapp.components.theme.extendedDark
import com.kronos.multiplatform.weatherapp.components.theme.extendedLight
import io.github.dellisd.spatialk.geojson.Position
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.style.rememberStyleState
import org.maplibre.compose.util.ClickResult
import kotlin.time.Duration.Companion.seconds

@Composable
fun FixMapView(
    lat: Double,
    lon: Double,
    darkTheme: Boolean,
    onMapClick: (Position) -> Unit,
    onMapLongClick: (Position) -> Unit,
    modifier: Modifier = Modifier
) {

    val cardBackgroundColor = if (darkTheme) {
        extendedDark.backgroundCardColor.color
    } else {
        extendedLight.backgroundCardColor.color
    }

    val camera =
        rememberCameraState(
            firstPosition = CameraPosition(
                target = Position(lon, lat), zoom = 5.5
            )
        )

    val styleState = rememberStyleState()

    LaunchedEffect(Unit) {
        camera.animateTo(
            finalPosition =
                camera.position.copy(),
            duration = 3.seconds,
        )
    }

    Card(
        modifier =
            Modifier.padding(4.dp),
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
                    println("Clicked on ${features[0].json()} at $pos")
                    onMapClick(pos)
                    ClickResult.Consume
                } else {
                    onMapClick(pos)
                    ClickResult.Pass
                }
            },
            onMapLongClick = { pos, offset ->
                println("Long click at $pos")
                onMapLongClick(pos)
                ClickResult.Pass
            },
        ) {

        }
    }
}

@Composable
fun MapView(
    darkTheme: Boolean,
    onMapClick: (Position) -> Unit,
    onMapLongClick: (Position) -> Unit,
    modifier: Modifier = Modifier
) {

    val camera = rememberCameraState()

    val styleState = rememberStyleState()

    LaunchedEffect(Unit) {
        camera.animateTo(
            finalPosition =
                camera.position.copy(),
            duration = 3.seconds,
        )
    }

    MaplibreMap(
        baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty"),
        styleState = styleState,
        modifier = modifier,
        cameraState = camera,
        options = MapOptions(
            gestureOptions = GestureOptions.Standard,
            ornamentOptions = OrnamentOptions.AllDisabled,
        ),
        onMapClick = { pos, offset ->
            val features = camera.projection?.queryRenderedFeatures(offset)
            if (!features.isNullOrEmpty()) {
                println("Clicked on ${features[0].json()} at $pos")
                onMapClick(pos)
                ClickResult.Consume
            } else {
                ClickResult.Pass
            }
        },
        onMapLongClick = { pos, offset ->
            println("Long click at $pos")
            onMapLongClick(pos)
            ClickResult.Pass
        },
    ) {

    }
}
