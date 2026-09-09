package com.kronos.multiplatform.weatherapp.domain.usecase.radar.rain

import com.kronos.multiplatform.weatherapp.components.maps.layers.MapLayerTiles
import com.kronos.multiplatform.weatherapp.core.result.Error
import com.kronos.multiplatform.weatherapp.core.result.Result
import com.kronos.multiplatform.weatherapp.domain.repository.MapLayerRepository

class GetMapLayerTilesUseCaseImpl(
    private val repository: MapLayerRepository,
) : GetMapLayerTilesUseCase() {
    override suspend fun run(params: Unit): Result<MapLayerTiles, Error> = repository.getLayerTiles()
}
