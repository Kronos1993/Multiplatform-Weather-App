package com.kronos.multiplatform.weatherapp.features.home.user_location

import androidx.lifecycle.viewModelScope
import com.kronos.multiplatform.weatherapp.core.result.onError
import com.kronos.multiplatform.weatherapp.core.result.onSuccess
import com.kronos.multiplatform.weatherapp.core.viewmodel.ParentViewModel
import com.kronos.multiplatform.weatherapp.data.remote.ktor.UrlProvider
import com.kronos.multiplatform.weatherapp.domain.model.UserCustomLocation
import com.kronos.multiplatform.weatherapp.domain.repository.LocationRepository
import com.kronos.multiplatform.weatherapp.domain.repository.UserCustomLocationLocalRepository
import com.kronos.multiplatform.weatherapp.domain.repository.WeatherRemoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserCustomLocationViewModel(
    private val weatherRemoteRepository: WeatherRemoteRepository,
    private val userCustomLocationLocalRepository: UserCustomLocationLocalRepository,
    private val locationRepository: LocationRepository,
    val urlProvider: UrlProvider,
) : ParentViewModel() {

    private val _locations = MutableStateFlow<List<UserCustomLocation>>(listOf())
    val locations = _locations.asStateFlow()

    private val _screenState =
        MutableStateFlow<UserCustomLocationScreenState>(UserCustomLocationScreenState.Idle)
    val screenState = _screenState.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var _resetSwipe = MutableStateFlow(false)
    var resetSwipe: StateFlow<Boolean> = _resetSwipe.asStateFlow()

    fun postResetSwipe(resetSwipe: Boolean) {
        _resetSwipe.value = resetSwipe
    }

    fun initLocations(lang: String, apiKey: String, days: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _screenState.value = UserCustomLocationScreenState.Loading
                _error.value = null

                val locationsFromDb = userCustomLocationLocalRepository.listAll()
                log("Custom location: ${locationsFromDb.size}", false)

                val updatedLocations = updateWeatherDataForLocations(
                    locationsFromDb,
                    lang,
                    apiKey,
                    days
                )

                _locations.value = updatedLocations
                _screenState.value = if (updatedLocations.isNotEmpty()) {
                    UserCustomLocationScreenState.LocationsObtained
                } else {
                    UserCustomLocationScreenState.NoLocations
                }

            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private suspend fun updateWeatherDataForLocations(
        locations: List<UserCustomLocation>,
        lang: String,
        apiKey: String,
        days: Int
    ): List<UserCustomLocation> {
        return locations.map { location ->
            try {
                val weatherResult = if (location.isCurrent && location.lat != null && location.lon != null) {
                    weatherRemoteRepository.getWeatherDataForecast(
                        location.lat!!,
                        location.lon!!,
                        lang,
                        apiKey,
                        days
                    )
                } else {
                    weatherRemoteRepository.getWeatherDataForecast(
                        location.cityName,
                        lang,
                        apiKey,
                        days
                    )
                }

                weatherResult
                    .onSuccess { forecast ->
                        location.icon = forecast.current.condition.icon
                        location.tempC = forecast.current.tempC
                        location.cityName = "${forecast.location.name}/${forecast.location.region}"
                        log("Location from coordinates acquired: ${forecast.location.name}", false)
                    }
                    .onError { error ->
                        log("Location error for ${location.cityName}: $error", isError = true)
                    }
            } catch (e: Exception) {
                log("Error updating weather for ${location.cityName}: ${e.message}", isError = true)
            }
            location
        }
    }

    fun addLocation(cityName: String,lat:Double,lon:Double, lang: String, apiKey: String, days: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _screenState.value = UserCustomLocationScreenState.Loading
            try {
                val currentLocations = userCustomLocationLocalRepository.listAll()
                currentLocations.forEach { loc ->
                    loc.isSelected = false
                    userCustomLocationLocalRepository.saveLocation(loc)
                }

                val location = UserCustomLocation(cityName = cityName, lat = lat, lon = lon, isSelected = true)
                userCustomLocationLocalRepository.saveLocation(location)
                log("Custom location: $cityName added.", false)

                refreshLocations(lang, apiKey, days)
            } catch (e: Exception) {
                _error.value = "Error adding location: ${e.message}"
                _screenState.value = UserCustomLocationScreenState.LocationsObtained
                log("Location error: ${e.message}", isError = true)
            }
        }
    }

    fun setLocationSelected(
        userLocation: UserCustomLocation,
        lang: String,
        apiKey: String,
        days: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _screenState.value = UserCustomLocationScreenState.Loading
            try {
                val allLocations = userCustomLocationLocalRepository.listAll()
                allLocations.forEach { location ->
                    location.isSelected = false
                    userCustomLocationLocalRepository.saveLocation(location)
                }

                userLocation.isSelected = true
                userCustomLocationLocalRepository.saveLocation(userLocation)
                log("Custom location: ${userLocation.cityName} selected.", false)

                refreshLocations(lang, apiKey, days)
            } catch (e: Exception) {
                _error.value = "Error selecting location: ${e.message}"
                log("Location selection error: ${e.message}", isError = true)
                _screenState.value = UserCustomLocationScreenState.LocationsObtained
            }
        }
    }

    fun removeLocation(location: UserCustomLocation, lang: String, apiKey: String, days: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                userCustomLocationLocalRepository.delete(location)
                log("Custom location: ${location.cityName} removed.", false)
                refreshLocations(lang, apiKey, days)
            } catch (e: Exception) {
                _error.value = "Error removing location: ${e.message}"
                log("Location removal error: ${e.message}", isError = true)
            }
        }
    }

    private fun log(item: String, isError: Boolean = false) {
        if (isError) {
            println("ERROR: $item")
        } else {
            println("INFO: $item")
        }
    }

    fun refreshLocations(lang: String, apiKey: String, days: Int) {
        initLocations(lang, apiKey, days)
    }

    fun clean() {
        _error.value = null
    }

    fun retryLastOperation(lang: String, apiKey: String, days: Int) {
        refreshLocations(lang, apiKey, days)
    }

    private fun handleError(e: Exception) {
        _locations.value = listOf()
        _error.value = "Error: ${e.message}"
        _screenState.value = UserCustomLocationScreenState.NoLocations
        log("General error: ${e.message}", isError = true)
    }

    fun handleRemoveCurrentLocation(message: String){
        _error.value = message
    }
}

sealed class UserCustomLocationScreenState {
    object Idle : UserCustomLocationScreenState()
    object Loading : UserCustomLocationScreenState()
    object NoLocations : UserCustomLocationScreenState()
    object LocationsObtained : UserCustomLocationScreenState()
}