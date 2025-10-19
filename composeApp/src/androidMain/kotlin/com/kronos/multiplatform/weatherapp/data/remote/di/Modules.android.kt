package com.kronos.multiplatform.weatherapp.data.remote.di

import com.kronos.multiplatform.weatherapp.data.remote.ktor.KtorEngineFactory
import com.kronos.multiplatform.weatherapp.data.remote.ktor.util.KtorEngine
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformDataRemoteModules= module {
    singleOf(::KtorEngine).bind<KtorEngineFactory>()
}