package com.kronos.multiplatform.weatherapp.di

import com.kronos.multiplatform.weatherapp.domain.usecase.alerts.GetWeatherAlertsUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.alerts.GetWeatherAlertsUseCaseImpl
import com.kronos.multiplatform.weatherapp.domain.usecase.location.GetCurrentLocationUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.location.GetCurrentLocationUseCaseImpl
import com.kronos.multiplatform.weatherapp.domain.usecase.location.IsLocationEnabledUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.location.IsLocationEnabledUseCaseImpl
import com.kronos.multiplatform.weatherapp.domain.usecase.preferences.GetBooleanPreferenceUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.preferences.GetBooleanPreferenceUseCaseImpl
import com.kronos.multiplatform.weatherapp.domain.usecase.preferences.GetDoublePreferenceUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.preferences.GetDoublePreferenceUseCaseImpl
import com.kronos.multiplatform.weatherapp.domain.usecase.preferences.GetIntPreferenceUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.preferences.GetIntPreferenceUseCaseImpl
import com.kronos.multiplatform.weatherapp.domain.usecase.preferences.GetStringPreferenceUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.preferences.GetStringPreferenceUseCaseImpl
import com.kronos.multiplatform.weatherapp.domain.usecase.preferences.SetBooleanPreferenceUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.preferences.SetBooleanPreferenceUseCaseImpl
import com.kronos.multiplatform.weatherapp.domain.usecase.preferences.SetDoublePreferenceUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.preferences.SetDoublePreferenceUseCaseImpl
import com.kronos.multiplatform.weatherapp.domain.usecase.preferences.SetIntPreferenceUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.preferences.SetIntPreferenceUseCaseImpl
import com.kronos.multiplatform.weatherapp.domain.usecase.preferences.SetStringPreferenceUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.preferences.SetStringPreferenceUseCaseImpl
import com.kronos.multiplatform.weatherapp.domain.usecase.radar.rain.GetMapLayerTilesUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.radar.rain.GetMapLayerTilesUseCaseImpl
import com.kronos.multiplatform.weatherapp.domain.usecase.user_custom_location.DeleteUserLocationUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.user_custom_location.DeleteUserLocationUseCaseImpl
import com.kronos.multiplatform.weatherapp.domain.usecase.user_custom_location.GetCurrentUserLocationUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.user_custom_location.GetCurrentUserLocationUseCaseImpl
import com.kronos.multiplatform.weatherapp.domain.usecase.user_custom_location.GetSelectedUserLocationUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.user_custom_location.GetSelectedUserLocationUseCaseImpl
import com.kronos.multiplatform.weatherapp.domain.usecase.user_custom_location.ListUserLocationsUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.user_custom_location.ListUserLocationsUseCaseImpl
import com.kronos.multiplatform.weatherapp.domain.usecase.user_custom_location.SaveUserLocationUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.user_custom_location.SaveUserLocationUseCaseImpl
import com.kronos.multiplatform.weatherapp.domain.usecase.weather.GetLastWeatherForecastUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.weather.GetLastWeatherForecastUseCaseImpl
import com.kronos.multiplatform.weatherapp.domain.usecase.weather.GetWeatherForecastByCityUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.weather.GetWeatherForecastByCityUseCaseImpl
import com.kronos.multiplatform.weatherapp.domain.usecase.weather.GetWeatherForecastByCoordinatesUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.weather.GetWeatherForecastByCoordinatesUseCaseImpl
import com.kronos.multiplatform.weatherapp.domain.usecase.weather.GetWeatherUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.weather.GetWeatherUseCaseImpl
import com.kronos.multiplatform.weatherapp.domain.usecase.weather.SetLastWeatherForecastUseCase
import com.kronos.multiplatform.weatherapp.domain.usecase.weather.SetLastWeatherForecastUseCaseImpl
import com.kronos.multiplatform.weatherapp.features.add_city.AddCityViewModel
import com.kronos.multiplatform.weatherapp.features.home.HomeViewModel
import com.kronos.multiplatform.weatherapp.features.home.about.AboutViewModel
import com.kronos.multiplatform.weatherapp.features.home.current_weather.WeatherViewModel
import com.kronos.multiplatform.weatherapp.features.home.user_location.UserCustomLocationViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val useCaseModule = module {
    // location
    singleOf(::GetCurrentLocationUseCaseImpl).bind<GetCurrentLocationUseCase>()
    singleOf(::IsLocationEnabledUseCaseImpl).bind<IsLocationEnabledUseCase>()

    // radar/rain
    singleOf(::GetMapLayerTilesUseCaseImpl).bind<GetMapLayerTilesUseCase>()

    // user custom location
    singleOf(::GetSelectedUserLocationUseCaseImpl).bind<GetSelectedUserLocationUseCase>()
    singleOf(::GetCurrentUserLocationUseCaseImpl).bind<GetCurrentUserLocationUseCase>()
    singleOf(::SaveUserLocationUseCaseImpl).bind<SaveUserLocationUseCase>()
    singleOf(::ListUserLocationsUseCaseImpl).bind<ListUserLocationsUseCase>()
    singleOf(::DeleteUserLocationUseCaseImpl).bind<DeleteUserLocationUseCase>()

    // alerts
    singleOf(::GetWeatherAlertsUseCaseImpl).bind<GetWeatherAlertsUseCase>()

    // weather
    singleOf(::GetWeatherUseCaseImpl).bind<GetWeatherUseCase>()
    singleOf(::GetWeatherForecastByCityUseCaseImpl).bind<GetWeatherForecastByCityUseCase>()
    singleOf(::GetWeatherForecastByCoordinatesUseCaseImpl).bind<GetWeatherForecastByCoordinatesUseCase>()
    singleOf(::GetLastWeatherForecastUseCaseImpl).bind<GetLastWeatherForecastUseCase>()
    singleOf(::SetLastWeatherForecastUseCaseImpl).bind<SetLastWeatherForecastUseCase>()

    // preferences
    singleOf(::GetStringPreferenceUseCaseImpl).bind<GetStringPreferenceUseCase>()
    singleOf(::GetIntPreferenceUseCaseImpl).bind<GetIntPreferenceUseCase>()
    singleOf(::GetBooleanPreferenceUseCaseImpl).bind<GetBooleanPreferenceUseCase>()
    singleOf(::GetDoublePreferenceUseCaseImpl).bind<GetDoublePreferenceUseCase>()
    singleOf(::SetStringPreferenceUseCaseImpl).bind<SetStringPreferenceUseCase>()
    singleOf(::SetIntPreferenceUseCaseImpl).bind<SetIntPreferenceUseCase>()
    singleOf(::SetBooleanPreferenceUseCaseImpl).bind<SetBooleanPreferenceUseCase>()
    singleOf(::SetDoublePreferenceUseCaseImpl).bind<SetDoublePreferenceUseCase>()
}

val viewModelModule = module {
    // ui viewmodels
    viewModelOf(::HomeViewModel)
    viewModelOf(::WeatherViewModel)
    viewModelOf(::UserCustomLocationViewModel)
    viewModelOf(::AddCityViewModel)
    viewModelOf(::AboutViewModel)
}
