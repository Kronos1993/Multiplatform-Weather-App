package com.kronos.multiplatform.weatherapp.features.home.current_weather

import androidx.lifecycle.viewModelScope
import com.kronos.multiplatform.weatherapp.core.result.onError
import com.kronos.multiplatform.weatherapp.core.result.onSuccess
import com.kronos.multiplatform.weatherapp.core.viewmodel.ParentViewModel
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
    val urlProvider: UrlProvider,
) : ParentViewModel() {

    // States
    private val _weather = MutableStateFlow<Forecast?>(null)
    val weather = _weather.asStateFlow()

    private val _selectedUserLocation = MutableStateFlow<UserCustomLocation?>(null)
    val selectedUserLocation = _selectedUserLocation.asStateFlow()

    private val _screenState = MutableStateFlow<WeatherScreenState>(WeatherScreenState.Idle)
    val screenState = _screenState.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    // Inicialización
    fun initLocations(lang: String, apiKey: String, days: Int, defaultCity: String = "") {
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
                        getWeather(userLocation.cityName, lang, apiKey, days)
                    }

                    else -> {
                        if (locationRepository.isLocationEnabled()) {
                            // Intentar usar GPS o fallback
                            getGpsLocation(null, lang, apiKey, days, defaultCity)
                        } else {
                            getWeather(defaultCity, lang, apiKey, days)
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
                getWeather(currentLocation.latitude, currentLocation.longitude, lang, apiKey, days)
                saveCurrentLocation(currentLocation)
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
        defaultCity: String = ""
    ) {
        message = hashMapOf("warning" to "GPS desactivado, usando ubicación guardada")

        if (userLocation != null) {
            if (userLocation.lat != null && userLocation.lon != null) {
                getWeather(userLocation.lat!!, userLocation.lon!!, lang, apiKey, days)
            } else {
                getWeather(userLocation.cityName, lang, apiKey, days)
            }
        } else {
            // Ciudad por defecto
            getWeather(defaultCity, lang, apiKey, days)
        }
    }

    private fun handleLocationFallback(
        userLocation: UserCustomLocation?,
        lang: String,
        apiKey: String,
        days: Int,
        defaultCity: String = ""
    ) {
        message = hashMapOf("warning" to "No se pudo obtener ubicación GPS")

        if (userLocation != null) {
            if (userLocation.lat != null && userLocation.lon != null) {
                getWeather(userLocation.lat!!, userLocation.lon!!, lang, apiKey, days)
            } else {
                getWeather(userLocation.cityName, lang, apiKey, days)
            }
        } else {
            getWeather(defaultCity, lang, apiKey, days)
        }
    }

    private fun getWeather(lat: Double, lon: Double, lang: String, apiKey: String, days: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _screenState.value = WeatherScreenState.Loading

            weatherRemoteRepository.getWeatherDataForecast(lat, lon, lang, apiKey, days)
                .onSuccess { forecast ->
                    _weather.value = forecast
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

    private fun getWeather(city: String, lang: String, apiKey: String, days: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _screenState.value = WeatherScreenState.Loading

            weatherRemoteRepository.getWeatherDataForecast(city, lang, apiKey, days)
                .onSuccess { forecast ->
                    _weather.value = forecast
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
                    isCurrent = true,
                    isSelected = true,
                    lat = location.latitude,
                    lon = location.longitude
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
            } else {
                println("INFO: $item")
            }
        }
    }

    fun refreshWeather(lang: String, apiKey: String, days: Int) {
        initLocations(lang, apiKey, days)
    }

    fun clean() {
        _error.value = null
        message = null
    }

    fun clearWeather() {
        _weather.value = null
        _screenState.value = WeatherScreenState.NoWeather
    }

    fun retryLastOperation(lang: String, apiKey: String, days: Int) {
        refreshWeather(lang, apiKey, days)
    }

    private fun handleError(e: Exception) {
        _weather.value = null
        _error.value = "Error: ${e.message}"
        _screenState.value = WeatherScreenState.NoWeather
        log("General error: ${e.message}", isError = true)
    }
}

sealed class WeatherScreenState {
    object Idle : WeatherScreenState()
    object Loading : WeatherScreenState()
    object NoWeather : WeatherScreenState()
    object WeatherObtained : WeatherScreenState()
}