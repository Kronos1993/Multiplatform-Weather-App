package com.kronos.multiplatform.weatherapp.features.home.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.kronos.multiplatform.weatherapp.components.SettingRadioOptions
import com.kronos.multiplatform.weatherapp.core.preferences.PreferenceViewModel
import com.kronos.multiplatform.weatherapp.device.screen_config.DeviceScreenConfiguration
import com.kronos.multiplatform.weatherapp.domain.model.MeasureUnit
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import weather_app.composeapp.generated.resources.Res
import weather_app.composeapp.generated.resources.default_city_key
import weather_app.composeapp.generated.resources.default_city_value
import weather_app.composeapp.generated.resources.default_days_key
import weather_app.composeapp.generated.resources.default_days_values
import weather_app.composeapp.generated.resources.default_image_quality_key
import weather_app.composeapp.generated.resources.default_image_quality_value
import weather_app.composeapp.generated.resources.default_lang_key
import weather_app.composeapp.generated.resources.lang_preference_default_value
import weather_app.composeapp.generated.resources.measure_unit_key
import weather_app.composeapp.generated.resources.measure_unit_preference_default_value
import weather_app.composeapp.generated.resources.preference_app_theme_entries
import weather_app.composeapp.generated.resources.preference_app_theme_values
import weather_app.composeapp.generated.resources.preference_days_entries
import weather_app.composeapp.generated.resources.preference_image_quality_entries
import weather_app.composeapp.generated.resources.preference_image_quality_subtitle
import weather_app.composeapp.generated.resources.preference_image_quality_title
import weather_app.composeapp.generated.resources.preference_image_quality_values
import weather_app.composeapp.generated.resources.preference_lang_entries
import weather_app.composeapp.generated.resources.preference_lang_subtitle
import weather_app.composeapp.generated.resources.preference_lang_title
import weather_app.composeapp.generated.resources.preference_lang_values
import weather_app.composeapp.generated.resources.preference_measure_unit_entries
import weather_app.composeapp.generated.resources.preference_measure_unit_subtitle
import weather_app.composeapp.generated.resources.preference_measure_unit_title
import weather_app.composeapp.generated.resources.preference_measure_unit_values
import weather_app.composeapp.generated.resources.preference_theme_subtitle
import weather_app.composeapp.generated.resources.preference_theme_title
import weather_app.composeapp.generated.resources.theme_preference_default_value
import weather_app.composeapp.generated.resources.theme_preference_key

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navHost: NavHostController,
    isDarkTheme: Boolean,
    deviceScreenConfiguration: DeviceScreenConfiguration,
    currentLang: String,
    onLanguageChange: (String) -> Unit
) {
    val viewModel = koinViewModel<PreferenceViewModel>()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Claves y valores predeterminados desde recursos
    val langPreferenceKey = stringResource(Res.string.default_lang_key)
    val langPreferenceDefault = stringResource(Res.string.lang_preference_default_value)
    val themePreferenceKey = stringResource(Res.string.theme_preference_key)
    val themePreferenceDefault = stringResource(Res.string.theme_preference_default_value)
    val amountDaysPreferenceKey = stringResource(Res.string.default_days_key)
    val amountDaysPreferenceDefault = stringResource(Res.string.default_days_values)
    val imageQualityPreferenceKey = stringResource(Res.string.default_image_quality_key)
    val imageQualityPreferenceDefault = stringResource(Res.string.default_image_quality_value)
    val defaultCityPreferenceKey = stringResource(Res.string.default_city_key)
    val defaultCityPreferenceDefault = stringResource(Res.string.default_city_value)
    val measureUnitPreferenceKey = stringResource(Res.string.measure_unit_key)
    val measureUnitPreferenceDefault = stringResource(Res.string.measure_unit_preference_default_value)

    // Obtener preferencias al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadPreferences(
            langKey = langPreferenceKey,
            langDefault = langPreferenceDefault,
            themeKey = themePreferenceKey,
            themeDefault = themePreferenceDefault,
            daysKey = amountDaysPreferenceKey,
            daysDefault = amountDaysPreferenceDefault.toInt(),
            imageQualityKey = imageQualityPreferenceKey,
            imageQualityDefault = imageQualityPreferenceDefault,
            defaultCityKey = defaultCityPreferenceKey,
            defaultCityDefault = defaultCityPreferenceDefault,
            defaultMeasureUnitKey = measureUnitPreferenceKey,
            defaultMeasureUnitDefault = MeasureUnit.from(measureUnitPreferenceDefault)
        )
    }

    // Estados locales para las opciones seleccionadas
    val selectedLang by viewModel.preferenceLangFlow.collectAsStateWithLifecycle()
    val selectedDays by viewModel.preferenceDays.collectAsStateWithLifecycle()
    val selectedImageQuality by viewModel.preferenceImageQuality.collectAsStateWithLifecycle()
    val selectedTheme by viewModel.preferenceThemeFlow.collectAsStateWithLifecycle()
    val selectedMeasureUnit by viewModel.preferenceMeasureUnitFlow.collectAsStateWithLifecycle()


    // Listas de opciones desde recursos

    val langOptions = stringResource(Res.string.preference_lang_entries)
        .split(",")
        .mapIndexed { index, entry ->
            Pair(
                entry.trim(),
                stringResource(Res.string.preference_lang_values).split(",")[index].trim()
            )
        }

    val daysOptions = stringResource(Res.string.preference_days_entries)
        .split(",")
        .mapIndexed { index, entry ->
            Pair(
                entry.trim(),
                stringResource(Res.string.preference_days_entries).split(",")[index].trim()
            )
        }

    val imageQualityOptions = stringResource(Res.string.preference_image_quality_entries)
        .split(",")
        .mapIndexed { index, entry ->
            Pair(
                entry.trim(),
                stringResource(Res.string.preference_image_quality_values).split(",")[index].trim()
            )
        }


    val measureUnitOptions = stringResource(Res.string.preference_measure_unit_entries)
        .split(",")
        .mapIndexed { index, entry ->
            Pair(
                entry.trim(),
                stringResource(Res.string.preference_measure_unit_values).split(",")[index].trim()
            )
        }

    val themeOptions = stringResource(Res.string.preference_app_theme_entries)
        .split(",")
        .mapIndexed { index, entry ->
            Pair(
                entry.trim(),
                stringResource(Res.string.preference_app_theme_values).split(",")[index].trim()
            )
        }

    LaunchedEffect(viewModel.message) {
        if (viewModel.message.orEmpty().containsKey("error")) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = viewModel.message.orEmpty()["error"].orEmpty(),
                    duration = SnackbarDuration.Short
                )
                viewModel.message?.clear()
            }
        }
    }

    // UI
    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = MaterialTheme.colorScheme.error, // Fondo del Snackbar
                        contentColor = MaterialTheme.colorScheme.onError // Color del texto
                    )
                }
            },
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            containerColor = MaterialTheme.colorScheme.onPrimary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                key(currentLang) {
                    SettingRadioOptions(
                        title = stringResource(Res.string.preference_lang_title),
                        subtitle = stringResource(Res.string.preference_lang_subtitle),
                        textColor = Color.White,
                        iconTint = Color.White,
                        icon = Icons.Filled.Language,
                        iconDesc = stringResource(Res.string.preference_lang_subtitle),
                        options = langOptions,
                        selectedOption = selectedLang,
                        onOptionSelected = {
                            scope.launch {
                                viewModel.preferenceRepository.setPreference(langPreferenceKey, it)
                            }
                            viewModel.setPreferenceLang(it)
                            onLanguageChange(it)
                        }
                    )

                    SettingRadioOptions(
                        title = stringResource(Res.string.preference_theme_title),
                        subtitle = stringResource(Res.string.preference_theme_subtitle),
                        textColor = Color.White,
                        iconTint = Color.White,
                        icon = Icons.Filled.Palette,
                        iconDesc = stringResource(Res.string.preference_theme_subtitle),
                        options = themeOptions,
                        selectedOption = selectedTheme,
                        onOptionSelected = {
                            scope.launch {
                                viewModel.preferenceRepository.setPreference(themePreferenceKey, it)
                            }
                            viewModel.setPreferenceTheme(it)
                        }
                    )

                    /*SettingRadioOptions(
                        title = stringResource(Res.string.preference_days_title),
                        subtitle = stringResource(Res.string.preference_days_subtitle),
                        textColor = Color.White,
                        iconTint = Color.White,
                        icon = Icons.Filled.CalendarToday,
                        iconDesc = stringResource(Res.string.preference_days_subtitle),
                        options = daysOptions,
                        selectedOption = selectedDays,
                        onOptionSelected = {
                            viewModel.savePreference(amountDaysPreferenceKey, it.toInt())
                            viewModel.setPreferenceDays(it.toInt())
                        }
                    )*/

                    SettingRadioOptions(
                        title = stringResource(Res.string.preference_image_quality_title),
                        subtitle = stringResource(Res.string.preference_image_quality_subtitle),
                        textColor = Color.White,
                        iconTint = Color.White,
                        icon = Icons.Filled.Image,
                        iconDesc = stringResource(Res.string.preference_image_quality_subtitle),
                        options = imageQualityOptions,
                        selectedOption = selectedImageQuality,
                        onOptionSelected = {
                            scope.launch {
                                viewModel.preferenceRepository.setPreference(
                                    imageQualityPreferenceKey,
                                    it
                                )
                            }
                            viewModel.setPreferenceImageQuality(it)
                        }
                    )

                    SettingRadioOptions(
                        title = stringResource(Res.string.preference_measure_unit_title),
                        subtitle = stringResource(Res.string.preference_measure_unit_subtitle),
                        textColor = Color.White,
                        iconTint = Color.White,
                        icon = Icons.Filled.Scale,
                        iconDesc = stringResource(Res.string.preference_measure_unit_subtitle),
                        options = measureUnitOptions,
                        selectedOption = selectedMeasureUnit.value,
                        onOptionSelected = {
                            scope.launch {
                                viewModel.preferenceRepository.setPreference(
                                    measureUnitPreferenceKey,
                                    it
                                )
                            }
                            viewModel.setPreferenceMeasureUnit(it)
                        }
                    )

                }
            }
        }
    }
}
