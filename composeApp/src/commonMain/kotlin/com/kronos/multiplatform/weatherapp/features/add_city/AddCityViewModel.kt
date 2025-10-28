package com.kronos.multiplatform.weatherapp.features.add_city

import androidx.lifecycle.viewModelScope
import com.kronos.multiplatform.weatherapp.components.maps.markers.MapMarker
import com.kronos.multiplatform.weatherapp.core.result.onError
import com.kronos.multiplatform.weatherapp.core.result.onSuccess
import com.kronos.multiplatform.weatherapp.core.viewmodel.ParentViewModel
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
) : ParentViewModel() {

    private val _markers = MutableStateFlow<List<MapMarker>>(listOf())
    val markers: StateFlow<List<MapMarker>> = _markers.asStateFlow()

    private val _forecast = MutableStateFlow<Forecast?>(null)
    val forecast: StateFlow<Forecast?> = _forecast.asStateFlow()

    private val _screenState = MutableStateFlow<AddCityScreenState>(AddCityScreenState.Idle)
    val screenState: StateFlow<AddCityScreenState> = _screenState.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun onMapClick(lat: Double, lon: Double, lang: String, apiKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _screenState.value = AddCityScreenState.Loading
            _error.value = null

            weatherRemoteRepository.getWeatherDataForecast(lat, lon, lang, apiKey, 1)
                .onSuccess { forecast ->
                    _forecast.value = forecast
                    _screenState.value = AddCityScreenState.CityObtained
                }
                .onError { error ->
                    _error.value = "Error getting weather: ${error.errorMessage}"
                    _screenState.value = AddCityScreenState.NoCity
                }
        }
    }

    fun addLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentForecast = _forecast.value
            if (currentForecast?.location != null) {
                _screenState.value = AddCityScreenState.Loading
                try {
                    userCustomLocationLocalRepository.listAll().forEach { location ->
                        userCustomLocationLocalRepository.saveLocation(
                            location.copy(isSelected = false)
                        )
                    }

                    // Guardar nueva ubicación seleccionada
                    val newLocation = UserCustomLocation(
                        cityName = currentForecast.location.name,
                        lat = currentForecast.location.lat,
                        lon = currentForecast.location.lon,
                        isSelected = true,
                        isCurrent = false
                    )
                    userCustomLocationLocalRepository.saveLocation(newLocation)
                    _screenState.value = AddCityScreenState.CityAdded
                } catch (e: Exception) {
                    _error.value = "Error adding location: ${e.message}"
                    _screenState.value = AddCityScreenState.NoCity
                }
            }
        }
    }

    fun getLocationMarkers() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = mutableListOf<MapMarker>()
            userCustomLocationLocalRepository.listAll().forEach { location ->
                val marker = MapMarker(
                    id = location.id.toString(),
                    latitude = location.lat?:0.0,
                    longitude = location.lon?:0.0,
                    title = location.cityName,
                    description = location.cityName,
                    customProperties = mapOf()
                )
                list.add(marker)
            }
            _markers.value = list.toList()
        }
    }

    fun dismissCityInfo() {
        _screenState.value = AddCityScreenState.Idle
    }

    fun clearError() {
        _error.value = null
    }
}

sealed class AddCityScreenState {
    object Idle : AddCityScreenState()
    object Loading : AddCityScreenState()
    object NoCity : AddCityScreenState()
    object CityObtained : AddCityScreenState()
    object CityAdded : AddCityScreenState()
}
