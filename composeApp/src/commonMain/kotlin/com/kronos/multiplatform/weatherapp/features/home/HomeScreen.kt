package com.kronos.multiplatform.weatherapp.features.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.kronos.multiplatform.weatherapp.Destinations
import com.kronos.multiplatform.weatherapp.components.ConfirmDialog
import com.kronos.multiplatform.weatherapp.components.ScrollableTabView
import com.kronos.multiplatform.weatherapp.components.TabItem
import com.kronos.multiplatform.weatherapp.core.util.BackPressHandlerEffect
import com.kronos.multiplatform.weatherapp.core.viewmodel.PermissionViewModel
import com.kronos.multiplatform.weatherapp.device.screen_config.DeviceScreenConfiguration
import com.kronos.multiplatform.weatherapp.features.home.current_weather.WeatherScreen
import com.kronos.multiplatform.weatherapp.features.home.user_location.UserCustomLocationScreen
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import org.jetbrains.compose.resources.stringResource
import weather_app.composeapp.generated.resources.Res
import weather_app.composeapp.generated.resources.denied_location_permission_message
import weather_app.composeapp.generated.resources.denied_notification_permission_message
import weather_app.composeapp.generated.resources.exit_dialog_body
import weather_app.composeapp.generated.resources.exit_dialog_no
import weather_app.composeapp.generated.resources.exit_dialog_title
import weather_app.composeapp.generated.resources.exit_dialog_yes
import weather_app.composeapp.generated.resources.title_about
import weather_app.composeapp.generated.resources.title_location
import weather_app.composeapp.generated.resources.title_settings
import weather_app.composeapp.generated.resources.title_weather

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navHost: NavHostController,
    isDarkTheme: Boolean,
    currentLang: String,
    apiKey: String,
    imageQuality: String,
    amountOfDays: Int,
    defaultCity: String,
    deviceScreenConfiguration: DeviceScreenConfiguration,
) {
    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) {
        factory.createPermissionsController()
    }
    val permissionViewModel = viewModel {
        PermissionViewModel(controller)
    }

    BindEffect(permissionViewModel.controller)

    val permissionNotification = permissionViewModel.stateNotification
    val permissionLocation = permissionViewModel.stateLocation
    val snackbarHostState = remember { SnackbarHostState() }

    var currentPermissionFlow by remember { mutableStateOf<PermissionFlow>(PermissionFlow.Idle) }

    val notificationDeniedMessage =
        stringResource(Res.string.denied_notification_permission_message)
    val locationDeniedMessage = stringResource(Res.string.denied_location_permission_message)

    LaunchedEffect(permissionNotification, permissionLocation) {
        when (currentPermissionFlow) {
            PermissionFlow.Idle -> {
                // Evaluar qué permisos faltan
                when {
                    permissionNotification != PermissionState.Granted && permissionLocation != PermissionState.Granted -> {
                        // Faltan ambos - empezar con notificaciones
                        currentPermissionFlow = PermissionFlow.RequestingNotification
                        permissionViewModel.provideOrRequestNotificationPermission()
                    }

                    permissionNotification != PermissionState.Granted -> {
                        // Solo falta notificación
                        currentPermissionFlow = PermissionFlow.RequestingNotification
                        permissionViewModel.provideOrRequestNotificationPermission()
                    }

                    permissionLocation != PermissionState.Granted -> {
                        // Solo falta ubicación
                        currentPermissionFlow = PermissionFlow.RequestingLocation
                        permissionViewModel.provideOrRequestLocationPermission()
                    }

                    else -> {
                        // Ambos permisos concedidos
                        currentPermissionFlow = PermissionFlow.Completed
                    }
                }
            }

            PermissionFlow.RequestingNotification -> {
                // Esperar respuesta de notificación
                when (permissionNotification) {
                    PermissionState.Granted -> {
                        // Notificación concedida, verificar si falta ubicación
                        if (permissionLocation != PermissionState.Granted) {
                            currentPermissionFlow = PermissionFlow.RequestingLocation
                            permissionViewModel.provideOrRequestLocationPermission()
                        } else {
                            currentPermissionFlow = PermissionFlow.Completed
                        }
                    }

                    PermissionState.Denied -> {
                        // Notificación denegada, mostrar mensaje y continuar con ubicación si falta
                        snackbarHostState.showSnackbar(message = notificationDeniedMessage)
                        if (permissionLocation != PermissionState.Granted) {
                            currentPermissionFlow = PermissionFlow.RequestingLocation
                            permissionViewModel.provideOrRequestLocationPermission()
                        } else {
                            currentPermissionFlow = PermissionFlow.Completed
                        }
                    }

                    else -> {
                        // Estado inicial, no hacer nada
                    }
                }
            }

            PermissionFlow.RequestingLocation -> {
                // Esperar respuesta de ubicación
                when (permissionLocation) {
                    PermissionState.Granted -> {
                        currentPermissionFlow = PermissionFlow.Completed
                    }

                    PermissionState.Denied -> {
                        snackbarHostState.showSnackbar(message = locationDeniedMessage)
                        currentPermissionFlow = PermissionFlow.Completed
                    }

                    else -> {
                        // Estado inicial, no hacer nada
                    }
                }
            }

            else -> {
                // Flujo completado o otros estados
            }
        }
    }

    // Efecto separado para manejar cambios de estado después de las solicitudes
    LaunchedEffect(currentPermissionFlow) {
        when (currentPermissionFlow) {
            PermissionFlow.RequestedNotification -> {
                // Transición después de solicitar notificación
                currentPermissionFlow = PermissionFlow.RequestingNotification
            }

            PermissionFlow.RequestedLocation -> {
                // Transición después de solicitar ubicación
                currentPermissionFlow = PermissionFlow.RequestingLocation
            }

            else -> {
                // Otros estados
            }
        }
    }

    var showExitDialog by remember { mutableStateOf(false) }

    BackPressHandlerEffect(
        enabled = navHost.currentBackStackEntry?.destination?.route == Destinations.HOME.name
    ) {
        showExitDialog = true
    }


    val tabs = listOf(
        TabItem(
            stringResource(Res.string.title_weather),
            Icons.Filled.Cloud,
            Icons.Outlined.Cloud,
            1
        ) {
            WeatherScreen(
                deviceScreenConfiguration,
                currentLang,
                apiKey,
                imageQuality,
                amountOfDays,
                defaultCity,
                isDarkTheme
            )
        },

        TabItem(
            stringResource(Res.string.title_location),
            Icons.Filled.LocationOn,
            Icons.Outlined.LocationOn,
            1
        ) {
            UserCustomLocationScreen(
                navHost,
                deviceScreenConfiguration,
                currentLang,
                apiKey,
                imageQuality,
                amountOfDays,
                isDarkTheme
            )
        },

        TabItem(
            stringResource(Res.string.title_settings),
            Icons.Filled.Settings,
            Icons.Outlined.Settings,
            1
        ) {

        },

        TabItem(
            stringResource(Res.string.title_about),
            Icons.Filled.Info,
            Icons.Outlined.Info,
            1
        ) {

        },
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize().systemBarsPadding(),
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                }
            }
        ) { paddingValues ->
            ScrollableTabView(
                tabs = tabs,
                paddingValues = paddingValues
            )
        }

        ConfirmDialog(
            title = stringResource(Res.string.exit_dialog_title),
            body = stringResource(Res.string.exit_dialog_body),
            confirmText = stringResource(Res.string.exit_dialog_yes),
            onConfirm = {
                showExitDialog = false
            },
            cancelText = stringResource(Res.string.exit_dialog_no),
            onCancel = { showExitDialog = false },
            showDialog = showExitDialog
        )
    }
}

sealed class PermissionFlow {
    object Idle : PermissionFlow()
    object RequestedNotification : PermissionFlow()
    object RequestingNotification : PermissionFlow()
    object RequestedLocation : PermissionFlow()
    object RequestingLocation : PermissionFlow()
    object Completed : PermissionFlow()
}