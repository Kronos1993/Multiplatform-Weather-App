package com.kronos.multiplatform.weatherapp.data.remote.di

import com.kronos.multiplatform.weatherapp.data.remote.datasources.WeatherRemoteDataSource
import com.kronos.multiplatform.weatherapp.data.remote.datasources.WeatherRemoteDataSourceImpl
import com.kronos.multiplatform.weatherapp.data.remote.ktor.KtorClientFactory
import com.kronos.multiplatform.weatherapp.data.remote.ktor.PrivateKtorClientFactoryImpl
import com.kronos.multiplatform.weatherapp.data.remote.ktor.PublicKtorClientFactoryImpl
import com.kronos.multiplatform.weatherapp.data.remote.ktor.UrlProvider
import com.kronos.multiplatform.weatherapp.data.remote.ktor.UrlProviderImp
import com.kronos.multiplatform.weatherapp.data.repository.weather.WeatherRemoteRepositoryImpl
import com.kronos.multiplatform.weatherapp.domain.repository.WeatherRemoteRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module


val commonRemoteModules = module {
    //ktor client
    single<KtorClientFactory>(named(KtorClientFactoryType.PUBLIC)) { PublicKtorClientFactoryImpl() }
    single<KtorClientFactory>(named(KtorClientFactoryType.PRIVATE)) { PrivateKtorClientFactoryImpl() }

    //datasources using a qualifier
    single {
        WeatherRemoteDataSourceImpl(
            get(),
            get(named(KtorClientFactoryType.PUBLIC)),
            get(),
        )
    }.bind<WeatherRemoteDataSource>()

    //repositories
    singleOf(::WeatherRemoteRepositoryImpl).bind<WeatherRemoteRepository>()

    //url provider
    singleOf(::UrlProviderImp).bind<UrlProvider>()
}

enum class KtorClientFactoryType {
    PUBLIC,
    PRIVATE
}

expect val platformDataRemoteModules: Module