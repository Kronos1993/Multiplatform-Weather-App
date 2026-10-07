package com.kronos.multiplatform.weatherapp.domain.usecase.radar.rain

import com.kronos.multiplatform.weatherapp.components.maps.layers.MapLayerTiles
import com.kronos.multiplatform.weatherapp.core.result.Error
import com.kronos.multiplatform.weatherapp.core.result.Result
import com.kronos.multiplatform.weatherapp.core.usecase.UseCase

abstract class GetMapLayerTilesUseCase : UseCase<Unit, Result<MapLayerTiles, Error>>()
