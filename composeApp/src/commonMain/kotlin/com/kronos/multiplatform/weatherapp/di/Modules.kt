package com.kronos.multiplatform.weatherapp.di

import com.kronos.multiplatform.weatherapp.features.home.current_weather.WeatherViewModel
import com.kronos.multiplatform.weatherapp.features.home.user_location.UserCustomLocationViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    //ui viewmodels
    viewModelOf(::WeatherViewModel)
    viewModelOf(::UserCustomLocationViewModel)
}