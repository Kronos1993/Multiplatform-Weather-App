package com.kronos.multiplatform.weatherapp.data.local.di

import com.kronos.multiplatform.weatherapp.data.local.database.ApplicationDatabaseFactory
import com.kronos.multiplatform.weatherapp.data.local.database.LocalDatabaseFactory
import com.kronos.multiplatform.weatherapp.data.local.location.LocationDataSource
import com.kronos.multiplatform.weatherapp.data.local.location.LocationDataSourceImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformDataLocalModules = module{
    singleOf(::ApplicationDatabaseFactory).bind<LocalDatabaseFactory>()
    singleOf(::LocationDataSourceImpl).bind<LocationDataSource>()
}