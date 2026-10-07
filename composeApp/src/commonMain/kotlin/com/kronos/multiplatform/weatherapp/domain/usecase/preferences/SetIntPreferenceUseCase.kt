package com.kronos.multiplatform.weatherapp.domain.usecase.preferences

import com.kronos.multiplatform.weatherapp.core.usecase.UseCase

abstract class SetIntPreferenceUseCase : UseCase<SetIntPreferenceUseCase.Params, Unit>() {
    data class Params(
        val key: String,
        val value: Int,
    )
}
