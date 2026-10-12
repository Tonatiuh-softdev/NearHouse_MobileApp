package com.equipo4.nearhome.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Guarda localmente si el usuario ya inició sesión y revisa la conexión a internet.
 * Es una implementación mínima con SharedPreferences; cuando exista el backend/auth
 * real basta con cambiar [isLoggedIn] para leer el token guardado.
 */
class SessionManager(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("nearhome_session", Context.MODE_PRIVATE)

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGGED_IN, false)

    fun setLoggedIn(value: Boolean) {
        prefs.edit().putBoolean(KEY_LOGGED_IN, value).apply()
    }

    fun hasInternet(): Boolean {
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private companion object {
        const val KEY_LOGGED_IN = "logged_in"
    }
}
