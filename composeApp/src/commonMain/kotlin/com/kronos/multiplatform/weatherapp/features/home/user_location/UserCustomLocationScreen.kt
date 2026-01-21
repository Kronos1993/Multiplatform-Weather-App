package com.kronos.multiplatform.weatherapp.features.home.user_location

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.kronos.multiplatform.weatherapp.Destinations
import com.kronos.multiplatform.weatherapp.components.UserCustomLocationIdleState
import com.kronos.multiplatform.weatherapp.components.UserCustomLocationLoadingState
import com.kronos.multiplatform.weatherapp.core.ui.components.ConfirmDialog
import com.kronos.multiplatform.weatherapp.core.ui.components.LoadingDialog
import com.kronos.multiplatform.weatherapp.core.ui.components.NoUserCustomLocationItem
import com.kronos.multiplatform.weatherapp.core.ui.components.PullToRefreshContainer
import com.kronos.multiplatform.weatherapp.core.ui.components.button.FabButton
import com.kronos.multiplatform.weatherapp.device.screen_config.DeviceScreenConfiguration
import com.kronos.multiplatform.weatherapp.domain.model.MeasureUnit
import com.kronos.multiplatform.weatherapp.features.home.user_location.content.GridList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import weather_app.composeapp.generated.resources.Res
import weather_app.composeapp.generated.resources.cant_delete_current_location
import weather_app.composeapp.generated.resources.current_weather_key
import weather_app.composeapp.generated.resources.delete_dialog_body
import weather_app.composeapp.generated.resources.delete_dialog_no
import weather_app.composeapp.generated.resources.delete_dialog_title
import weather_app.composeapp.generated.resources.delete_dialog_yes
import weather_app.composeapp.generated.resources.loading_dialog_text
import weather_app.composeapp.generated.resources.loading_dialog_title
import weather_app.composeapp.generated.resources.notification_long_details
import weather_app.composeapp.generated.resources.notification_long_details_fahrenheit
import weather_app.composeapp.generated.resources.notification_short_details
import weather_app.composeapp.generated.resources.notification_short_details_fahrenheit
import weather_app.composeapp.generated.resources.notification_title
import weather_app.composeapp.generated.resources.notification_title_fahrenheit

