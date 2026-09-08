package com.kronos.multiplatform.weatherapp

import androidx.compose.ui.window.ComposeUIViewController
import com.kronos.multiplatform.weatherapp.di.initKoin

fun MainViewController() = ComposeUIViewController { App() }


// Called once from AppDelegate.didFinishLaunchingWithOptions, before any
// background weather refresh or notification scheduling runs — Koin must be
// started before didFinishLaunchingWithOptions returns, since BGAppRefreshTask
// launches (and even the first cold launch) can run WeatherNotificationBackgroundTask
// before SwiftUI ever builds ComposeView/MainViewController. A no-arg wrapper is
// used (instead of calling initKoin() directly from Swift) because Kotlin default
// parameters don't bridge cleanly to Swift/Obj-C interop. Named startKoinIOS
// (not initKoinIOS) because Kotlin/Native's ObjC export renames any top-level
// function starting with "init" to "doInit..." (Cocoa initializer convention
// collision) — avoiding the "init" prefix keeps the Swift-visible name predictable.
fun startKoinIOS() {
    initKoin()
}


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
