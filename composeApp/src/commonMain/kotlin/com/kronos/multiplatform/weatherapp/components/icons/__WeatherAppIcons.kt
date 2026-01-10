package com.kronos.multiplatform.weatherapp.components.icons

import androidx.compose.ui.graphics.vector.ImageVector
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.AllIcons
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.CloudsIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.CompassIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.MoonFallIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.MoonPhases
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.NoWeatherIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.PressionIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.RainyIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.SnowflakeIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.SnowflakeWeatherIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.SunIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.SunSunriseIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.TempIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.VisibilityIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.WaterDropsIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.WindIndicator
import kotlin.collections.List as ____KtList

public object WeatherAppIcons

private var __AllIcons: ____KtList<ImageVector>? = null

public val WeatherAppIcons.AllIcons: ____KtList<ImageVector>
  get() {
    if (__AllIcons != null) {
      return __AllIcons!!
    }
    __AllIcons= MoonPhases.AllIcons + listOf(CloudsIndicator, CompassIndicator, MoonFallIndicator,
        NoWeatherIndicator, PressionIndicator, RainyIndicator, SnowflakeIndicator,
        SnowflakeWeatherIndicator, SunIndicator, SunSunriseIndicator, TempIndicator,
        VisibilityIndicator, WaterDropsIndicator, WindIndicator)
    return __AllIcons!!
  }
