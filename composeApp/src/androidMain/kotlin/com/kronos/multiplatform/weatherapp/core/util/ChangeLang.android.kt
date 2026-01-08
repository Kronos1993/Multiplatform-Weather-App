package com.kronos.multiplatform.weatherapp.core.util

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import java.util.Locale

actual class ChangeLang(
    private val context: Context
) : IChangeLang {

    override fun onLangChange(lang: String) {
        if (isCurrentLanguage(lang)) {
            return // No hacer nada si ya está en el idioma deseado
        }

        val locale = when {
            lang == "not-set" || lang.isEmpty() -> getSystemLocale()
            else -> Locale(lang)
        }

        updateAppLocale(locale)
    }

    override fun getSystemLang(): String {
        return  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0].toString().lowercase()
        } else {
            context.resources.configuration.locale.toString().lowercase()
        }
    }

    /**
     * Verifica si el idioma actual es el mismo que se quiere cambiar
     */
    private fun isCurrentLanguage(targetLang: String): Boolean {
        val currentLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            context.resources.configuration.locale
        }

        return when {
            targetLang == "not-set" -> currentLocale == getSystemLocale()
            else -> currentLocale.language == targetLang
        }
    }

    /**
     * Obtiene el locale del sistema
     */
    private fun getSystemLocale(): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Resources.getSystem().configuration.locales[0]
        } else {
            Resources.getSystem().configuration.locale
        }
    }

    /**
     * Actualiza el locale de la aplicación
     */
    private fun updateAppLocale(locale: Locale) {
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
            context.createConfigurationContext(configuration)
        } else {
            configuration.locale = locale
            @Suppress("DEPRECATION")
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }

        // Para Android 10+ también actualizar la configuración del contexto
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            context.resources.configuration.setLocale(locale)
        }
    }

    /**
     * Método adicional para obtener el idioma actual
     */
    fun getCurrentLanguage(): String {
        val currentLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }

        return currentLocale.language
    }
}