@Composable
fun UserCustomLocationScreen(
    navHost: NavHostController,
    deviceScreenConfiguration: DeviceScreenConfiguration,
    currentLang: String,
    apiKey: String,
    imageQuality: String,
    amountOfDays: Int,
    measureUnit: MeasureUnit,
    isDarkTheme: Boolean,
) {
    val viewModel = koinViewModel<UserCustomLocationViewModel>()
    val locations by viewModel.locations.collectAsStateWithLifecycle()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val resetSwipe by viewModel.resetSwipe.collectAsStateWithLifecycle()
    val listState = rememberLazyGridState()

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

    var showFab by remember { mutableStateOf(true) }
    var fabExpanded by remember { mutableStateOf(false) }
    LaunchedEffect(listState) {
        var lastScrollIndex = 0
        listState.scrollToItem(0) // Inicializar en la posición 0

        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { index ->
                showFab = index == 0 || index < lastScrollIndex
                lastScrollIndex = index
            }
    }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showConfirmDialog by mutableStateOf(false)

    LaunchedEffect(Unit) {
        viewModel.initLocations(currentLang, apiKey, amountOfDays, measureUnit)
    }

    // Manejo de errores
    LaunchedEffect(error) {
        error?.let { errorMessage ->
            if (errorMessage.isNotBlank()) {
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    duration = SnackbarDuration.Short
                )
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

                            else -> Color.White
                        }
                    )
                }
            },
            floatingActionButton = {
                AnimatedVisibility(
                    visible = showFab,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                ) {
                    FabButton(
                        icon = Icons.Filled.AddLocationAlt,
                        onClick = {
                            navHost.navigate(Destinations.ADD_CITY.name)
                            fabExpanded = !fabExpanded
                        }
                    )
                }
            },
        ) { paddingValues ->
            PullToRefreshContainer(
                innerPadding = paddingValues,
                isRefreshing = screenState == UserCustomLocationScreenState.Loading,
                onRefresh = {
                    viewModel.refreshLocations(currentLang, apiKey, amountOfDays, measureUnit)
                }
            ) {
                val rootModifier = Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(WindowInsets.navigationBars)

                Column(
                    modifier = rootModifier
                ) {
                    when (screenState) {
                        UserCustomLocationScreenState.Idle -> {
                            UserCustomLocationIdleState(
                                modifier = Modifier.fillMaxSize(),
                            )
                        }

                        UserCustomLocationScreenState.Loading -> {
                            UserCustomLocationLoadingState(
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        UserCustomLocationScreenState.NoLocations -> {
                            NoUserCustomLocationItem(
                                modifier = Modifier.fillMaxSize(),
                                onRetry = {
                                    viewModel.retryLastOperation(
                                        currentLang,
                                        apiKey,
                                        amountOfDays,
                                        measureUnit
                                    )
                                }
                            )
                        }

                        UserCustomLocationScreenState.LocationsObtained -> {
                            if (locations.isNotEmpty()) {
                                /*LaunchedEffect(listState) {
                                    snapshotFlow { listState.layoutInfo }
                                        .collect { layoutInfo ->
                                            val visibleItemCount = layoutInfo.visibleItemsInfo.size
                                            val totalItemCount = layoutInfo.totalItemsCount
                                            val firstVisibleItemIndex =
                                                layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0

                                            if (viewModel.loading == false && visibleItemCount + firstVisibleItemIndex >= totalItemCount) {
                                                if (viewModel.offset.value <= viewModel.total.value) {
                                                    viewModel.refreshLocations(lang = currentLang, apiKey = apiKey, days = amountOfDays)
                                                }
                                            }
                                        }
                                }*/

                                val cantDeleteLocation =
                                    stringResource(Res.string.cant_delete_current_location)
                                GridList(
                                    gridColumns = when (deviceScreenConfiguration) {
                                        DeviceScreenConfiguration.MOBILE_PORTRAIT -> {
                                            1
                                        }

                                        DeviceScreenConfiguration.MOBILE_LANDSCAPE,
                                        DeviceScreenConfiguration.TABLET_PORTRAIT -> {
                                            2
                                        }

                                        DeviceScreenConfiguration.TABLET_LANDSCAPE,
                                        DeviceScreenConfiguration.DESKTOP -> {
                                            3
                                        }
                                    },
                                    listState = listState,
                                    items = locations,
                                    urlProvider = viewModel.urlProvider,
                                    imageQuality = imageQuality,
                                    measureUnit = measureUnit,
                                    darkTheme = isDarkTheme,
                                    enableStartToEnd = false,
                                    startToEndIcon = Icons.Filled.Delete,
                                    onSwipeStartToEnd = {},
                                    enableEndToStart = true,
                                    endToStartIcon = Icons.Filled.Delete,
                                    onSwipeEndToStart = {
                                        if (it.isCurrent || it.isSelected) {
                                            viewModel.handleRemoveCurrentLocation(cantDeleteLocation)
                                            viewModel.postResetSwipe(true)
                                        } else {
                                            viewModel.currentLocation = it
                                            showConfirmDialog = true
                                        }
                                    },
                                    onItemClick = {
                                        viewModel.setLocationSelected(
                                            it,
                                            currentLang,
                                            apiKey,
                                            amountOfDays,
                                            measureUnit
                                        )
                                    },
                                    onItemLongClick = {},
                                    resetSwipe = resetSwipe,
                                    modifier = rootModifier
                                )
                            } else {
                                NoUserCustomLocationItem(
                                    modifier = Modifier.fillMaxSize(),
                                    onRetry = {
                                        viewModel.retryLastOperation(
                                            currentLang,
                                            apiKey,
                                            amountOfDays,
                                            measureUnit
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            LoadingDialog(
                title = Res.string.loading_dialog_title,
                message = Res.string.loading_dialog_text,
                showDialog = screenState == UserCustomLocationScreenState.Loading
            )

            ConfirmDialog(
                title = stringResource(Res.string.delete_dialog_title),
                body = stringResource(Res.string.delete_dialog_body),
                confirmText = stringResource(Res.string.delete_dialog_yes),
                cancelText = stringResource(Res.string.delete_dialog_no),
                showDialog = showConfirmDialog,
                onCancel = {
                    viewModel.postResetSwipe(true)
                    showConfirmDialog = false
                },
                onConfirm = {
                    viewModel.removeLocation()
                    viewModel.postResetSwipe(false)
                    showConfirmDialog = false
                }
            )

        }
    }
}