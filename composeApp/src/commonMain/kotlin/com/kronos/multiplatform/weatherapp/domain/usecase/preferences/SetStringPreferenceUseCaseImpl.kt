package com.kronos.multiplatform.weatherapp.domain.usecase.preferences

import com.kronos.multiplatform.weatherapp.core.preferences.repository.PreferenceRepository

class SetStringPreferenceUseCaseImpl(
    private val repository: PreferenceRepository,
) : SetStringPreferenceUseCase() {
    override suspend fun run(params: Params) = repository.setPreference(params.key, params.value)
}
