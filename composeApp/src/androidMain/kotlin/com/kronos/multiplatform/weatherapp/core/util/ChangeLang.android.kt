package com.kronos.multiplatform.weatherapp.core.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

actual class ChangeLang (
    val context: Context
) : IChangeLang {
    override fun onLangChange(lang: String) {
        val locale: Locale = if (lang == "not-set") { //use any value for default
            Locale.getDefault()
        } else {
            Locale(lang)
        }
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        context.resources.updateConfiguration(
            config,
            context.resources.displayMetrics
        )
    }
}