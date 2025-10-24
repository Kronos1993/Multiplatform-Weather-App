package com.kronos.multiplatform.weatherapp.features.home.add_city

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
import com.kronos.multiplatform.weatherapp.components.MapView
import com.kronos.multiplatform.weatherapp.components.ShowCityInfoDialog
import com.kronos.multiplatform.weatherapp.components.button.IconButton
import org.koin.compose.viewmodel.koinViewModel
import weather_app.composeapp.generated.resources.Res
import weather_app.composeapp.generated.resources.loading_dialog_text
import weather_app.composeapp.generated.resources.loading_dialog_title

@Composable
fun AddCityScreen(
    navController: NavHostController,
    currentLang: String,
    apiKey: String,
    isDarkTheme: Boolean,
) {
    val viewModel = koinViewModel<AddCityViewModel>()
    val forecast by viewModel.forecast.collectAsStateWithLifecycle()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

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
                        onMapClick = { coordinate ->
                            viewModel.onMapClick(
                                coordinate.latitude,
                                coordinate.longitude,
                                currentLang,
                                apiKey
                            )
                        },
                        onMapLongClick = {},
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
                confirmText = "Add city",
                onConfirm = { viewModel.addLocation() },
                cancelText = "Cancel",
                onCancel = { viewModel.dismissCityInfo() },
                onClose = { viewModel.dismissCityInfo() }
            )
        }
    }
}