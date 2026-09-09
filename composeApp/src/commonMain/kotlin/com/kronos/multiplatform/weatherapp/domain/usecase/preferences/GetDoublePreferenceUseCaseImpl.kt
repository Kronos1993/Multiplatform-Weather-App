package com.kronos.multiplatform.weatherapp.domain.usecase.preferences

import com.kronos.multiplatform.weatherapp.core.preferences.repository.PreferenceRepository

class GetDoublePreferenceUseCaseImpl(
    private val repository: PreferenceRepository,
) : GetDoublePreferenceUseCase() {
    override suspend fun run(params: Params): Double = repository.getPreference(params.key, params.defaultValue)
}
