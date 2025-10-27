package com.kronos.multiplatform.weatherapp.features.home.current_weather

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kronos.multiplatform.weatherapp.components.LoadingDialog
import com.kronos.multiplatform.weatherapp.components.NoWeatherItem
import com.kronos.multiplatform.weatherapp.components.PullToRefreshContainer
import com.kronos.multiplatform.weatherapp.components.WeatherIdleState
import com.kronos.multiplatform.weatherapp.components.WeatherLoadingState
import com.kronos.multiplatform.weatherapp.device.screen_config.DeviceScreenConfiguration
import com.kronos.multiplatform.weatherapp.features.home.current_weather.content.WeatherContentLandscape
import com.kronos.multiplatform.weatherapp.features.home.current_weather.content.WeatherContentPortrait
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import weather_app.composeapp.generated.resources.Res
import weather_app.composeapp.generated.resources.current_weather_key
import weather_app.composeapp.generated.resources.loading_dialog_text
import weather_app.composeapp.generated.resources.loading_dialog_title
import weather_app.composeapp.generated.resources.notification_long_details
import weather_app.composeapp.generated.resources.notification_short_details
import weather_app.composeapp.generated.resources.notification_title

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
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }


    viewModel.initNotificationsString(
        stringResource(Res.string.current_weather_key),
        stringResource(Res.string.notification_title),
        stringResource(Res.string.notification_short_details),
        stringResource(Res.string.notification_long_details),
    )

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

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val rootModifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .consumeWindowInsets(WindowInsets.navigationBars)

                    when (deviceScreenConfiguration) {
                        DeviceScreenConfiguration.MOBILE_PORTRAIT -> {
                            Column(
                                modifier = rootModifier,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
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
                                            WeatherContentPortrait(
                                                weather = weather!!,
                                                deviceScreenConfiguration = deviceScreenConfiguration,
                                                isDarkTheme = isDarkTheme,
                                                urlProvider = viewModel.urlProvider,
                                                currentLang = currentLang,
                                                imageQuality = imageQuality,
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
                        }

                        DeviceScreenConfiguration.MOBILE_LANDSCAPE -> {
                            when (screenState) {
                                WeatherScreenState.Idle -> {
                                    WeatherIdleState(
                                        modifier = rootModifier,
                                    )
                                }

                                WeatherScreenState.Loading -> {
                                    WeatherLoadingState(
                                        modifier = rootModifier
                                    )
                                }

                                WeatherScreenState.NoWeather -> {
                                    NoWeatherItem(
                                        modifier = rootModifier,
                                        onRetry = { viewModel.retryLastOperation(currentLang, apiKey, amountOfDays) }
                                    )
                                }

                                WeatherScreenState.WeatherObtained -> {
                                    if (weather != null) {
                                        WeatherContentLandscape(
                                            weather = weather!!,
                                            isDarkTheme = isDarkTheme,
                                            urlProvider = viewModel.urlProvider,
                                            imageQuality = imageQuality,
                                            currentLang = currentLang,
                                            modifier = rootModifier,
                                            deviceScreenConfiguration = deviceScreenConfiguration
                                        )
                                    } else {
                                        NoWeatherItem(
                                            modifier = rootModifier,
                                            onRetry = { viewModel.retryLastOperation(currentLang, apiKey, amountOfDays) }
                                        )
                                    }
                                }
                            }
                        }

                        DeviceScreenConfiguration.TABLET_PORTRAIT->{
                            Column(
                                modifier = rootModifier,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
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
                                            WeatherContentPortrait(
                                                weather = weather!!,
                                                deviceScreenConfiguration = deviceScreenConfiguration,
                                                isDarkTheme = isDarkTheme,
                                                urlProvider = viewModel.urlProvider,
                                                imageQuality = imageQuality,
                                                currentLang = currentLang,
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
                        }
                        DeviceScreenConfiguration.TABLET_LANDSCAPE,
                        DeviceScreenConfiguration.DESKTOP -> {
                            Column(
                                modifier = rootModifier
                                    .padding(top = 48.dp),
                                verticalArrangement = Arrangement.spacedBy(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
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
                                            WeatherContentLandscape(
                                                weather = weather!!,
                                                isDarkTheme = isDarkTheme,
                                                urlProvider = viewModel.urlProvider,
                                                imageQuality = imageQuality,
                                                currentLang = currentLang,
                                                deviceScreenConfiguration = deviceScreenConfiguration,
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
                        }
                    }
                }
            }

            LoadingDialog(
                title = Res.string.loading_dialog_title,
                message = Res.string.loading_dialog_text,
                showDialog = screenState == WeatherScreenState.Loading
            )
        }
    }
}