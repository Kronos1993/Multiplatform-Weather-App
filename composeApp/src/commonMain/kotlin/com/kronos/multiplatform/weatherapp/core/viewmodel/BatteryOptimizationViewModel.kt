package com.kronos.multiplatform.weatherapp.core.viewmodel

import androidx.lifecycle.viewModelScope
import com.kronos.multiplatform.weatherapp.core.util.IBatteryOptimizationHelper
import com.kronos.multiplatform.weatherapp.domain.usecase.preferences.GetBooleanPreferenceUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.preferences.GetDoublePreferenceUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.preferences.SetBooleanPreferenceUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.preferences.SetDoublePreferenceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import weather_app.composeapp.generated.resources.Res
import weather_app.composeapp.generated.resources.battery_optimization_dont_ask_key
import weather_app.composeapp.generated.resources.battery_optimization_snooze_until_key
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

class BatteryOptimizationViewModel(
    private val batteryOptimizationHelper: IBatteryOptimizationHelper,
    private val getBooleanPreferenceUseCase: GetBooleanPreferenceUseCase,
    private val setBooleanPreferenceUseCase: SetBooleanPreferenceUseCase,
    private val getDoublePreferenceUseCase: GetDoublePreferenceUseCase,
    private val setDoublePreferenceUseCase: SetDoublePreferenceUseCase,
) : ParentViewModel() {
    val isSupported: Boolean = batteryOptimizationHelper.isSupported()
    val hasOemAutoStartSettings: Boolean = batteryOptimizationHelper.hasOemAutoStartSettings()

    private val _isRestricted = MutableStateFlow(false)
    val isRestricted: StateFlow<Boolean> = _isRestricted.asStateFlow()

    // Home and Settings can share this instance (Settings is a tab inside Home), so each keeps its own flag.
    private val _showHomePrompt = MutableStateFlow(false)
    val showHomePrompt: StateFlow<Boolean> = _showHomePrompt.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    private var homePromptEvaluated = false

    init {
        refreshStatus()
    }

    fun refreshStatus() {
        _isRestricted.value = isSupported && !batteryOptimizationHelper.isIgnoringBatteryOptimizations()
        if (!_isRestricted.value) _showHomePrompt.value = false
    }

    @OptIn(ExperimentalTime::class)
    fun evaluateHomePrompt() {
        if (homePromptEvaluated) return
        homePromptEvaluated = true
        refreshStatus()
        if (!_isRestricted.value) return

        viewModelScope.launch {
            val dontAsk = getBooleanPreferenceUseCase(
                GetBooleanPreferenceUseCase.Params(getString(Res.string.battery_optimization_dont_ask_key), false),
            )
            val snoozeUntil = getDoublePreferenceUseCase(
                GetDoublePreferenceUseCase.Params(getString(Res.string.battery_optimization_snooze_until_key), 0.0),
            )
            val now = Clock.System.now().toEpochMilliseconds().toDouble()
            _showHomePrompt.value = !dontAsk && now >= snoozeUntil
        }
    }

    fun openFromSettings() {
        _showSettingsDialog.value = true
    }

    fun onAllow() {
        dismissAll()
        batteryOptimizationHelper.requestIgnoreBatteryOptimizations()
    }

    fun onOemSettings() {
        dismissAll()
        batteryOptimizationHelper.openOemAutoStartSettings()
    }

    @OptIn(ExperimentalTime::class)
    fun onNotNow() {
        val fromHome = _showHomePrompt.value
        dismissAll()
        if (!fromHome) return
        viewModelScope.launch {
            val snoozeUntil = Clock.System.now().plus(SNOOZE_DAYS.days).toEpochMilliseconds().toDouble()
            setDoublePreferenceUseCase(
                SetDoublePreferenceUseCase.Params(getString(Res.string.battery_optimization_snooze_until_key), snoozeUntil),
            )
        }
    }

    fun onDontAskAgain() {
        dismissAll()
        viewModelScope.launch {
            setBooleanPreferenceUseCase(
                SetBooleanPreferenceUseCase.Params(getString(Res.string.battery_optimization_dont_ask_key), true),
            )
        }
    }

    private fun dismissAll() {
        _showHomePrompt.value = false
        _showSettingsDialog.value = false
    }

    private companion object {
        const val SNOOZE_DAYS = 3
    }
}
