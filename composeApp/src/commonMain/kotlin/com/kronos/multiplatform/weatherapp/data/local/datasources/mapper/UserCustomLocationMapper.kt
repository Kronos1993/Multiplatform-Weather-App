package com.kronos.multiplatform.weatherapp.data.local.datasources.mapper

import com.kronos.multiplatform.weatherapp.data.local.datasources.entity.UserCustomLocationEntity
import com.kronos.multiplatform.weatherapp.domain.model.UserCustomLocation


internal fun UserCustomLocationEntity.toDomain(): UserCustomLocation =
    UserCustomLocation(
        id = id,
        cityName = cityName,
        isCurrent = isCurrent,
        isSelected = isSelected,
        lat = lat,
        lon = lon,
        tempC = tempC,
        tempF = tempF,
        icon = icon
    )

internal fun UserCustomLocation.toEntity(): UserCustomLocationEntity =
    UserCustomLocationEntity(
        id = id,
        cityName = cityName,
        isCurrent = isCurrent,
        isSelected = isSelected,
        lat = lat!!,
        lon = lon!!,
        tempC = tempC,
        tempF = tempF,
        icon = icon
    )

