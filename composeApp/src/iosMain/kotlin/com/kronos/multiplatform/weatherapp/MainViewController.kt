package com.kronos.multiplatform.weatherapp

import androidx.compose.ui.window.ComposeUIViewController
import com.kronos.multiplatform.weatherapp.core.job.WeatherNotificationBackgroundTask
import com.kronos.multiplatform.weatherapp.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin ()
        val backgroundTask = WeatherNotificationBackgroundTask()
        backgroundTask.initNotificationStrings()
        //backgroundTask.schedule()
    }
) { App() }


/*
fun initNotificationManager(){
    //By default showPushNotification value is true.
    //When set showPushNotification to false foreground push  notification will not be shown.
    //You can still get notification content using #onPushNotification listener method.
    NotifierManager.initialize(
        NotificationPlatformConfiguration.Ios(
            showPushNotification = true,
        )
    )
}*/
