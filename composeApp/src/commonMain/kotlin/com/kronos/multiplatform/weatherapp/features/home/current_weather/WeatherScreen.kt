package com.kronos.multiplatform.weatherapp.features.home.current_weather

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
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kronos.multiplatform.weatherapp.components.LoadingDialog
import com.kronos.multiplatform.weatherapp.components.NoWeather
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

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.initLocations(currentLang, apiKey, amountOfDays,defaultCity)
    }

    // Manejo de errores
    LaunchedEffect(viewModel.message) {
        viewModel.message?.get("error")?.let { errorMessage ->
            if (errorMessage.isNotBlank()) {
                snackbarHostState.showSnackbar(
                    message = errorMessage,
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
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                }
            },
        ) { paddingValues ->
            PullToRefreshContainer(
                innerPadding = paddingValues,
                isRefreshing = viewModel.loading,
                onRefresh = {
                    viewModel.refreshWeather(currentLang, apiKey, 3)
                }
            ) {
                when {
                    weather == null -> {
                        NoWeather(modifier = Modifier.fillMaxSize())
                    }

                    weather != null -> {
                        WeatherContent(
                            weather = weather!!,
                            deviceScreenConfiguration = deviceScreenConfiguration,
                            isDarkTheme = isDarkTheme,
                            urlProvider = viewModel.urlProvider,
                            imageQuality = imageQuality,
                            paddingValues = paddingValues
                        )
                    }
                }
            }

            // Diálogo de carga
            LoadingDialog(
                title = Res.string.loading_dialog_title,
                message = Res.string.loading_dialog_text,
                showDialog = viewModel.loading
            )
        }
    }
}