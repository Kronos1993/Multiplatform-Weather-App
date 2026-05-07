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

public val WeatherAppIcons.MoonPhases: com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.MoonPhasesGroup
  get() = _root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.MoonPhasesGroup

private var __AllIcons: ____KtList<ImageVector>? = null

public val com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.MoonPhasesGroup.AllIcons: ____KtList<ImageVector>
  get() {
    if (_root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.__AllIcons != null) {
      return _root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.__AllIcons!!
    }
    _root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.__AllIcons = listOf(FirstQuarterMoonIndicator, FullMoonIndicator, NewMoonIndicator,
        ThirdQuarterMoonIndicator, WaningCrescentMoonIndicator, WaningGibbousMoonIndicator,
        WaxingCescentMoonIndicator, WaxingGibbousMoonIndicator)
    return _root_ide_package_.com.kronos.multiplatform.weatherapp.components.icons.weatherappicons.__AllIcons!!
  }
