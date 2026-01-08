package com.kronos.multiplatform.weatherapp.core.util

import co.touchlab.kermit.Logger
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.currentLocale
import platform.Foundation.languageCode
import platform.Foundation.localeIdentifier

actual class ChangeLang : IChangeLang {

    override fun onLangChange(lang: String) {
        if (isCurrentLanguage(lang)) {
            return // No hacer nada si ya está en el idioma deseado
        }

        val localeCode = when {
            lang == "not-set" || lang.isEmpty() -> getSystemLang()
            else -> lang
        }

        updateAppLocale(localeCode)
    }

    override fun getSystemLang(): String {
        val currentLocale = NSLocale.currentLocale
        val languageCode = currentLocale.languageCode ?: "en"
        return languageCode.lowercase()
    }

    /**
     * Verifica si el idioma actual es el mismo que se quiere cambiar
     */
    private fun isCurrentLanguage(targetLang: String): Boolean {
        val currentLang = getCurrentAppLanguage()

        return when {
            targetLang == "not-set" -> currentLang == getSystemLang()
            else -> currentLang == targetLang
        }
    }

    /**
     * Obtiene el idioma actual de la aplicación
     */
    private fun getCurrentAppLanguage(): String {
        val defaults = NSUserDefaults.standardUserDefaults
        val preferredLanguages = defaults.stringArrayForKey("AppleLanguages") as? List<String>

        return if (preferredLanguages?.isNotEmpty() == true) {
            // Tomar el primer idioma preferido y extraer el código (ej: "es-ES" -> "es")
            preferredLanguages.first().split("-").first().lowercase()
        } else {
            getSystemLang()
        }
    }

    /**
     * Actualiza el locale de la aplicación
     */
    private fun updateAppLocale(localeCode: String) {
        val defaults = NSUserDefaults.standardUserDefaults

        // Crear el array de idiomas preferidos
        val preferredLanguages = listOf(localeCode)

        // Guardar en UserDefaults
        defaults.setObject(preferredLanguages, "AppleLanguages")
        defaults.synchronize()

        // También actualizar el locale por defecto si es necesario
        updateDefaultLocale(localeCode)

        Logger.i("Language changed to: $localeCode")
    }

    /**
     * Actualiza el locale por defecto del sistema
     */
    private fun updateDefaultLocale(localeCode: String) {
        // En iOS, el locale por defecto se maneja automáticamente
        // pero podemos forzar algunos cambios si es necesario
        // Nota: Cambiar el locale a nivel de app en iOS es más limitado que en Android
    }

    /**
     * Método adicional para obtener el idioma actual de la app
     */
    fun getCurrentLanguage(): String {
        return getCurrentAppLanguage()
    }

    /**
     * Método para obtener el locale completo (idioma-región)
     */
    fun getCurrentLocale(): String {
        val defaults = NSUserDefaults.standardUserDefaults
        val preferredLanguages = defaults.stringArrayForKey("AppleLanguages") as? List<String>

        return preferredLanguages?.firstOrNull() ?: getSystemLocale()
    }

    /**
     * Obtiene el locale del sistema completo
     */
    private fun getSystemLocale(): String {
        val currentLocale = NSLocale.currentLocale
        return currentLocale.localeIdentifier ?: "en-US"
    }
}