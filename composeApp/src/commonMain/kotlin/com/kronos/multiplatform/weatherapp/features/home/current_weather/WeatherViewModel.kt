package com.kronos.multiplatform.weatherapp.features.home.current_weather

import androidx.lifecycle.viewModelScope
import com.kronos.multiplatform.weatherapp.core.logguer.LogLevel
import com.kronos.multiplatform.weatherapp.core.logguer.LogManager
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
import com.kronos.multiplatform.weatherapp.domain.model.UserCustomLocation
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast
import com.kronos.multiplatform.weatherapp.domain.repository.LocationRepository
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
    private val locationRepository: LocationRepository,
    private var notifications: INotifications,
    private val loggerManager: LogManager,
    private val widgetUpdater: IWidgetUpdater,
    val urlProvider: UrlProvider,
) : ParentViewModel() {

    private val TAG = this::class.simpleName
    // States
    private val _weather = MutableStateFlow<Forecast?>(null)
    val weather = _weather.asStateFlow()

    private val _selectedUserLocation = MutableStateFlow<UserCustomLocation?>(null)
    val selectedUserLocation = _selectedUserLocation.asStateFlow()

    private val _screenState = MutableStateFlow<WeatherScreenState>(WeatherScreenState.Idle)
    val screenState = _screenState.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var weatherPrefKey = ""
    private var notificationTitle = ""
    private var notificationShortDetails = ""
    private var notificationLongDetails = ""

    fun initNotificationsString(
        weatherPrefKey: String,
        notificationTitle: String,
        notificationShortDetails: String,
        notificationLongDetails: String
    ) {

        this.weatherPrefKey = weatherPrefKey
        this.notificationTitle = notificationTitle
        this.notificationShortDetails = notificationShortDetails
        this.notificationLongDetails = notificationLongDetails
    }

    // Inicialización
    fun initLocations(
        lang: String,
        apiKey: String,
        days: Int,
        imageQuality: String,
        defaultCity: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _screenState.value = WeatherScreenState.Loading
                _error.value = null

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
                        getGpsLocation(userLocation, lang, apiKey, days, defaultCity)
                    }

                    userLocation != null -> {
                        // Usar ciudad guardada
                        getWeather(userLocation.cityName, lang, apiKey, days, imageQuality)
                    }

                    else -> {
                        if (locationRepository.isLocationEnabled()) {
                            // Intentar usar GPS o fallback
                            getGpsLocation(null, lang, apiKey, days, defaultCity)
                        } else {
                            getWeather(defaultCity, lang, apiKey, days, imageQuality)
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
        defaultCity: String = ""
    ) {
        try {
            // Verificar si el GPS está activado
            if (!locationRepository.isLocationEnabled()) {
                handleGpsDisabled(userLocation, lang, apiKey, days, defaultCity)
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
                    imageQuality
                )
            } else {
                // Fallback si no se pudo obtener ubicación GPS
                handleLocationFallback(userLocation, lang, apiKey, days, defaultCity)
            }
        } catch (e: Exception) {
            handleLocationFallback(userLocation, lang, apiKey, days, defaultCity)
        }
    }

    private fun handleGpsDisabled(
        userLocation: UserCustomLocation?,
        lang: String,
        apiKey: String,
        days: Int,
        imageQuality: String,
        defaultCity: String = ""
    ) {
        message = hashMapOf("warning" to "GPS desactivado, usando ubicación guardada")

        if (userLocation != null) {
            if (userLocation.lat != null && userLocation.lon != null) {
                getWeather(userLocation.lat!!, userLocation.lon!!, lang, apiKey, days, imageQuality)
            } else {
                getWeather(userLocation.cityName, lang, apiKey, days, imageQuality)
            }
        } else {
            // Ciudad por defecto
            getWeather(defaultCity, lang, apiKey, days, imageQuality)
        }
    }

    private fun handleLocationFallback(
        userLocation: UserCustomLocation?,
        lang: String,
        apiKey: String,
        days: Int,
        imageQuality: String,
        defaultCity: String = "",
    ) {
        message = hashMapOf("warning" to "No se pudo obtener ubicación GPS")

        if (userLocation != null) {
            if (userLocation.lat != null && userLocation.lon != null) {
                getWeather(userLocation.lat!!, userLocation.lon!!, lang, apiKey, days, imageQuality)
            } else {
                getWeather(userLocation.cityName, lang, apiKey, days, imageQuality)
            }
        } else {
            getWeather(defaultCity, lang, apiKey, days, imageQuality)
        }
    }

    private fun getWeather(
        lat: Double,
        lon: Double,
        lang: String,
        apiKey: String,
        days: Int,
        imageQuality: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _screenState.value = WeatherScreenState.Loading

            weatherRemoteRepository.getWeatherDataForecast(lat, lon, lang, apiKey, days)
                .onSuccess { forecast ->
                    _weather.value = forecast
                    weatherRemoteRepository.setLastWeatherForecast(weatherPrefKey, forecast)
                    saveCurrentLocation(
                        LocationModel(
                            latitude = forecast.location.lat,
                            longitude = forecast.location.lon,
                            cityName = forecast.location.name,
                            temp = forecast.current.tempC,
                            icon = urlProvider.getImageUrl(
                                forecast.current.condition.icon,
                                imageQuality
                            ),
                            current = false
                        )
                    )
                    createWeatherNotification()
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
        location: LocationModel,
        lang: String,
        apiKey: String,
        days: Int,
        imageQuality: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _screenState.value = WeatherScreenState.Loading

            weatherRemoteRepository.getWeatherDataForecast(location.latitude, location.longitude, lang, apiKey, days)
                .onSuccess { forecast ->
                    _weather.value = forecast
                    weatherRemoteRepository.setLastWeatherForecast(weatherPrefKey, forecast)
                    saveCurrentLocation(
                        LocationModel(
                            latitude = forecast.location.lat,
                            longitude = forecast.location.lon,
                            cityName = forecast.location.name,
                            temp = forecast.current.tempC,
                            icon = urlProvider.getImageUrl(
                                forecast.current.condition.icon,
                                imageQuality
                            ),
                            current = true
                        )
                    )
                    createWeatherNotification()
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
        imageQuality: String
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
                            temp = forecast.current.tempC,
                            icon = urlProvider.getImageUrl(
                                forecast.current.condition.icon,
                                imageQuality
                            ),
                            current = false
                        )
                    )
                    createWeatherNotification()
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
                    tempC = location.temp ?: 0.0,
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
                loggerManager.log(LogLevel.ERROR,TAG.orEmpty(),item)
            } else {
                println("INFO: $item")
                loggerManager.log(LogLevel.INFO,TAG.orEmpty(),item)
            }
        }
    }

    fun refreshWeather(lang: String, apiKey: String, days: Int, imageQuality: String) {
        initLocations(lang, apiKey, days, imageQuality)
    }

    fun clean() {
        _error.value = null
        message = null
    }

    fun clearWeather() {
        _weather.value = null
        _screenState.value = WeatherScreenState.NoWeather
    }

    fun retryLastOperation(lang: String, apiKey: String, days: Int, imageQuality: String) {
        refreshWeather(lang, apiKey, days, imageQuality)
    }

    private fun handleError(e: Exception) {
        _weather.value = null
        _error.value = "Error: ${e.message}"
        _screenState.value = WeatherScreenState.NoWeather
        log("General error: ${e.message}", isError = true)
    }

    private fun createWeatherNotification() {
        if (_weather.value != null) {
            notifications.createNotification(
                notificationTitle.format(
                    _weather.value!!.current.tempC,
                    _weather.value!!.location.region.orEmpty()
                ),
                notificationShortDetails.format(
                    _weather.value!!.current.condition.description,
                    _weather.value!!.current.feelslikeC
                ),
                notificationLongDetails.format(
                    _weather.value!!.current.condition.description,
                    _weather.value!!.current.feelslikeC,
                    _weather.value!!.forecast.forecastDay[0].day.mintempC.toString(),
                    _weather.value!!.forecast.forecastDay[0].day.maxtempC.toString(),
                    _weather.value!!.forecast.forecastDay[0].day.dailyChanceOfRain.toString()
                ),
                "https:${_weather.value!!.current.condition.icon}",
                NotificationGroup.GENERAL,
                NotificationType.FROM_APP
            )
        }
    }
}

sealed class WeatherScreenState {
    object Idle : WeatherScreenState()
    object Loading : WeatherScreenState()
    object NoWeather : WeatherScreenState()
    object WeatherObtained : WeatherScreenState()
}