package com.kronos.multiplatform.weatherapp.core.preferences

import androidx.lifecycle.viewModelScope
import com.kronos.multiplatform.weatherapp.core.util.IChangeLang
import com.kronos.multiplatform.weatherapp.core.viewmodel.ParentViewModel
import com.kronos.multiplatform.weatherapp.domain.model.MeasureUnit
import com.kronos.multiplatform.weatherapp.domain.usecase.preferences.GetIntPreferenceUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.preferences.GetStringPreferenceUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.preferences.SetBooleanPreferenceUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.preferences.SetDoublePreferenceUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.preferences.SetIntPreferenceUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.preferences.SetStringPreferenceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PreferenceViewModel(
    private val getStringPreferenceUseCase: GetStringPreferenceUseCase,
    private val getIntPreferenceUseCase: GetIntPreferenceUseCase,
    private val setStringPreferenceUseCase: SetStringPreferenceUseCase,
    private val setIntPreferenceUseCase: SetIntPreferenceUseCase,
    private val setBooleanPreferenceUseCase: SetBooleanPreferenceUseCase,
    private val setDoublePreferenceUseCase: SetDoublePreferenceUseCase,
    private val changeLang: IChangeLang,
) : ParentViewModel() {
    private var _preferenceLangFlow = MutableStateFlow("en")
    val preferenceLangFlow: StateFlow<String> = _preferenceLangFlow.asStateFlow()

    private var _preferenceThemeFlow = MutableStateFlow("light")
    val preferenceThemeFlow: StateFlow<String> = _preferenceThemeFlow.asStateFlow()

    private var _preferenceDays = MutableStateFlow(3)
    val preferenceDays: StateFlow<Int> = _preferenceDays.asStateFlow()

    private var _preferenceImageQuality = MutableStateFlow("low")
    val preferenceImageQuality: StateFlow<String> = _preferenceImageQuality.asStateFlow()

    private var _preferenceDefaultCity = MutableStateFlow("")
    val preferenceDefaultCity: StateFlow<String> = _preferenceDefaultCity.asStateFlow()

    private var _preferenceCurrentCityFlow = MutableStateFlow("")
    val preferenceCurrentCityFlow: StateFlow<String> = _preferenceCurrentCityFlow.asStateFlow()

    private var _preferenceMeasureUnitFlow = MutableStateFlow(MeasureUnit.INTERNATIONAL)
    val preferenceMeasureUnitFlow: StateFlow<MeasureUnit> = _preferenceMeasureUnitFlow.asStateFlow()

    private val _prefsLoaded = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _prefsLoaded
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    fun loadPreferences(
        langKey: String,
        langDefault: String,
        themeKey: String,
        themeDefault: String,
        daysKey: String,
        daysDefault: Int,
        imageQualityKey: String,
        imageQualityDefault: String,
        defaultCityKey: String,
        defaultCityDefault: String,
        defaultMeasureUnitKey: String,
        defaultMeasureUnitDefault: MeasureUnit,
    ) {
        viewModelScope.launch {
            try {
                val lang = getStringPreferenceUseCase(
                    GetStringPreferenceUseCase.Params(
                        langKey,
                        changeLang.getSystemLang().ifBlank { langDefault },
                    ),
                )
                _preferenceLangFlow.value = lang
                changeLang.onLangChange(lang)

                _preferenceThemeFlow.value =
                    getStringPreferenceUseCase(GetStringPreferenceUseCase.Params(themeKey, themeDefault))

                _preferenceDays.value =
                    getIntPreferenceUseCase(GetIntPreferenceUseCase.Params(daysKey, daysDefault))

                _preferenceImageQuality.value =
                    getStringPreferenceUseCase(GetStringPreferenceUseCase.Params(imageQualityKey, imageQualityDefault))

                _preferenceDefaultCity.value =
                    getStringPreferenceUseCase(GetStringPreferenceUseCase.Params(defaultCityKey, defaultCityDefault))

                val measureUnit = getStringPreferenceUseCase(
                    GetStringPreferenceUseCase.Params(
                        defaultMeasureUnitKey,
                        defaultMeasureUnitDefault.value,
                    ),
                )
                _preferenceMeasureUnitFlow.value = MeasureUnit.fromInt(measureUnit.toInt())

                _prefsLoaded.value = true
            } catch (e: Exception) {
                e.printStackTrace()
                message = hashMapOf("error" to e.message.orEmpty())
            }
        }
    }

    fun savePreference(key: String, value: Any) {
        viewModelScope.launch {
            try {
                when (value) {
                    is String -> setStringPreferenceUseCase(SetStringPreferenceUseCase.Params(key, value))
                    is Int -> setIntPreferenceUseCase(SetIntPreferenceUseCase.Params(key, value))
                    is Boolean -> setBooleanPreferenceUseCase(SetBooleanPreferenceUseCase.Params(key, value))
                    is Double -> setDoublePreferenceUseCase(SetDoublePreferenceUseCase.Params(key, value))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val err = HashMap<String, String>()
                err["error"] = e.message.orEmpty()
                message = (err)
            }
        }
    }

    fun getPreferenceLang(key: String, defaultValue: String) {
        viewModelScope.launch {
            _preferenceLangFlow.value = getStringPreferenceUseCase(GetStringPreferenceUseCase.Params(key, defaultValue))
            changeLang.onLangChange(_preferenceLangFlow.value)
        }
    }

    fun getPreferenceCurrentCity(key: String, defaultValue: String) {
        viewModelScope.launch {
            _preferenceCurrentCityFlow.value = getStringPreferenceUseCase(GetStringPreferenceUseCase.Params(key, defaultValue))
        }
    }

    fun getPreferenceTheme(key: String, defaultValue: String) {
        viewModelScope.launch {
            _preferenceThemeFlow.value = getStringPreferenceUseCase(GetStringPreferenceUseCase.Params(key, defaultValue))
        }
    }

    fun getPreferenceDays(key: String, defaultValue: Int) {
        viewModelScope.launch {
            _preferenceDays.value = getIntPreferenceUseCase(GetIntPreferenceUseCase.Params(key, defaultValue))
        }
    }

    fun getPreferenceImageQuality(key: String, defaultValue: String) {
        viewModelScope.launch {
            _preferenceImageQuality.value = getStringPreferenceUseCase(GetStringPreferenceUseCase.Params(key, defaultValue))
        }
    }

    fun getPreferenceDefaultCity(key: String, defaultValue: String) {
        viewModelScope.launch {
            _preferenceDefaultCity.value = getStringPreferenceUseCase(GetStringPreferenceUseCase.Params(key, defaultValue))
        }
    }

    fun setPreferenceLang(value: String) {
        _preferenceLangFlow.value = value
    }

    fun setPreferenceTheme(value: String) {
        _preferenceThemeFlow.value = value
    }

    fun setPreferenceDays(value: Int) {
        _preferenceDays.value = value
    }

    fun setPreferenceImageQuality(value: String) {
        _preferenceImageQuality.value = value
    }

    fun setPreferenceMeasureUnit(value: String) {
        _preferenceMeasureUnitFlow.value = MeasureUnit.from(value)
    }

    fun setPreferenceDefaultCity(value: String) {
        _preferenceDefaultCity.value = value
    }

    fun setPreferenceCurrentCity(value: Int) {
        _preferenceDays.value = value
    }

    fun changeLanguage(lang: String) {
        changeLang.onLangChange(lang)
    }

    fun getSystemLanguage() =
        changeLang.getSystemLang()
}
