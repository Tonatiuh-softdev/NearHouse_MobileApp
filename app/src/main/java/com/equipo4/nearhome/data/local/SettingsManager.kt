package com.equipo4.nearhome.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Preferencias del dispositivo (idioma, etc.).
 * Va en su propio archivo a propósito: SessionManager.clearSession() y expireSession()
 * borran todo "nearhome_session", y las preferencias no deben perderse al cerrar sesión.
 */
class SettingsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("nearhome_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LANGUAGE = "KEY_LANGUAGE"
        const val DEFAULT_LANGUAGE = "Español"
    }

    fun getLanguage(): String = prefs.getString(KEY_LANGUAGE, null) ?: DEFAULT_LANGUAGE

    fun saveLanguage(language: String) {
        prefs.edit().putString(KEY_LANGUAGE, language).apply()
    }
}