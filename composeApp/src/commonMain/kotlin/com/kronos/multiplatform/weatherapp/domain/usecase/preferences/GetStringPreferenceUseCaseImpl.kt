package com.kronos.multiplatform.weatherapp.domain.usecase.preferences

import com.kronos.multiplatform.weatherapp.core.preferences.repository.PreferenceRepository

class GetStringPreferenceUseCaseImpl(
    private val repository: PreferenceRepository,
) : GetStringPreferenceUseCase() {
    override suspend fun run(params: Params): String = repository.getPreference(params.key, params.defaultValue)
}
