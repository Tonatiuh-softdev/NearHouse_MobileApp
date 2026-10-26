package com.equipo4.nearhome.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Preferencias del dispositivo (idioma, modo oscuro, etc.).
 * Va en su propio archivo a propósito: SessionManager.clearSession() y expireSession()
 * borran todo "nearhome_session", y las preferencias no deben perderse al cerrar sesión.
 */
class SettingsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("nearhome_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LANGUAGE = "KEY_LANGUAGE"
        private const val KEY_DARK_MODE = "KEY_DARK_MODE"
        const val DEFAULT_LANGUAGE = "Español"

        // Emisor global reactivo para que el tema de la app cambie al instante
        private val _darkModeState = MutableStateFlow<Boolean?>(null)
        val darkModeState: StateFlow<Boolean?> = _darkModeState.asStateFlow()
    }

    init {
        _darkModeState.value = getDarkMode()
    }

    fun getLanguage(): String = prefs.getString(KEY_LANGUAGE, null) ?: DEFAULT_LANGUAGE

    fun saveLanguage(language: String) {
        prefs.edit().putString(KEY_LANGUAGE, language).apply()
    }

    /**
     * Retorna null si el usuario no ha elegido preferencia (sigue al celular).
     * Retorna true si eligió "Activado" y false si eligió "Desactivado".
     */
    fun getDarkMode(): Boolean? {
        return if (prefs.contains(KEY_DARK_MODE)) {
            prefs.getBoolean(KEY_DARK_MODE, false)
        } else {
            null
        }
    }

    fun saveDarkMode(enabled: Boolean?) {
        if (enabled == null) {
            prefs.edit().remove(KEY_DARK_MODE).apply()
        } else {
            prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        }
        _darkModeState.value = enabled
    }
}
