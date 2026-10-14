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

    // --- Estado de la sesión ---
    fun setLoggedIn(isLoggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) || getToken() != null
    }

    // --- Guardar sesión tras el Login ---
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

    // --- Métodos requeridos por ProfileViewModel ---
    fun getUserName(): String? {
        // Reemplaza "USER_NAME" por la clave con la que guardas el nombre en SharedPreferences
        return prefs.getString("USER_NAME", null)
    }

    fun getUserRole(): String? = prefs.getString(KEY_ROL, null)

    fun saveUser(name: String, role: String) {
        prefs.edit().apply {
            putString(KEY_NOMBRE, name)
            putString(KEY_ROL, role)
            apply()
        }
    }

    // --- Cerrar sesión ---
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}