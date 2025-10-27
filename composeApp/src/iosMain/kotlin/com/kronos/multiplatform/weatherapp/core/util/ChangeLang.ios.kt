package com.kronos.multiplatform.weatherapp.core.util

import co.touchlab.kermit.Logger
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode
import platform.Foundation.languageIdentifier

actual class ChangeLang () : IChangeLang {
    override fun onLangChange(lang: String) {
        var currentLangCode = NSLocale.currentLocale.languageCode
        var currentLang = NSLocale.currentLocale.languageIdentifier
        Logger.i("Current System Lang Code $currentLangCode")
        Logger.i("Current System Lang  $currentLang")
    }

    override fun getSystemLang(): String {
        return "en"
    }
}