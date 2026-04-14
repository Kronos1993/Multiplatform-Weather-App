package com.kronos.multiplatform.weatherapp.features.home.current_weather

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
import com.kronos.multiplatform.weatherapp.data.local.location.LocationModel
import com.kronos.multiplatform.weatherapp.data.remote.ktor.UrlProvider
import com.kronos.multiplatform.weatherapp.domain.model.MeasureUnit
import com.kronos.multiplatform.weatherapp.domain.model.UserCustomLocation
import com.kronos.multiplatform.weatherapp.domain.model.alerts.WeatherAlert
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast
import com.kronos.multiplatform.weatherapp.domain.repository.LocationRepository
import com.kronos.multiplatform.weatherapp.domain.repository.RainRadarRepository
import com.kronos.multiplatform.weatherapp.domain.repository.UserCustomLocationLocalRepository
import com.kronos.multiplatform.weatherapp.domain.repository.WeatherRemoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val weatherRemoteRepository: WeatherRemoteRepository,
    private val userCustomLocationLocalRepository: UserCustomLocationLocalRepository,
    private val rainRadarRepository: RainRadarRepository,
    private val locationRepository: LocationRepository,
    private var notifications: INotifications,
    private val loggerManager: ILogManager,
    private val widgetUpdater: IWidgetUpdater,
    val urlProvider: UrlProvider,
) : ParentViewModel() {

    private val TAG = this::class.simpleName

    // States
    private val _weather = MutableStateFlow<Forecast?>(null)
    val weather = _weather.asStateFlow()

    private val _selectedUserLocation = MutableStateFlow<UserCustomLocation?>(null)
    val selectedUserLocation = _selectedUserLocation.asStateFlow()

    private val _rainRadarTiles = MutableStateFlow<String?>(null)
    val rainRadarTiles = _rainRadarTiles.asStateFlow()

    private val _screenState = MutableStateFlow<WeatherScreenState>(WeatherScreenState.Idle)
    val screenState = _screenState.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _selectedAlert = MutableStateFlow<WeatherAlert?>(null)
    val selectedAlert = _selectedAlert.asStateFlow()

    private val _showAlertInfo = MutableStateFlow(false)
    val showAlertInfo = _showAlertInfo.asStateFlow()

    private var weatherPrefKey = ""
    private var notificationTitle = ""
    private var notificationShortDetails = ""
    private var notificationLongDetails = ""
    private var gpsDisableMessage = ""
    private var getLocationErrorMessage = ""

    fun initNotificationsString(
        weatherPrefKey: String,
        notificationTitle: String,
        notificationShortDetails: String,
        notificationLongDetails: String,
        gpsDisableMessage: String,
        getLocationErrorMessage: String
    ) {

        this.weatherPrefKey = weatherPrefKey
        this.notificationTitle = notificationTitle
        this.notificationShortDetails = notificationShortDetails
        this.notificationLongDetails = notificationLongDetails
        this.gpsDisableMessage = gpsDisableMessage
        this.getLocationErrorMessage = getLocationErrorMessage
    }

    // Inicialización
    fun initLocations(
        lang: String,
        apiKey: String,
        days: Int,
        imageQuality: String,
        defaultCity: String = "",
        measureUnit: MeasureUnit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _screenState.value = WeatherScreenState.Loading
                _error.value = null

                _rainRadarTiles.value = rainRadarRepository.getRadarTileUrl()

                // 1. Buscar ubicación guardada del usuario
                var userLocation = userCustomLocationLocalRepository.getSelectedLocation()
                if (userLocation == null) {
                    userLocation = userCustomLocationLocalRepository.getCurrentLocation()
                }

                _selectedUserLocation.value = userLocation

                // 2. Decidir estrategia basada en la ubicación
                when {
                    userLocation != null && userLocation.isCurrent && userLocation.isSelected -> {
                        // Usar GPS para ubicación actual
                        getGpsLocation(
                            userLocation,
                            lang,
                            apiKey,
                            days,
                            imageQuality,
                            defaultCity,
                            measureUnit
                        )
                    }

                    userLocation != null -> {
                        // Usar ciudad guardada
                        getWeather(userLocation, lang, apiKey, days, imageQuality, measureUnit)
                    }

                    else -> {
                        if (locationRepository.isLocationEnabled()) {
                            // Intentar usar GPS o fallback
                            getGpsLocation(
                                null,
                                lang,
                                apiKey,
                                days,
                                imageQuality,
                                defaultCity,
                                measureUnit
                            )
                        } else {
                            _screenState.value = WeatherScreenState.NoWeather
                        }
                    }
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private suspend fun getGpsLocation(
        userLocation: UserCustomLocation?,
        lang: String,
        apiKey: String,
        days: Int,
        imageQuality: String,
        defaultCity: String = "",
        measureUnit: MeasureUnit
    ) {
        try {
            // Verificar si el GPS está activado
            if (!locationRepository.isLocationEnabled()) {
                handleGpsDisabled(
                    userLocation,
                    lang,
                    apiKey,
                    days,
                    imageQuality,
                    defaultCity,
                    measureUnit
                )
                return
            }

            // Obtener ubicación actual
            val currentLocation = locationRepository.getCurrentLocation()

            if (currentLocation != null) {
                // Usar ubicación GPS obtenida
                getWeather(
                    currentLocation,
                    lang,
                    apiKey,
                    days,
                    imageQuality,
                    measureUnit
                )
            } else {
                // Fallback si no se pudo obtener ubicación GPS
                handleLocationFallback(
                    userLocation,
                    lang,
                    apiKey,
                    days,
                    imageQuality,
                    defaultCity,
                    measureUnit
                )
            }
        } catch (e: Exception) {
            handleLocationFallback(
                userLocation,
                lang,
                apiKey,
                days,
                imageQuality,
                defaultCity,
                measureUnit
            )
        }
    }

    private fun handleGpsDisabled(
        userLocation: UserCustomLocation?,
        lang: String,
        apiKey: String,
        days: Int,
        imageQuality: String,
        defaultCity: String = "",
        measureUnit: MeasureUnit
    ) {
        message = hashMapOf("warning" to gpsDisableMessage)

        if (userLocation != null) {
            if (userLocation.lat != null && userLocation.lon != null) {
                getWeather(userLocation, lang, apiKey, days, imageQuality, measureUnit)
            } else {
                getWeather(userLocation.cityName, lang, apiKey, days, imageQuality, measureUnit)
            }
        } else {
            // Ciudad por defecto
            _screenState.value = WeatherScreenState.NoWeather
        }
    }

    private fun handleLocationFallback(
        userLocation: UserCustomLocation?,
        lang: String,
        apiKey: String,
        days: Int,
        imageQuality: String,
        defaultCity: String = "",
        measureUnit: MeasureUnit
    ) {
        message = hashMapOf("warning" to getLocationErrorMessage)

        if (userLocation != null) {
            if (userLocation.lat != null && userLocation.lon != null) {
                getWeather(userLocation, lang, apiKey, days, imageQuality, measureUnit)
            } else {
                getWeather(userLocation.cityName, lang, apiKey, days, imageQuality, measureUnit)
            }
        } else {
            _screenState.value = WeatherScreenState.NoWeather
        }
    }

    private fun getWeather(
        location: LocationModel,
        lang: String,
        apiKey: String,
        days: Int,
        imageQuality: String,
        measureUnit: MeasureUnit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _screenState.value = WeatherScreenState.Loading

            weatherRemoteRepository.getWeatherDataForecast(
                location.latitude,
                location.longitude,
                lang,
                apiKey,
                days
            )
                .onSuccess { forecast ->
                    _weather.value = forecast
                    weatherRemoteRepository.setLastWeatherForecast(weatherPrefKey, forecast)
                    saveCurrentLocation(
                        LocationModel(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            cityName = forecast.location.name,
                            tempC = forecast.current.tempC,
                            tempF = forecast.current.tempF,
                            icon = urlProvider.getImageUrl(
                                forecast.current.condition.icon,
                                imageQuality
                            ),
                            current = true
                        )
                    )
                    createWeatherNotification(measureUnit)
                    widgetUpdater.updateAllWeatherWidgets()
                    _screenState.value = WeatherScreenState.WeatherObtained
                    _error.value = null
                    log("Weather from coordinates acquired: ${forecast.location.name}", false)
                }
                .onError { error ->
                    _error.value = "Error getting weather: ${error.errorMessage}"
                    _screenState.value = WeatherScreenState.NoWeather
                    _weather.value = null
                    log("Weather error: ${error.errorMessage}", isError = true)
                }
        }
    }

    private fun getWeather(
        city: String,
        lang: String,
        apiKey: String,
        days: Int,
        imageQuality: String,
        measureUnit: MeasureUnit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _screenState.value = WeatherScreenState.Loading

            weatherRemoteRepository.getWeatherDataForecast(city, lang, apiKey, days)
                .onSuccess { forecast ->
                    _weather.value = forecast
                    weatherRemoteRepository.setLastWeatherForecast(weatherPrefKey, forecast)
                    saveCurrentLocation(
                        LocationModel(
                            latitude = forecast.location.lat,
                            longitude = forecast.location.lon,
                            cityName = city,
                            tempC = forecast.current.tempC,
                            tempF = forecast.current.tempF,
                            icon = urlProvider.getImageUrl(
                                forecast.current.condition.icon,
                                imageQuality
                            ),
                            current = false
                        )
                    )
                    createWeatherNotification(measureUnit)
                    widgetUpdater.updateAllWeatherWidgets()
                    _screenState.value = WeatherScreenState.WeatherObtained
                    _error.value = null
                    log("Weather from city acquired: ${forecast.location.name}", false)
                }
                .onError { error ->
                    _error.value = "Error getting weather: ${error.errorMessage}"
                    _screenState.value = WeatherScreenState.NoWeather
                    _weather.value = null
                    log("Weather error: ${error.errorMessage}", isError = true)
                }
        }
    }

    private fun getWeather(
        userLocation: UserCustomLocation,
        lang: String,
        apiKey: String,
        days: Int,
        imageQuality: String,
        measureUnit: MeasureUnit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _screenState.value = WeatherScreenState.Loading

            weatherRemoteRepository.getWeatherDataForecast(
                userLocation.lat ?: 0.0,
                userLocation.lon ?: 0.0,
                lang,
                apiKey,
                days
            )
                .onSuccess { forecast ->
                    _weather.value = forecast
                    weatherRemoteRepository.setLastWeatherForecast(weatherPrefKey, forecast)
                    saveCurrentLocation(
                        LocationModel(
                            latitude = userLocation.lat ?: 0.0,
                            longitude = userLocation.lon ?: 0.0,
                            cityName = userLocation.cityName,
                            tempC = forecast.current.tempC,
                            tempF = forecast.current.tempF,
                            icon = urlProvider.getImageUrl(
                                forecast.current.condition.icon,
                                imageQuality
                            ),
                            current = userLocation.isCurrent
                        )
                    )
                    createWeatherNotification(measureUnit)
                    widgetUpdater.updateAllWeatherWidgets()
                    _screenState.value = WeatherScreenState.WeatherObtained
                    _error.value = null
                    log("Weather from city acquired: ${forecast.location.name}", false)
                }
                .onError { error ->
                    _error.value = "Error getting weather: ${error.errorMessage}"
                    _screenState.value = WeatherScreenState.NoWeather
                    _weather.value = null
                    log("Weather error: ${error.errorMessage}", isError = true)
                }
        }
    }

    private fun saveCurrentLocation(location: LocationModel) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userLocation = UserCustomLocation(
                    cityName = location.cityName ?: "Current Location",
                    isCurrent = location.current,
                    isSelected = true,
                    lat = location.latitude,
                    lon = location.longitude,
                    tempC = location.tempC ?: 0.0,
                    tempF = location.tempF ?: 0.0,
                    icon = location.icon.orEmpty()
                )

                _selectedUserLocation.value?.id?.let { userLocation.id = it }

                userCustomLocationLocalRepository.saveLocation(userLocation)
                _selectedUserLocation.value = userLocation

                log("Current location saved: ${userLocation.cityName}", false)
            } catch (e: Exception) {
                log("Error saving location: ${e.message}", isError = true)
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

    fun refreshWeather(
        lang: String,
        apiKey: String,
        days: Int,
        imageQuality: String,
        defaultCity: String,
        measureUnit: MeasureUnit
    ) {
        initLocations(lang, apiKey, days, imageQuality, defaultCity, measureUnit)
    }

    fun clean() {
        _error.value = null
        message = null
    }

    fun clearWeather() {
        _weather.value = null
        _screenState.value = WeatherScreenState.NoWeather
    }

    fun retryLastOperation(
        lang: String,
        apiKey: String,
        days: Int,
        imageQuality: String,
        defaultCity: String,
        measureUnit: MeasureUnit
    ) {
        refreshWeather(lang, apiKey, days, imageQuality, defaultCity, measureUnit)
    }

    private fun handleError(e: Exception) {
        _weather.value = null
        _error.value = "Error: ${e.message}"
        _screenState.value = WeatherScreenState.NoWeather
        log("General error: ${e.message}", isError = true)
    }

    private fun createWeatherNotification(
        measureUnit: MeasureUnit
    ) {
        if (_weather.value != null) {
            notifications.createNotification(
                notificationTitle.format(
                    if (measureUnit == MeasureUnit.INTERNATIONAL) _weather.value!!.current.tempC else _weather.value!!.current.tempF,
                    _weather.value!!.location.region.orEmpty()
                ),
                notificationShortDetails.format(
                    _weather.value!!.current.condition.description,
                    if (measureUnit == MeasureUnit.INTERNATIONAL) _weather.value!!.current.feelslikeC else _weather.value!!.current.feelslikeF
                ),
                notificationLongDetails.format(
                    _weather.value!!.current.condition.description,
                    if (measureUnit == MeasureUnit.INTERNATIONAL) _weather.value!!.current.feelslikeC else _weather.value!!.current.feelslikeF,
                    if (measureUnit == MeasureUnit.INTERNATIONAL) _weather.value!!.forecast.forecastDay[0].day.mintempC.toString() else _weather.value!!.forecast.forecastDay[0].day.mintempF.toString(),
                    if (measureUnit == MeasureUnit.INTERNATIONAL) _weather.value!!.forecast.forecastDay[0].day.maxtempC.toString() else _weather.value!!.forecast.forecastDay[0].day.maxtempF.toString(),
                    _weather.value!!.forecast.forecastDay[0].day.dailyChanceOfRain.toString()
                ),
                "https:${_weather.value!!.current.condition.icon}",
                NotificationGroup.GENERAL,
                NotificationType.WEATHER_UPDATED
            )
        }
    }

    fun showAlertInfo(alert: WeatherAlert?) {
        _selectedAlert.value = alert
        _showAlertInfo.value = alert != null
    }
}

sealed class WeatherScreenState {
    object Idle : WeatherScreenState()
    object Loading : WeatherScreenState()
    object NoWeather : WeatherScreenState()
    object WeatherObtained : WeatherScreenState()
}