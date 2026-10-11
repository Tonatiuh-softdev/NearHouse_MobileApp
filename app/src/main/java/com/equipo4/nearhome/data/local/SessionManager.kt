package com.equipo4.nearhome.data.local // O ajusta según la carpeta donde lo crees

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("nearhome_session", Context.MODE_PRIVATE)

    fun saveSession(token: String, userId: Int, nombre: String, rol: String) {
        prefs.edit().apply {
            putString("KEY_TOKEN", token)
            putInt("KEY_USER_ID", userId)
            putString("KEY_NOMBRE", nombre)
            putString("KEY_ROL", rol)
            apply()
        }
    }

    fun getToken(): String? = prefs.getString("KEY_TOKEN", null)

    fun isSessionActive(): Boolean = getToken() != null

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}