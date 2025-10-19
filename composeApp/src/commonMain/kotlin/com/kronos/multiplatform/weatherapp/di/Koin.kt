package com.kronos.multiplatform.weatherapp.di

import com.kronos.multiplatform.weatherapp.core.di.coreViewModel
import com.kronos.multiplatform.weatherapp.core.di.platformModule
import com.kronos.multiplatform.weatherapp.core.di.preferenceModule
import com.kronos.multiplatform.weatherapp.data.local.di.commonDataLocalModules
import com.kronos.multiplatform.weatherapp.data.local.di.platformDataLocalModules
import com.kronos.multiplatform.weatherapp.data.remote.di.commonRemoteModules
import com.kronos.multiplatform.weatherapp.data.remote.di.platformDataRemoteModules
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config : KoinAppDeclaration? = null){
    startKoin {
        config?.invoke(this)
        modules(
            coreViewModel,
            viewModelModule,
            preferenceModule,
            commonRemoteModules,
            commonDataLocalModules,
            platformDataRemoteModules,
            platformModule,
            platformDataLocalModules,
        )
    }
}