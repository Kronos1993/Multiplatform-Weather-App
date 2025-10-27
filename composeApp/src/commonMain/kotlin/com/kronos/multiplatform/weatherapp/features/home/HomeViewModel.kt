package com.kronos.multiplatform.weatherapp.features.home

import androidx.lifecycle.viewModelScope
import com.kronos.multiplatform.weatherapp.core.notification.INotifications
import com.kronos.multiplatform.weatherapp.core.notification.NotificationGroup
import com.kronos.multiplatform.weatherapp.core.notification.NotificationType
import com.kronos.multiplatform.weatherapp.core.util.IChangeLang
import com.kronos.multiplatform.weatherapp.core.util.ICloseApp
import com.kronos.multiplatform.weatherapp.core.util.format
import com.kronos.multiplatform.weatherapp.core.viewmodel.ParentViewModel
import com.kronos.multiplatform.weatherapp.core.widget.IWidgetUpdater
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private var closeApp: ICloseApp,
    private var notifications: INotifications,
    private var widgetUpdater: IWidgetUpdater,
    private var changeLang: IChangeLang
) : ParentViewModel() {

    private val _weather = MutableStateFlow<Forecast?>(null)
    private var notificationTitle = ""
    private var notificationShortDetails = ""
    private var notificationLongDetails = ""

    fun initNotificationsString(
        notificationTitle: String,
        notificationShortDetails: String,
        notificationLongDetails: String
    ) {

        this.notificationTitle = notificationTitle
        this.notificationShortDetails = notificationShortDetails
        this.notificationLongDetails = notificationLongDetails
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

    fun updateAppLanguage(lang:String){
        changeLang.onLangChange(lang)
    }

    fun closeApp() {
        viewModelScope.launch(Dispatchers.IO) {
            createWeatherNotification()
            closeApp.closeApp()
        }
    }

    fun setForecast(it: Forecast) {
        viewModelScope.launch(Dispatchers.IO) {
            _weather.value = it
            widgetUpdater.updateAllWeatherWidgets()
        }
    }
}