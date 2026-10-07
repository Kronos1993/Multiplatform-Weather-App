package com.kronos.multiplatform.weatherapp.domain.usecase.preferences

import com.kronos.multiplatform.weatherapp.core.usecase.UseCase

abstract class GetStringPreferenceUseCase : UseCase<GetStringPreferenceUseCase.Params, String>() {
    data class Params(
        val key: String,
        val defaultValue: String,
    )
}
