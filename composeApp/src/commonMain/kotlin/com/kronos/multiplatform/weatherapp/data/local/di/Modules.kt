package com.kronos.multiplatform.weatherapp.data.local.di

import com.kronos.multiplatform.weatherapp.data.local.datasources.UserCustomLocationLocalDataSource
import com.kronos.multiplatform.weatherapp.data.local.datasources.UserCustomLocationLocalDataSourceImpl
import com.kronos.multiplatform.weatherapp.data.repository.location.LocationRepositoryImpl
import com.kronos.multiplatform.weatherapp.data.repository.user_custom_location.UserCustomLocationLocalRepositoryImpl
import com.kronos.multiplatform.weatherapp.domain.repository.LocationRepository
import com.kronos.multiplatform.weatherapp.domain.repository.UserCustomLocationLocalRepository
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val commonDataLocalModules = module {
    //datasources
    single<UserCustomLocationLocalDataSource>(named(ParcelLocalDataSourceType.LOCAL)) {
        UserCustomLocationLocalDataSourceImpl(
            get()
        )
    }

    single{
        UserCustomLocationLocalRepositoryImpl (
            get(named(ParcelLocalDataSourceType.LOCAL))
        )
    }.bind<UserCustomLocationLocalRepository>()


    single{
        LocationRepositoryImpl (get())
    }.bind<LocationRepository>()

}

enum class ParcelLocalDataSourceType {
    LOCAL,
    DUMMY
}

expect val platformDataLocalModules: Module
