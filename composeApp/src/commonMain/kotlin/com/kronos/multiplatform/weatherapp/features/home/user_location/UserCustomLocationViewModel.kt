package com.kronos.multiplatform.weatherapp.features.home.user_location

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.kronos.multiplatform.weatherapp.core.logguer.ILogManager
import com.kronos.multiplatform.weatherapp.core.logguer.LogLevel
import com.kronos.multiplatform.weatherapp.core.notification.INotifications
import com.kronos.multiplatform.weatherapp.core.notification.NotificationGroup
import com.kronos.multiplatform.weatherapp.core.notification.NotificationType
import com.kronos.multiplatform.weatherapp.core.result.onError
import com.kronos.multiplatform.weatherapp.core.result.onSuccess
import com.kronos.multiplatform.weatherapp.core.util.format
import com.kronos.multiplatform.weatherapp.core.viewmodel.ParentViewModel
import com.kronos.multiplatform.weatherapp.core.widget.IWidgetUpdater
import com.kronos.multiplatform.weatherapp.data.remote.ktor.UrlProvider
import com.kronos.multiplatform.weatherapp.domain.model.MeasureUnit
import com.kronos.multiplatform.weatherapp.domain.model.UserCustomLocation
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast
import com.kronos.multiplatform.weatherapp.domain.usecase.user_custom_location.DeleteUserLocationUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.user_custom_location.ListUserLocationsUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.user_custom_location.SaveUserLocationUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.weather.GetWeatherForecastByCityUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.weather.GetWeatherForecastByCoordinatesUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.weather.SetLastWeatherForecastUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class UserCustomLocationViewModel(
    private val getWeatherForecastByCityUseCase: GetWeatherForecastByCityUseCase,
    private val getWeatherForecastByCoordinatesUseCase: GetWeatherForecastByCoordinatesUseCase,
    private val setLastWeatherForecastUseCase: SetLastWeatherForecastUseCase,
    private val listUserLocationsUseCase: ListUserLocationsUseCase,
    private val saveUserLocationUseCase: SaveUserLocationUseCase,
    private val deleteUserLocationUseCase: DeleteUserLocationUseCase,
    private var notifications: INotifications,
    private val widgetUpdater: IWidgetUpdater,
    val urlProvider: UrlProvider,
    private val loggerManager: ILogManager,
) : ParentViewModel() {
    private val TAG = this::class.simpleName

    private val _locations = MutableStateFlow<List<UserCustomLocation>>(listOf())
    val locations = _locations.asStateFlow()

    var currentLocation: UserCustomLocation by mutableStateOf(UserCustomLocation())

    private val _screenState =
        MutableStateFlow<UserCustomLocationScreenState>(UserCustomLocationScreenState.Idle)
    val screenState = _screenState.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var _resetSwipe = MutableStateFlow(false)
    var resetSwipe: StateFlow<Boolean> = _resetSwipe.asStateFlow()

    private var weatherPrefKey = ""
    private var notificationTitle = ""
    private var notificationShortDetails = ""
    private var notificationLongDetails = ""

    private var weatherUpdateJob: Job? = null

    fun initString(
        weatherPrefKey: String,
        notificationTitle: String,
        notificationShortDetails: String,
        notificationLongDetails: String,
    ) {
        this.weatherPrefKey = weatherPrefKey
        this.notificationTitle = notificationTitle
        this.notificationShortDetails = notificationShortDetails
        this.notificationLongDetails = notificationLongDetails
    }

    fun postResetSwipe(resetSwipe: Boolean) {
        _resetSwipe.value = resetSwipe
    }

    fun initLocations(lang: String, apiKey: String, days: Int, measureUnit: MeasureUnit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _screenState.value = UserCustomLocationScreenState.Loading
                _error.value = null

                val locationsFromDb = listUserLocationsUseCase(Unit)
                log("Custom location: ${locationsFromDb.size}", false)

                _locations.value = locationsFromDb.sortedWith(
                    compareByDescending<UserCustomLocation> { it.isCurrent }
                        .thenByDescending { it.isSelected },
                )
                _screenState.value = if (locationsFromDb.isNotEmpty()) {
                    UserCustomLocationScreenState.LocationsObtained
                } else {
                    UserCustomLocationScreenState.NoLocations
                }

                launchWeatherUpdates(locationsFromDb, lang, apiKey, days, measureUnit)
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private fun launchWeatherUpdates(
        locations: List<UserCustomLocation>,
        lang: String,
        apiKey: String,
        days: Int,
        measureUnit: MeasureUnit,
    ) {
        weatherUpdateJob?.cancel()
        weatherUpdateJob = viewModelScope.launch(Dispatchers.IO) {
            val jobs = locations.map { location ->
                log("Refreshing weather for: ${location.cityName}", false)
                launch {
                    fetchAndUpdateWeatherForLocation(location, lang, apiKey, days, measureUnit)
                }
            }
            jobs.joinAll()
        }
    }

    /**
     * Fetch del clima para una sola location.
     * Cuando llega la respuesta, actualiza solo esa location en el StateFlow
     * sin tocar las demás.
     */
    private suspend fun fetchAndUpdateWeatherForLocation(
        location: UserCustomLocation,
        lang: String,
        apiKey: String,
        days: Int,
        measureUnit: MeasureUnit,
    ) {
        try {
            val weatherResult = if (location.lat != null && location.lon != null) {
                getWeatherForecastByCoordinatesUseCase(
                    GetWeatherForecastByCoordinatesUseCase.Params(
                        location.lat!!,
                        location.lon!!,
                        lang,
                        apiKey,
                        days,
                    ),
                )
            } else {
                getWeatherForecastByCityUseCase(
                    GetWeatherForecastByCityUseCase.Params(
                        location.cityName,
                        lang,
                        apiKey,
                        days,
                    ),
                )
            }

            weatherResult
                .onSuccess { forecast ->
                    _locations.value = _locations.value.map { current ->
                        if (current.id == location.id) {
                            current.copy(
                                icon = forecast.current.condition.icon,
                                tempC = forecast.current.tempC,
                                tempF = forecast.current.tempF,
                                cityName = "${forecast.location.name}/${forecast.location.region}",
                            )
                        } else {
                            current
                        }
                    }

                    log("Weather refreshed for: ${forecast.location.name}", false)

                    if (location.isSelected) {
                        createWeatherNotification(forecast, measureUnit)
                        widgetUpdater.updateAllWeatherWidgets()
                    }
                }
                .onError { error ->
                    log("Weather error for ${location.cityName}: $error", isError = true)
                }
        } catch (e: Exception) {
            log("Exception fetching weather for ${location.cityName}: ${e.message}", isError = true)
        }
    }

    fun setLocationSelected(
        userLocation: UserCustomLocation,
        lang: String,
        apiKey: String,
        days: Int,
        measureUnit: MeasureUnit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _screenState.value = UserCustomLocationScreenState.Loading
            try {
                val allLocations = listUserLocationsUseCase(Unit)
                allLocations.forEach { location ->
                    val wasSelected = location.isSelected
                    location.isSelected = false
                    if (wasSelected) {
                        saveUserLocationUseCase(SaveUserLocationUseCase.Params(location))
                    }
                }

                userLocation.isSelected = true
                saveUserLocationUseCase(SaveUserLocationUseCase.Params(userLocation))

                val updatedLocations = _locations.value.map { location ->
                    when {
                        location.id == userLocation.id -> location.copy(isSelected = true)
                        location.isSelected -> location.copy(isSelected = false)
                        else -> location
                    }
                }

                _locations.value = updatedLocations
                getWeatherForecastByCoordinatesUseCase(
                    GetWeatherForecastByCoordinatesUseCase.Params(
                        userLocation.lat!!,
                        userLocation.lon!!,
                        lang,
                        apiKey,
                        days,
                    ),
                ).onSuccess {
                    setLastWeatherForecastUseCase(SetLastWeatherForecastUseCase.Params(weatherPrefKey, it))
                    createWeatherNotification(it, measureUnit)
                    widgetUpdater.updateAllWeatherWidgets()
                }
                _screenState.value = UserCustomLocationScreenState.LocationsObtained

                log("Custom location: ${userLocation.cityName} selected.", false)
            } catch (e: Exception) {
                _error.value = "Error selecting location: ${e.message}"
                log("Location selection error: ${e.message}", isError = true)
                _screenState.value = UserCustomLocationScreenState.LocationsObtained
            }
        }
    }

    fun removeLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                deleteUserLocationUseCase(DeleteUserLocationUseCase.Params(currentLocation))
                log("Custom location: ${currentLocation.cityName} removed.", false)

                val updatedLocations = _locations.value.filter { it.id != currentLocation.id }
                _locations.value = updatedLocations

                currentLocation = UserCustomLocation()

                _screenState.value = if (updatedLocations.isNotEmpty()) {
                    UserCustomLocationScreenState.LocationsObtained
                } else {
                    UserCustomLocationScreenState.NoLocations
                }
            } catch (e: Exception) {
                _error.value = "Error removing location: ${e.message}"
                log("Location removal error: ${e.message}", isError = true)
            }
        }
    }

    fun refreshLocations(lang: String, apiKey: String, days: Int, measureUnit: MeasureUnit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _screenState.value = UserCustomLocationScreenState.Loading

                val locationsFromDb = listUserLocationsUseCase(Unit)

                if (locationsFromDb.isEmpty()) {
                    _locations.value = emptyList()
                    _screenState.value = UserCustomLocationScreenState.NoLocations
                    return@launch
                }

                _locations.value = locationsFromDb
                _screenState.value = UserCustomLocationScreenState.LocationsObtained

                launchWeatherUpdates(locationsFromDb, lang, apiKey, days, measureUnit)
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private fun log(item: String, isError: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isError) {
                println("ERROR: $item")
                loggerManager.log(LogLevel.ERROR, TAG.orEmpty(), item)
            } else {
                println("INFO: $item")
                loggerManager.log(LogLevel.INFO, TAG.orEmpty(), item)
            }
        }
    }

    fun clean() {
        _error.value = null
    }

    fun retryLastOperation(lang: String, apiKey: String, days: Int, measureUnit: MeasureUnit) {
        refreshLocations(lang, apiKey, days, measureUnit)
    }

    private fun handleError(e: Exception) {
        _locations.value = listOf()
        _error.value = "Error: ${e.message}"
        _screenState.value = UserCustomLocationScreenState.NoLocations
        log("General error: ${e.message}", isError = true)
    }

    fun handleRemoveCurrentLocation(message: String) {
        _error.value = message
    }

    private fun createWeatherNotification(
        forecast: Forecast,
        measureUnit: MeasureUnit,
    ) {
        notifications.createNotification(
            notificationTitle.format(
                if (measureUnit == MeasureUnit.INTERNATIONAL) forecast.current.tempC else forecast.current.tempF,
                forecast.location.region.orEmpty(),
            ),
            notificationShortDetails.format(
                forecast.current.condition.description,
                if (measureUnit == MeasureUnit.INTERNATIONAL) forecast.current.feelslikeC else forecast.current.feelslikeF,
            ),
            notificationLongDetails.format(
                forecast.current.condition.description,
                if (measureUnit == MeasureUnit.INTERNATIONAL) forecast.current.feelslikeC else forecast.current.feelslikeF,
                if (measureUnit == MeasureUnit.INTERNATIONAL) forecast.forecast.forecastDay[0].day.mintempC.toString() else forecast.forecast.forecastDay[0].day.mintempF.toString(),
                if (measureUnit == MeasureUnit.INTERNATIONAL) forecast.forecast.forecastDay[0].day.maxtempC.toString() else forecast.forecast.forecastDay[0].day.maxtempF.toString(),
                forecast.forecast.forecastDay[0].day.dailyChanceOfRain.toString(),
            ),
            "https:${forecast.current.condition.icon}",
            NotificationGroup.GENERAL,
            NotificationType.WEATHER_UPDATED,
        )
    }

    fun onLeavingScreen() {
        weatherUpdateJob?.cancel()
        weatherUpdateJob = null
        log("Weather update job cancelled — left screen.", false)
    }
}

sealed class UserCustomLocationScreenState {
    object Idle : UserCustomLocationScreenState()

    object Loading : UserCustomLocationScreenState()

    object NoLocations : UserCustomLocationScreenState()

    object LocationsObtained : UserCustomLocationScreenState()
}
