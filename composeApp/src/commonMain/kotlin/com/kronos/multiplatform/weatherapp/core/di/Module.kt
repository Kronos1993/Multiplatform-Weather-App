package com.kronos.multiplatform.weatherapp.core.di

import com.kronos.multiplatform.weatherapp.core.preferences.PreferenceViewModel
import com.kronos.multiplatform.weatherapp.core.preferences.datasource.PreferenceDataSource
import com.kronos.multiplatform.weatherapp.core.preferences.datasource.PreferenceDatasourceImpl
import com.kronos.multiplatform.weatherapp.core.preferences.repository.PreferenceRepository
import com.kronos.multiplatform.weatherapp.core.preferences.repository.PreferenceRepositoryImpl
import com.kronos.multiplatform.weatherapp.core.viewmodel.PermissionViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformModule: Module


val coreViewModel = module{
    //core viewmodels
    viewModelOf(::PermissionViewModel)
    singleOf(::PreferenceViewModel)
}


val preferenceModule = module {
    singleOf(::PreferenceDatasourceImpl).bind<PreferenceDataSource>()
    singleOf(::PreferenceRepositoryImpl).bind<PreferenceRepository>()
}