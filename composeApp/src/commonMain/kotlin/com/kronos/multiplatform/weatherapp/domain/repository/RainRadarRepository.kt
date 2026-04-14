package com.kronos.multiplatform.weatherapp.domain.repository

interface RainRadarRepository {

    suspend fun getRadarTileUrl(): String
}