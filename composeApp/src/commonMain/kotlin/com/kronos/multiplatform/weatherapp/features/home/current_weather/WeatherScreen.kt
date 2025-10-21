package com.kronos.multiplatform.weatherapp.features.home.current_weather

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kronos.multiplatform.weatherapp.components.LoadingDialog
import com.kronos.multiplatform.weatherapp.components.NoWeatherItem
import com.kronos.multiplatform.weatherapp.components.PullToRefreshContainer
import com.kronos.multiplatform.weatherapp.device.screen_config.DeviceScreenConfiguration
import com.kronos.multiplatform.weatherapp.features.home.current_weather.content.WeatherContent
import org.koin.compose.viewmodel.koinViewModel
import weather_app.composeapp.generated.resources.Res
import weather_app.composeapp.generated.resources.loading_dialog_text
import weather_app.composeapp.generated.resources.loading_dialog_title

@Composable
fun WeatherScreen(
    deviceScreenConfiguration: DeviceScreenConfiguration,
    currentLang: String,
    apiKey: String,
    imageQuality: String,
    amountOfDays: Int,
    defaultCity: String,
    isDarkTheme: Boolean,
) {
    val viewModel = koinViewModel<WeatherViewModel>()
    val weather by viewModel.weather.collectAsStateWithLifecycle()
    val selectedUserLocation by viewModel.selectedUserLocation.collectAsStateWithLifecycle()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.initLocations(currentLang, apiKey, amountOfDays, defaultCity)
    }

    // Manejo de errores
    LaunchedEffect(error) {
        error?.let { errorMessage ->
            if (errorMessage.isNotBlank()) {
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    duration = SnackbarDuration.Short
                )
                viewModel.clean()
            }
        }
    }

    // Manejo de mensajes de advertencia
    LaunchedEffect(viewModel.message) {
        viewModel.message?.get("warning")?.let { warningMessage ->
            if (warningMessage.isNotBlank()) {
                snackbarHostState.showSnackbar(
                    message = warningMessage,
                    duration = SnackbarDuration.Short
                )
                viewModel.clean()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = when {
                            data.visuals.message.contains("error", ignoreCase = true) ->
                                MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.primary
                        },
                        contentColor = when {
                            data.visuals.message.contains("error", ignoreCase = true) ->
                                MaterialTheme.colorScheme.onError
                            else -> MaterialTheme.colorScheme.onPrimary
                        }
                    )
                }
            },
        ) { paddingValues ->
            PullToRefreshContainer(
                innerPadding = paddingValues,
                isRefreshing = screenState == WeatherScreenState.Loading,
                onRefresh = {
                    viewModel.refreshWeather(currentLang, apiKey, amountOfDays)
                }
            ) {
                when (screenState) {
                    WeatherScreenState.Idle -> {
                        WeatherIdleState(
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    WeatherScreenState.Loading -> {
                        WeatherLoadingState(
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    WeatherScreenState.NoWeather -> {
                        NoWeatherItem(
                            modifier = Modifier.fillMaxSize(),
                            onRetry = { viewModel.retryLastOperation(currentLang, apiKey, amountOfDays) }
                        )
                    }

                    WeatherScreenState.WeatherObtained -> {
                        if (weather != null) {
                            WeatherContent(
                                weather = weather!!,
                                deviceScreenConfiguration = deviceScreenConfiguration,
                                isDarkTheme = isDarkTheme,
                                urlProvider = viewModel.urlProvider,
                                imageQuality = imageQuality,
                                paddingValues = paddingValues,
                            )
                        } else {
                            NoWeatherItem(
                                modifier = Modifier.fillMaxSize(),
                                onRetry = { viewModel.retryLastOperation(currentLang, apiKey, amountOfDays) }
                            )
                        }
                    }
                }
            }

            // Diálogo de carga - solo se muestra durante Loading state
            LoadingDialog(
                title = Res.string.loading_dialog_title,
                message = Res.string.loading_dialog_text,
                showDialog = screenState == WeatherScreenState.Loading
            )
        }
    }
}

@Composable
private fun WeatherIdleState(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
        }
    }
}

@Composable
private fun WeatherLoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
    }
}