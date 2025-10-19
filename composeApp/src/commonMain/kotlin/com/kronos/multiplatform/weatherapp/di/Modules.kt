package com.kronos.multiplatform.weatherapp.di

import com.kronos.multiplatform.weatherapp.features.home.current_weather.WeatherViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    //ui viewmodels
    viewModelOf(::WeatherViewModel)
}