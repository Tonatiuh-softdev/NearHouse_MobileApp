package com.equipo4.nearhome.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("nearhome_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "KEY_IS_LOGGED_IN"
        private const val KEY_TOKEN = "KEY_TOKEN"
        private const val KEY_USER_ID = "KEY_USER_ID"
        private const val KEY_NOMBRE = "KEY_NOMBRE"
        private const val KEY_ROL = "KEY_ROL"
        private const val KEY_CORREO = "KEY_CORREO"
        private const val KEY_TELEFONO = "KEY_TELEFONO"
        private const val KEY_SESSION_EXPIRED = "KEY_SESSION_EXPIRED"
    }

    // --- Estado de la sesión ---
    fun setLoggedIn(isLoggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) || getToken() != null
    }

    // --- Guardar sesión tras el Login ---
    // correo y telefono son opcionales para no romper a quien ya llama a saveSession
    fun saveSession(
        token: String,
        userId: Int,
        nombre: String,
        rol: String,
        correo: String? = null,
        telefono: String? = null
    ) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_TOKEN, token)
            putInt(KEY_USER_ID, userId)
            putString(KEY_NOMBRE, nombre)
            putString(KEY_ROL, rol)
            if (correo != null) putString(KEY_CORREO, correo)
            if (telefono != null) putString(KEY_TELEFONO, telefono)
            apply()
        }
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    // --- Métodos requeridos por ProfileViewModel ---
    fun getUserName(): String? = prefs.getString(KEY_NOMBRE, null)

    fun getUserRole(): String? = prefs.getString(KEY_ROL, null)

    fun saveUser(name: String, role: String) {
        prefs.edit().apply {
            putString(KEY_NOMBRE, name)
            putString(KEY_ROL, role)
            apply()
        }
    }

    // --- Contacto del usuario (WhatsApp y correo) ---
    fun getUserEmail(): String? = prefs.getString(KEY_CORREO, null)

    fun getUserPhone(): String? = prefs.getString(KEY_TELEFONO, null)

    fun saveContact(correo: String?, telefono: String?) {
        prefs.edit().apply {
            if (!correo.isNullOrBlank()) putString(KEY_CORREO, correo)
            if (!telefono.isNullOrBlank()) putString(KEY_TELEFONO, telefono)
            apply()
        }
    }

    // --- Sesión expirada (401) ---
    /** Borra la sesión y deja la marca para que el login avise una sola vez. */
    fun expireSession() {
        prefs.edit().clear().putBoolean(KEY_SESSION_EXPIRED, true).apply()
    }

    /** Devuelve true una sola vez y limpia la marca. */
    fun consumeSessionExpired(): Boolean {
        val expired = prefs.getBoolean(KEY_SESSION_EXPIRED, false)
        if (expired) prefs.edit().remove(KEY_SESSION_EXPIRED).apply()
        return expired
    }

    // --- Cerrar sesión ---
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}