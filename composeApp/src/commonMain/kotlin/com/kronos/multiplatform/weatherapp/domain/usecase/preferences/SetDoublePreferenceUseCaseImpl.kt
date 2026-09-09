package com.kronos.multiplatform.weatherapp.domain.usecase.preferences

import com.kronos.multiplatform.weatherapp.core.preferences.repository.PreferenceRepository

class SetDoublePreferenceUseCaseImpl(
    private val repository: PreferenceRepository,
) : SetDoublePreferenceUseCase() {
    override suspend fun run(params: Params) = repository.setPreference(params.key, params.value)
}
