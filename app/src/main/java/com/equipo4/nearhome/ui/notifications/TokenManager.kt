package com.equipo4.nearhome.ui.notifications

import android.content.Context
import android.util.Log
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.local.SettingsManager
import com.equipo4.nearhome.data.repository.Repositories
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object TokenManager {

    /**
     * Obtiene el token FCM actual y lo manda al backend.
     * Llama a esto al: iniciar sesión, abrir la app, cambiar idioma, etc.
     */
    fun registrarTokenActual(context: Context, scope: CoroutineScope) {
        val session = SessionManager(context)
        if (!session.isLoggedIn()) return // Solo si hay sesión

        scope.launch(Dispatchers.IO) {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                val settings = SettingsManager(context)

                // Determinar idioma (es o en)
                val idioma = if (settings.getLanguage() == SettingsManager.DEFAULT_LANGUAGE) "es" else "en"

                Repositories.notificaciones.registrarDispositivo(token, idioma)
                    .onSuccess { Log.d("FCM", "Token registrado con éxito en el backend") }
                    .onFailure { Log.e("FCM", "Error al registrar token: ${it.message}") }

            } catch (e: Exception) {
                Log.e("FCM", "Error obteniendo token de Firebase: ${e.message}")
            }
        }
    }

    /**
     * Elimina el token del backend cuando el usuario cierra sesión.
     */
    fun borrarTokenActual(context: Context, scope: CoroutineScope, onFinished: () -> Unit) {
        scope.launch(Dispatchers.IO) {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                Repositories.notificaciones.bajaDispositivo(token)
            } catch (e: Exception) {
                Log.e("FCM", "Error al borrar token: ${e.message}")
            } finally {
                // Pase lo que pase, cerramos la sesión al final.
                // onFinished navega (NavController), así que debe correr en el hilo principal.
                withContext(NonCancellable + Dispatchers.Main) { onFinished() }
            }
        }
    }
}