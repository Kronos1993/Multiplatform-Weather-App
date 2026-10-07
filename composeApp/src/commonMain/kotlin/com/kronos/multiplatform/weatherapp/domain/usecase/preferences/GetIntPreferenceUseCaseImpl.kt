package com.kronos.multiplatform.weatherapp.domain.usecase.preferences

import com.kronos.multiplatform.weatherapp.core.preferences.repository.PreferenceRepository

class GetIntPreferenceUseCaseImpl(
    private val repository: PreferenceRepository,
) : GetIntPreferenceUseCase() {
    override suspend fun run(params: Params): Int = repository.getPreference(params.key, params.defaultValue)
}
