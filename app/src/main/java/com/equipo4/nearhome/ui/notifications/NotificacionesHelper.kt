package com.equipo4.nearhome.ui.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat

object NotificacionesHelper {

    const val CANAL_AVISOS_ID = "avisos"

    /** Crea el canal "avisos" (obligatorio en Android 8+). Llámalo en el onCreate de MainActivity. */
    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Avisos de NearHouse"
            val descriptionText = "Avisos sobre tus publicaciones, reportes y cuenta"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CANAL_AVISOS_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /** Retorna true si ya tenemos permiso para mostrar notificaciones. */
    fun tienePermiso(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // En versiones anteriores ya viene concedido por defecto
        }
    }
}

/**
 * Composable que pide el permiso de notificaciones (Android 13+).
 * Úsalo en la pantalla de Home o después del Login.
 */
@Composable
fun PedirPermisoNotificaciones() {
    val context = androidx.compose.ui.platform.LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // No hacemos nada si lo niega, la pantalla de notificaciones seguirá funcionando internamente.
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!NotificacionesHelper.tienePermiso(context)) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}