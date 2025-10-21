package com.kronos.multiplatform.weatherapp.components.icons

import androidx.compose.ui.graphics.vector.ImageVector
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.MoonFallIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.NoWeatherIndicator
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
import kotlin.collections.Map as ____KtMap

public object WeatherAppIcons

private var __AllIcons: ____KtList<ImageVector>? = null

public val WeatherAppIcons.AllIcons: ____KtList<ImageVector>
  get() {
    if (__AllIcons != null) {
      return __AllIcons!!
    }
    __AllIcons= listOf(MoonFallIndicator, NoWeatherIndicator, RainyIndicator, SnowflakeIndicator,
        SnowflakeWeatherIndicator, SunIndicator, SunSunriseIndicator, TempIndicator,
        VisibilityIndicator, WaterDropsIndicator, WindIndicator)
    return __AllIcons!!
  }

private var __AllIconsNamed: ____KtMap<String, ImageVector>? = null

public val WeatherAppIcons.AllIconsNamed: ____KtMap<String, ImageVector>
  get() {
    if (__AllIconsNamed != null) {
      return __AllIconsNamed!!
    }
    __AllIconsNamed= mapOf("moonfallindicator" to MoonFallIndicator, "noweatherindicator" to
        NoWeatherIndicator, "rainyindicator" to RainyIndicator, "snowflakeindicator" to
        SnowflakeIndicator, "snowflakeweatherindicator" to SnowflakeWeatherIndicator, "sunindicator"
        to SunIndicator, "sunsunriseindicator" to SunSunriseIndicator, "tempindicator" to
        TempIndicator, "visibilityindicator" to VisibilityIndicator, "waterdropsindicator" to
        WaterDropsIndicator, "windindicator" to WindIndicator)
    return __AllIconsNamed!!
  }
