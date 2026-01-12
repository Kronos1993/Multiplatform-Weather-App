package com.kronos.multiplatform.weatherapp.features.add_city

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.kronos.multiplatform.weatherapp.components.maps.markers.MapMarker
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
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast
import com.kronos.multiplatform.weatherapp.domain.repository.LocationRepository
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
    private val locationRepository: LocationRepository,
    private var notifications: INotifications,
    private val widgetUpdater: IWidgetUpdater,
    val loggerManager: ILogManager
) : ParentViewModel() {

    private val TAG = this::class.simpleName

    private val _markers = MutableStateFlow<List<MapMarker>>(listOf())
    val markers: StateFlow<List<MapMarker>> = _markers.asStateFlow()

    private val _currentLocation = MutableStateFlow<LocationModel?>(null)
    val currentLocation = _currentLocation.asStateFlow()

    private val _forecast = MutableStateFlow<Forecast?>(null)
    val forecast: StateFlow<Forecast?> = _forecast.asStateFlow()

    var isCurrentLocation by mutableStateOf(false )


    private val _markerSelected = MutableStateFlow<MapMarker?>(null)
    val markerSelected: StateFlow<MapMarker?> = _markerSelected.asStateFlow()

    private val _screenState = MutableStateFlow<AddCityScreenState>(AddCityScreenState.Idle)
    val screenState: StateFlow<AddCityScreenState> = _screenState.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var weatherPrefKey = ""
    private var notificationTitle = ""
    private var notificationShortDetails = ""
    private var notificationLongDetails = ""

    init {
        log("ViewModel initialized.",false)
    }

    fun initString(
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

    fun getGpsLocation(
        lang: String,
        apiKey: String,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _screenState.value = AddCityScreenState.Loading
                // Verificar si el GPS está activado
                if (!locationRepository.isLocationEnabled()) {
                    val msg = "Enable location"
                    _error.value = msg
                    _screenState.value = AddCityScreenState.NoCity
                }

                // Obtener ubicación actual
                _currentLocation.value = locationRepository.getCurrentLocation()

                if (_currentLocation.value != null) {
                    // Usar ubicación GPS obtenida
                    weatherRemoteRepository.getWeatherDataForecast(_currentLocation.value!!.latitude, _currentLocation.value!!.longitude, lang, apiKey, 1)
                        .onSuccess { forecast ->
                            log("Weather data received for ${forecast.location.name} (${forecast.location.lat}, ${forecast.location.lon})",false)
                            _forecast.value = forecast
                            isCurrentLocation = true
                            _screenState.value = AddCityScreenState.CityObtained
                        }
                        .onError { error ->
                            val msg = "Error getting weather: ${error.errorMessage}"
                            _error.value = msg
                            _screenState.value = AddCityScreenState.NoCity
                            log(msg, isError = true)
                        }
                } else {
                    // Fallback si no se pudo obtener ubicación GPS
                    val msg = "GPS error"
                    _error.value = msg
                    _screenState.value = AddCityScreenState.NoCity
                    log(msg, isError = true)
                }
            } catch (e: Exception) {
                log("Error adding location: ${e.message}", isError = true)
                _screenState.value = AddCityScreenState.NoCity
            }
        }
    }

    fun addLocation(measureUnit: MeasureUnit) {
        viewModelScope.launch(Dispatchers.IO) {
            val forecast = _forecast.value ?: run {
                log("No forecast available to add location.", isError = true)
                return@launch
            }

            val currentLocation = _currentLocation.value ?: run {
                LocationModel(
                    latitude = forecast.location.lat,
                    longitude = forecast.location.lon
                )
            }

            _screenState.value = AddCityScreenState.Loading

            try {
                val allLocations = userCustomLocationLocalRepository.listAll()
                val existingCurrent = allLocations.firstOrNull { it.isCurrent }

                // Desmarcar todas las ubicaciones
                allLocations.forEach {
                    userCustomLocationLocalRepository.saveLocation(
                        it.copy(isSelected = false)
                    )
                }

                val newLocation = UserCustomLocation(
                    cityName = forecast.location.name,
                    lat = currentLocation.latitude,
                    lon = currentLocation.longitude,
                    isSelected = true,
                    isCurrent = isCurrentLocation,
                    tempC = forecast.current.tempC,
                    tempF = forecast.current.tempF,
                    icon = urlProvider.getImageUrl(
                        forecast.current.condition.icon,
                        ""
                    )
                )

                val locationToSave = when {
                    // Caso: GPS y ya existe un current → actualizar
                    isCurrentLocation && existingCurrent != null -> {
                        existingCurrent.copy(
                            cityName = newLocation.cityName,
                            lat = newLocation.lat,
                            lon = newLocation.lon,
                            isSelected = true,
                            tempC = newLocation.tempC,
                            tempF = newLocation.tempF,
                            icon = newLocation.icon
                        )
                    }

                    // Caso: GPS nuevo o ciudad normal → insertar
                    else -> newLocation
                }

                userCustomLocationLocalRepository.saveLocation(locationToSave)

                weatherRemoteRepository.setLastWeatherForecast(
                    weatherPrefKey,
                    forecast
                )

                createWeatherNotification(measureUnit)
                widgetUpdater.updateAllWeatherWidgets()

                _screenState.value = AddCityScreenState.CityAdded
                log("Location saved successfully: ${locationToSave.cityName}", false)

            } catch (e: Exception) {
                val msg = "Error adding location: ${e.message}"
                _error.value = msg
                _screenState.value = AddCityScreenState.NoCity
                log(msg, isError = true)
            } finally {
                isCurrentLocation = false
                _currentLocation.value = null
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
                        "tempC" to location.tempC.toString(),
                        "tempF" to location.tempF.toString(),
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
        isCurrentLocation = false
        _currentLocation.value = null
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

    private fun createWeatherNotification(
        measureUnit: MeasureUnit
    ) {
        if (_forecast.value != null) {
            notifications.createNotification(
                notificationTitle.format(
                    if (measureUnit == MeasureUnit.INTERNATIONAL) _forecast.value!!.current.tempC else _forecast.value!!.current.tempF,
                    _forecast.value!!.location.region.orEmpty()
                ),
                notificationShortDetails.format(
                    _forecast.value!!.current.condition.description,
                    if (measureUnit == MeasureUnit.INTERNATIONAL) _forecast.value!!.current.feelslikeC else _forecast.value!!.current.feelslikeF
                ),
                notificationLongDetails.format(
                    _forecast.value!!.current.condition.description,
                    if (measureUnit == MeasureUnit.INTERNATIONAL) _forecast.value!!.current.feelslikeC else _forecast.value!!.current.feelslikeF,
                    if (measureUnit == MeasureUnit.INTERNATIONAL) _forecast.value!!.forecast.forecastDay[0].day.mintempC.toString() else _forecast.value!!.forecast.forecastDay[0].day.mintempF.toString(),
                    if (measureUnit == MeasureUnit.INTERNATIONAL) _forecast.value!!.forecast.forecastDay[0].day.maxtempC.toString() else _forecast.value!!.forecast.forecastDay[0].day.maxtempF.toString(),
                    _forecast.value!!.forecast.forecastDay[0].day.dailyChanceOfRain.toString()
                ),
                "https:${_forecast.value!!.current.condition.icon}",
                NotificationGroup.GENERAL,
                NotificationType.WEATHER_UPDATED
            )
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
