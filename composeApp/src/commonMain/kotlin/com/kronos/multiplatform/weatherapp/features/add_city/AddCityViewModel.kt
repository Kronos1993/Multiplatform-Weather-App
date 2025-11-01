package com.kronos.multiplatform.weatherapp.features.add_city

import androidx.lifecycle.viewModelScope
import com.kronos.multiplatform.weatherapp.components.maps.markers.MapMarker
import com.kronos.multiplatform.weatherapp.core.logguer.LogLevel
import com.kronos.multiplatform.weatherapp.core.logguer.LogManager
import com.kronos.multiplatform.weatherapp.core.result.onError
import com.kronos.multiplatform.weatherapp.core.result.onSuccess
import com.kronos.multiplatform.weatherapp.core.viewmodel.ParentViewModel
import com.kronos.multiplatform.weatherapp.data.remote.ktor.UrlProvider
import com.kronos.multiplatform.weatherapp.domain.model.UserCustomLocation
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast
import com.kronos.multiplatform.weatherapp.domain.repository.UserCustomLocationLocalRepository
import com.kronos.multiplatform.weatherapp.domain.repository.WeatherRemoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddCityViewModel(
    private val weatherRemoteRepository: WeatherRemoteRepository,
    private val userCustomLocationLocalRepository: UserCustomLocationLocalRepository,
    private val urlProvider: UrlProvider,
    val loggerManager: LogManager
) : ParentViewModel() {

    private val TAG = this::class.simpleName

    private val _markers = MutableStateFlow<List<MapMarker>>(listOf())
    val markers: StateFlow<List<MapMarker>> = _markers.asStateFlow()

    private val _forecast = MutableStateFlow<Forecast?>(null)
    val forecast: StateFlow<Forecast?> = _forecast.asStateFlow()

    private val _markerSelected = MutableStateFlow<MapMarker?>(null)
    val markerSelected: StateFlow<MapMarker?> = _markerSelected.asStateFlow()

    private val _screenState = MutableStateFlow<AddCityScreenState>(AddCityScreenState.Idle)
    val screenState: StateFlow<AddCityScreenState> = _screenState.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        log("ViewModel initialized.",false)
    }

    fun setMarkerSelected(markerSelected: MapMarker?) {
        _markerSelected.value = markerSelected
        _screenState.value = AddCityScreenState.ShowCityInfo
        log("Marker selected: ${markerSelected?.title ?: "none"}",false)
    }

    fun onMapClick(lat: Double, lon: Double, lang: String, apiKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            log("Map clicked at lat=$lat, lon=$lon. Fetching weather...",false)
            _screenState.value = AddCityScreenState.Loading
            _error.value = null

            weatherRemoteRepository.getWeatherDataForecast(lat, lon, lang, apiKey, 1)
                .onSuccess { forecast ->
                    log("Weather data received for ${forecast.location.name} (${forecast.location.lat}, ${forecast.location.lon})",false)
                    _forecast.value = forecast
                    _screenState.value = AddCityScreenState.CityObtained
                }
                .onError { error ->
                    val msg = "Error getting weather: ${error.errorMessage}"
                    _error.value = msg
                    _screenState.value = AddCityScreenState.NoCity
                    log(msg, isError = true)
                }
        }
    }

    fun addLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentForecast = _forecast.value
            if (currentForecast?.location != null) {
                _screenState.value = AddCityScreenState.Loading
                log("Adding location: ${currentForecast.location.name}",false)

                try {
                    userCustomLocationLocalRepository.listAll().forEach { location ->
                        userCustomLocationLocalRepository.saveLocation(
                            location.copy(isSelected = false)
                        )
                    }

                    val newLocation = UserCustomLocation(
                        cityName = currentForecast.location.name,
                        lat = currentForecast.location.lat,
                        lon = currentForecast.location.lon,
                        isSelected = true,
                        isCurrent = false,
                        tempC = currentForecast.current.tempC,
                        icon = urlProvider.getImageUrl(currentForecast.current.condition.icon, "")
                    )

                    userCustomLocationLocalRepository.saveLocation(newLocation)
                    _screenState.value = AddCityScreenState.CityAdded
                    log("Location added successfully: ${newLocation.cityName}",false)

                } catch (e: Exception) {
                    val msg = "Error adding location: ${e.message}"
                    _error.value = msg
                    _screenState.value = AddCityScreenState.NoCity
                    log(msg, isError = true)
                }
            } else {
                log("No forecast available to add location.", isError = true)
            }
        }
    }

    fun getLocationMarkers() {
        viewModelScope.launch(Dispatchers.IO) {
            log("Loading saved location markers...",false)
            val list = mutableListOf<MapMarker>()
            userCustomLocationLocalRepository.listAll().forEach { location ->
                val marker = MapMarker(
                    id = location.id.toString(),
                    latitude = location.lat ?: 0.0,
                    longitude = location.lon ?: 0.0,
                    title = location.cityName,
                    description = "",
                    customProperties = mapOf(
                        "temp" to location.tempC.toString(),
                        "icon" to location.icon
                    )
                )
                list.add(marker)
            }
            _markers.value = list.toList()
            log("Loaded ${list.size} markers.",false)
        }
    }

    fun dismissCityInfo() {
        _screenState.value = AddCityScreenState.Idle
    }

    fun dismissMarkerInfo() {
        _screenState.value = AddCityScreenState.Idle
    }

    fun setError(error: String) {
        _error.value = error
        log("Manual error set: $error", isError = true)
    }

    fun clearError() {
        _error.value = null
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
}


sealed class AddCityScreenState {
    object Idle : AddCityScreenState()
    object Loading : AddCityScreenState()
    object NoCity : AddCityScreenState()
    object CityObtained : AddCityScreenState()
    object ShowCityInfo : AddCityScreenState()
    object CityAdded : AddCityScreenState()
}
