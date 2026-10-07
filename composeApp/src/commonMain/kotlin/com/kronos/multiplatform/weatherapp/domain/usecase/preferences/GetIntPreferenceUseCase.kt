package com.kronos.multiplatform.weatherapp.domain.usecase.preferences

import com.kronos.multiplatform.weatherapp.core.usecase.UseCase

abstract class GetIntPreferenceUseCase : UseCase<GetIntPreferenceUseCase.Params, Int>() {
    data class Params(
        val key: String,
        val defaultValue: Int,
    )
}
