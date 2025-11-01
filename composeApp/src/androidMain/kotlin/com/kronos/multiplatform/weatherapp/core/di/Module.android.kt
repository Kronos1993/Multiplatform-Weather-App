package com.kronos.multiplatform.weatherapp.core.di

import com.kronos.multiplatform.weatherapp.core.DevicePlatform
import com.kronos.multiplatform.weatherapp.core.Platform
import com.kronos.multiplatform.weatherapp.core.exception.ExceptionHandler
import com.kronos.multiplatform.weatherapp.core.exception.ExceptionHandlerImpl
import com.kronos.multiplatform.weatherapp.core.logguer.ILogManager
import com.kronos.multiplatform.weatherapp.core.logguer.LogManager
import com.kronos.multiplatform.weatherapp.core.notification.AppNotification
import com.kronos.multiplatform.weatherapp.core.notification.INotifications
import com.kronos.multiplatform.weatherapp.core.preferences.AppPreference
import com.kronos.multiplatform.weatherapp.core.preferences.IPreference
import com.kronos.multiplatform.weatherapp.core.util.AppInfo
import com.kronos.multiplatform.weatherapp.core.util.ExpectedIntents
import com.kronos.multiplatform.weatherapp.core.util.HapticFeedback
import com.kronos.multiplatform.weatherapp.core.util.ChangeLang
import com.kronos.multiplatform.weatherapp.core.util.CloseAppImpl
import com.kronos.multiplatform.weatherapp.core.util.IAppInfo
import com.kronos.multiplatform.weatherapp.core.util.IChangeLang
import com.kronos.multiplatform.weatherapp.core.util.ICloseApp
import com.kronos.multiplatform.weatherapp.core.util.IExpectedIntents
import com.kronos.multiplatform.weatherapp.core.util.IHapticFeedback
import com.kronos.multiplatform.weatherapp.core.widget.IWidgetUpdater
import com.kronos.multiplatform.weatherapp.core.widget.WidgetUpdater
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule = module{
    singleOf(::AppPreference).bind<IPreference>()
    singleOf(::AppNotification).bind<INotifications>()
    singleOf(::ExpectedIntents).bind<IExpectedIntents>()
    singleOf(::HapticFeedback).bind<IHapticFeedback>()
    singleOf(::AppInfo).bind<IAppInfo>()
    singleOf(::DevicePlatform).bind<Platform>()
    singleOf(::ExceptionHandlerImpl).bind<ExceptionHandler>()
    singleOf(::ChangeLang).bind<IChangeLang>()
    singleOf(::CloseAppImpl).bind<ICloseApp>()
    singleOf(::WidgetUpdater).bind<IWidgetUpdater>()
    singleOf(::LogManager).bind<ILogManager>()
}
