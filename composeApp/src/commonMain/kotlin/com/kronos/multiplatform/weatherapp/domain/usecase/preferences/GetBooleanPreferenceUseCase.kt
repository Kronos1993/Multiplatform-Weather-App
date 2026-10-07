package com.kronos.multiplatform.weatherapp.domain.usecase.preferences

import com.kronos.multiplatform.weatherapp.core.usecase.UseCase

abstract class GetBooleanPreferenceUseCase : UseCase<GetBooleanPreferenceUseCase.Params, Boolean>() {
    data class Params(
        val key: String,
        val defaultValue: Boolean,
    )
}
