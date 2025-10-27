package com.kronos.multiplatform.weatherapp

import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kronos.multiplatform.weatherapp.components.theme.AppTheme
import com.kronos.multiplatform.weatherapp.core.preferences.PreferenceViewModel
import com.kronos.multiplatform.weatherapp.device.screen_config.DeviceScreenConfiguration
import com.kronos.multiplatform.weatherapp.features.add_city.AddCityScreen
import com.kronos.multiplatform.weatherapp.features.home.HomeScreen
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import weather_app.composeapp.generated.resources.Res
import weather_app.composeapp.generated.resources.api_key
import weather_app.composeapp.generated.resources.default_city_key
import weather_app.composeapp.generated.resources.default_city_value
import weather_app.composeapp.generated.resources.default_days_key
import weather_app.composeapp.generated.resources.default_days_values
import weather_app.composeapp.generated.resources.default_image_quality_key
import weather_app.composeapp.generated.resources.default_image_quality_value
import weather_app.composeapp.generated.resources.default_lang_key
import weather_app.composeapp.generated.resources.lang_preference_default_value
import weather_app.composeapp.generated.resources.theme_preference_default_value
import weather_app.composeapp.generated.resources.theme_preference_key

@Composable
@Preview
fun App() {
    val navController = rememberNavController()
    val viewModel = koinViewModel<PreferenceViewModel>()

    val themePreferenceKey = stringResource(Res.string.theme_preference_key)
    val themePreferenceDefault = stringResource(Res.string.theme_preference_default_value)
    val langPreferenceKey = stringResource(Res.string.default_lang_key)
    val langPreferenceDefault = stringResource(Res.string.lang_preference_default_value)
    val daysPreferenceKey = stringResource(Res.string.default_days_key)
    val daysPreferenceDefault = stringResource(Res.string.default_days_values)
    val defaultCityPreferenceKey = stringResource(Res.string.default_city_key)
    val defaultCityPreferenceDefault = stringResource(Res.string.default_city_value)
    val imageQualityPreferenceKey = stringResource(Res.string.default_image_quality_key)
    val imageQualityPreferenceDefault = stringResource(Res.string.default_image_quality_value)
    val apiKey = stringResource(Res.string.api_key)

    val isDarkTheme by viewModel.preferenceThemeFlow.collectAsStateWithLifecycle()
    val currentLang by viewModel.preferenceLangFlow.collectAsStateWithLifecycle()
    val amountDays by viewModel.preferenceDays.collectAsStateWithLifecycle()
    val imageQuality by viewModel.preferenceImageQuality.collectAsStateWithLifecycle()
    val defaultCity by viewModel.preferenceDefaultCity.collectAsStateWithLifecycle()
    val apiKeyRemember by remember { mutableStateOf(apiKey) }

    LaunchedEffect(Unit) {
        viewModel.getPreferenceTheme(
            themePreferenceKey,
            themePreferenceDefault
        )

        viewModel.getPreferenceLang(
            langPreferenceKey,
            langPreferenceDefault
        )

        viewModel.getPreferenceDays(
            daysPreferenceKey,
            if (daysPreferenceDefault.isEmpty()) {
                3
            } else daysPreferenceDefault.toInt()
        )

        viewModel.getPreferenceDefaultCity(
            defaultCityPreferenceKey,
            defaultCityPreferenceDefault
        )

        viewModel.getPreferenceImageQuality(
            imageQualityPreferenceKey,
            imageQualityPreferenceDefault
        )
    }

    LaunchedEffect(currentLang) {
        viewModel.changeLanguage(currentLang)
    }

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val deviceScreenConfiguration =
        DeviceScreenConfiguration.fromWindowSizeClass(windowSizeClass)

    val scope = rememberCoroutineScope()

    Scaffold(
    ) {
        AppTheme(
            darkTheme = isDarkTheme == stringResource(Res.string.theme_preference_default_value)
        ) {
            NavHost(
                navController = navController,
                startDestination = Destinations.HOME.name
            ) {
                composable(route = Destinations.HOME.name) {
                    HomeScreen(
                        navController,
                        isDarkTheme == stringResource(Res.string.theme_preference_default_value),
                        currentLang,
                        apiKeyRemember,
                        imageQuality,
                        amountDays,
                        defaultCity,
                        deviceScreenConfiguration = deviceScreenConfiguration,
                    )
                }
                composable(route = Destinations.ADD_CITY.name) {
                    AddCityScreen(
                        navController,
                        currentLang,
                        apiKeyRemember,
                        isDarkTheme == stringResource(Res.string.theme_preference_default_value),
                    )
                }
            }
        }
    }
}