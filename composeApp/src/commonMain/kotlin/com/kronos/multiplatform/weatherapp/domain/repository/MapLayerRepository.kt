package com.kronos.multiplatform.weatherapp.domain.repository

import com.kronos.multiplatform.weatherapp.components.maps.layers.MapLayerTiles
import com.kronos.multiplatform.weatherapp.core.result.Error
import com.kronos.multiplatform.weatherapp.core.result.Result

interface MapLayerRepository {

    suspend fun getLayerTiles(): Result<MapLayerTiles, Error>
}