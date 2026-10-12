package com.equipo4.nearhome.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("nearhome_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "KEY_IS_LOGGED_IN"
        private const val KEY_TOKEN = "KEY_TOKEN"
        private const val KEY_USER_ID = "KEY_USER_ID"
        private const val KEY_NOMBRE = "KEY_NOMBRE"
        private const val KEY_ROL = "KEY_ROL"
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) || getToken() != null
    }

    fun saveSession(token: String, userId: Int, nombre: String, rol: String) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_TOKEN, token)
            putInt(KEY_USER_ID, userId)
            putString(KEY_NOMBRE, nombre)
            putString(KEY_ROL, rol)
            apply()
        }
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}