package com.kronos.multiplatform.weatherapp.domain.usecase.preferences

import com.kronos.multiplatform.weatherapp.core.usecase.UseCase

abstract class SetBooleanPreferenceUseCase : UseCase<SetBooleanPreferenceUseCase.Params, Unit>() {
    data class Params(
        val key: String,
        val value: Boolean,
    )
}
