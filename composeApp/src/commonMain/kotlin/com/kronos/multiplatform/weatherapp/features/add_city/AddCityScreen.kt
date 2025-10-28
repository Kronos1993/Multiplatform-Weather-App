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
import com.kronos.multiplatform.weatherapp.components.maps.MapView
import com.kronos.multiplatform.weatherapp.components.ShowCityInfoDialog
import com.kronos.multiplatform.weatherapp.components.ShowSelectedCityInfoDialog
import com.kronos.multiplatform.weatherapp.components.button.IconButton
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import weather_app.composeapp.generated.resources.Res
import weather_app.composeapp.generated.resources.add_city
import weather_app.composeapp.generated.resources.close
import weather_app.composeapp.generated.resources.loading_dialog_text
import weather_app.composeapp.generated.resources.loading_dialog_title
import weather_app.composeapp.generated.resources.marker_to_close

@Composable
fun AddCityScreen(
    navController: NavHostController,
    currentLang: String,
    apiKey: String,
    isDarkTheme: Boolean,
) {
    val viewModel = koinViewModel<AddCityViewModel>()
    val forecast by viewModel.forecast.collectAsStateWithLifecycle()
    val markers by viewModel.markers.collectAsStateWithLifecycle()
    val markerSelected by viewModel.markerSelected.collectAsStateWithLifecycle()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    val errorMessage = stringResource(Res.string.marker_to_close)

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

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
                        onMapClick = { coordinate,canAdd ->
                            if (canAdd){
                                viewModel.onMapClick(
                                    coordinate.latitude,
                                    coordinate.longitude,
                                    currentLang,
                                    apiKey
                                )
                            }else{
                                viewModel.setError(errorMessage)
                            }
                        },
                        onMapLongClick = {coordinate,canAdd->

                        },
                        modifier = Modifier.fillMaxSize()
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
                }
            }

            LoadingDialog(
                title = Res.string.loading_dialog_title,
                message = Res.string.loading_dialog_text,
                showDialog = screenState == AddCityScreenState.Loading
            )

            ShowCityInfoDialog(
                cityName = forecast?.location?.name ?: "",
                temp = "${forecast?.current?.tempC ?: ""}°C",
                showDialog = screenState == AddCityScreenState.CityObtained,
                confirmText = stringResource(Res.string.add_city),
                onConfirm = { viewModel.addLocation() },
                cancelText = stringResource(Res.string.close),
                onCancel = { viewModel.dismissCityInfo() },
                onClose = { viewModel.dismissCityInfo() }
            )

            ShowSelectedCityInfoDialog(
                cityName = markerSelected?.title ?: "",
                temp = "${markerSelected?.customProperties["temp"] ?: ""}°C",
                iconUrl = markerSelected?.customProperties["icon"] ?: "",
                showDialog = screenState == AddCityScreenState.ShowCityInfo,
                confirmText = stringResource(Res.string.close),
                onConfirm = { viewModel.dismissMarkerInfo() },
                onClose = { viewModel.dismissMarkerInfo() }
            )
        }
    }
}