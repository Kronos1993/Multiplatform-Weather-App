package com.kronos.multiplatform.weatherapp.features.home.about

import androidx.lifecycle.viewModelScope
import com.kronos.multiplatform.weatherapp.core.util.AppInfo
import com.kronos.multiplatform.weatherapp.core.util.IExpectedIntents
import com.kronos.multiplatform.weatherapp.core.viewmodel.ParentViewModel
import kotlinx.coroutines.launch

class AboutViewModel(
    val intents: IExpectedIntents,
    private val appInfo: AppInfo
) : ParentViewModel() {

    fun openBrowser(url: String) {
        viewModelScope.launch {
            intents.openBrowser(url)
        }
    }

    var appVersion = appInfo.getAppVersion()

}