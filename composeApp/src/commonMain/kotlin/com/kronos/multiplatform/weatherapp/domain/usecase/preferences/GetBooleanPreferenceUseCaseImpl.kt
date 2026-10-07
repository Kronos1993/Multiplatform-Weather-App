package com.kronos.multiplatform.weatherapp.domain.usecase.preferences

import com.kronos.multiplatform.weatherapp.core.preferences.repository.PreferenceRepository

class GetBooleanPreferenceUseCaseImpl(
    private val repository: PreferenceRepository,
) : GetBooleanPreferenceUseCase() {
    override suspend fun run(params: Params): Boolean = repository.getPreference(params.key, params.defaultValue)
}
