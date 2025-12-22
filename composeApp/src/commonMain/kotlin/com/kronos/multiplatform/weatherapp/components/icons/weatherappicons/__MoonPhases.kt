package com.kronos.multiplatform.weatherapp.components.icons.weatherappicons

import androidx.compose.ui.graphics.vector.ImageVector
import com.kronos.multiplatform.weatherapp.components.icons.WeatherAppIcons
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.moonphases.FirstQuarterMoonIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.moonphases.FullMoonIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.moonphases.NewMoonIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.moonphases.ThirdQuarterMoonIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.moonphases.WaningCrescentMoonIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.moonphases.WaningGibbousMoonIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.moonphases.WaxingCescentMoonIndicator
import com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.moonphases.WaxingGibbousMoonIndicator
import kotlin.collections.List as ____KtList

public object MoonPhasesGroup

public val WeatherAppIcons.MoonPhases: MoonPhasesGroup
  get() = MoonPhasesGroup

private var __AllIcons: ____KtList<ImageVector>? = null

public val MoonPhasesGroup.AllIcons: ____KtList<ImageVector>
  get() {
    if (__AllIcons != null) {
      return __AllIcons!!
    }
    __AllIcons= listOf(FirstQuarterMoonIndicator, FullMoonIndicator, NewMoonIndicator,
        ThirdQuarterMoonIndicator, WaningCrescentMoonIndicator, WaningGibbousMoonIndicator,
        WaxingCescentMoonIndicator, WaxingGibbousMoonIndicator)
    return __AllIcons!!
  }
