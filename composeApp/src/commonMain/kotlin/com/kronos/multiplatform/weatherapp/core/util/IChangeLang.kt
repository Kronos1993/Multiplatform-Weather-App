package com.kronos.multiplatform.weatherapp.core.util

interface IChangeLang {
    fun onLangChange(lang: String)
    fun getSystemLang(): String
}
