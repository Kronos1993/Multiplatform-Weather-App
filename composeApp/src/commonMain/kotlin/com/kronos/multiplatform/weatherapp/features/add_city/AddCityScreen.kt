package com.kronos.multiplatform.weatherapp.features.add_city

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.kronos.multiplatform.weatherapp.components.ComponentSize
import com.kronos.multiplatform.weatherapp.components.LoadingDialog
import com.kronos.multiplatform.weatherapp.components.button.FabButton
import com.kronos.multiplatform.weatherapp.components.button.IconButton
import com.kronos.multiplatform.weatherapp.components.maps.MapView
import com.kronos.multiplatform.weatherapp.core.util.format
import com.kronos.multiplatform.weatherapp.domain.model.MeasureUnit
import com.kronos.multiplatform.weatherapp.features.home.current_weather.content.ShowCityInfoDialog
import com.kronos.multiplatform.weatherapp.features.home.current_weather.content.ShowSelectedCityInfoDialog
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import weather_app.composeapp.generated.resources.Res
import weather_app.composeapp.generated.resources.add_city
import weather_app.composeapp.generated.resources.close
import weather_app.composeapp.generated.resources.current_weather_key
import weather_app.composeapp.generated.resources.loading_dialog_text
import weather_app.composeapp.generated.resources.loading_dialog_title
import weather_app.composeapp.generated.resources.marker_to_close
import weather_app.composeapp.generated.resources.notification_long_details
import weather_app.composeapp.generated.resources.notification_long_details_fahrenheit
import weather_app.composeapp.generated.resources.notification_short_details
import weather_app.composeapp.generated.resources.notification_short_details_fahrenheit
import weather_app.composeapp.generated.resources.notification_title
import weather_app.composeapp.generated.resources.notification_title_fahrenheit
import weather_app.composeapp.generated.resources.temp_celsius
import weather_app.composeapp.generated.resources.temp_fahrenheit

@Composable
fun AddCityScreen(
    navController: NavHostController,
    currentLang: String,
    apiKey: String,
    isDarkTheme: Boolean,
    measureUnit: MeasureUnit
) {
    val viewModel = koinViewModel<AddCityViewModel>()
    val forecast by viewModel.forecast.collectAsStateWithLifecycle()
    val markers by viewModel.markers.collectAsStateWithLifecycle()
    val markerSelected by viewModel.markerSelected.collectAsStateWithLifecycle()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()

    val errorMessage = stringResource(Res.string.marker_to_close)

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.initString(
        stringResource(Res.string.current_weather_key),
        if (measureUnit == MeasureUnit.INTERNATIONAL)
            stringResource(Res.string.notification_title)
        else
            stringResource(Res.string.notification_title_fahrenheit),
        if (measureUnit == MeasureUnit.INTERNATIONAL)
            stringResource(Res.string.notification_short_details)
        else
            stringResource(Res.string.notification_short_details_fahrenheit),
        if (measureUnit == MeasureUnit.INTERNATIONAL)
            stringResource(Res.string.notification_long_details)
        else
            stringResource(Res.string.notification_long_details_fahrenheit),
    )

    LaunchedEffect(error) {
        error?.let { errorMessage ->
            if (errorMessage.isNotBlank()) {
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    duration = SnackbarDuration.Short
                )
                viewModel.clearError()
            }
        }
    }

    LaunchedEffect(screenState){
        if (screenState == AddCityScreenState.CityAdded) {
            navController.popBackStack()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.getLocationMarkers()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            containerColor = MaterialTheme.colorScheme.onPrimary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            },
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.surface)
                ){
                    MapView(
                        darkTheme = isDarkTheme,
                        markers = markers,
                        onMarkerClick = {
                            viewModel.setMarkerSelected(it)
                        },
                        onMapClick = { coordinate ->
                            viewModel.onMapClick(
                                coordinate.latitude,
                                coordinate.longitude,
                                currentLang,
                                apiKey)
                        },
                        onMapLongClick = {coordinate->
                            viewModel.onMapClick(
                                coordinate.latitude,
                                coordinate.longitude,
                                currentLang,
                                apiKey
                            )
                        },
                        onMapToCloseTap = {
                            viewModel.setError(errorMessage)
                        },
                        modifier = Modifier.fillMaxSize(),
                        currentLocation = currentLocation
                    )

                    IconButton(
                        onClick = { navController.popBackStack() },
                        icon = Icons.AutoMirrored.Outlined.ArrowBack,
                        size = ComponentSize.LARGE,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = CircleShape
                            )
                    )

                    FabButton(
                        onClick = { viewModel.getGpsLocation(currentLang,apiKey) },
                        icon = Icons.Outlined.MyLocation,
                        size = ComponentSize.MEDIUM,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 60.dp)
                    )
                }
            }

            LoadingDialog(
                title = Res.string.loading_dialog_title,
                message = Res.string.loading_dialog_text,
                showDialog = screenState == AddCityScreenState.Loading
            )

            ShowCityInfoDialog(
                cityName = forecast?.location?.name ?: "",
                temp =
                    if (measureUnit == MeasureUnit.INTERNATIONAL)
                        stringResource(Res.string.temp_celsius).format(
                            forecast?.current?.tempC?:0.0,
                        )
                    else
                        stringResource(Res.string.temp_fahrenheit).format(
                            forecast?.current?.tempF?:0.0,
                        ),
                showDialog = screenState == AddCityScreenState.CityObtained,
                confirmText = stringResource(Res.string.add_city),
                onConfirm = { viewModel.addLocation(measureUnit) },
                cancelText = stringResource(Res.string.close),
                onCancel = { viewModel.dismissCityInfo() },
                onClose = { viewModel.dismissCityInfo() }
            )

            ShowSelectedCityInfoDialog(
                cityName = markerSelected?.title ?: "",
                temp =
                    if (measureUnit == MeasureUnit.INTERNATIONAL)
                        stringResource(Res.string.temp_celsius).format(
                            markerSelected?.customProperties["tempC"]?:0.0,
                        )
                    else
                        stringResource(Res.string.temp_fahrenheit).format(
                            markerSelected?.customProperties["tempF"]?:0.0,
                        ),
                iconUrl = markerSelected?.customProperties["icon"] ?: "",
                showDialog = screenState == AddCityScreenState.ShowCityInfo,
                confirmText = stringResource(Res.string.close),
                onConfirm = { viewModel.dismissMarkerInfo() },
                onClose = { viewModel.dismissMarkerInfo() }
            )
        }
    }
}