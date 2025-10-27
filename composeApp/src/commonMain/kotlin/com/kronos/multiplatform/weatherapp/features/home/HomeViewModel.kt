package com.kronos.multiplatform.weatherapp.features.home

import androidx.lifecycle.viewModelScope
import com.kronos.multiplatform.weatherapp.core.util.IChangeLang
import com.kronos.multiplatform.weatherapp.core.util.ICloseApp
import com.kronos.multiplatform.weatherapp.core.viewmodel.ParentViewModel
import com.kronos.multiplatform.weatherapp.domain.model.forecast.Forecast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private var closeApp: ICloseApp,
    private var changeLang: IChangeLang
) : ParentViewModel() {

    private val _weather = MutableStateFlow<Forecast?>(null)

    fun updateAppLanguage(lang:String){
        changeLang.onLangChange(lang)
    }

    fun closeApp() {
        viewModelScope.launch(Dispatchers.IO) {
            closeApp.closeApp()
        }
    }
}