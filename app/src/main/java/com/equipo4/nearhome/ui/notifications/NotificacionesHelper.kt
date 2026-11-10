package com.equipo4.nearhome.ui.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.equipo4.nearhome.MainActivity
import com.equipo4.nearhome.util.AppStrings
import android.app.NotificationChannel
import android.content.ContentResolver
import android.media.AudioAttributes
import android.net.Uri
import com.equipo4.nearhome.R
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

    const val CANAL_AVISOS_ID = "nearhouse_avisos"

    // Canales viejos que se borran al iniciar (el sonido de un canal no se puede cambiar una vez creado)
    private const val CANAL_AVISOS_ANTERIOR_ID = "avisos"
    private const val CANAL_FCM_POR_DEFECTO_ID = "fcm_fallback_notification_channel"

    /** Sonido propio de NearHouse para las notificaciones (res/raw/sonido_notificacion.ogg). */
    fun sonidoNotificacionUri(context: Context): Uri =
        Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${R.raw.sonido_notificacion}")

    /** Crea el canal "avisos" (obligatorio en Android 8+). Llámalo en el onCreate de MainActivity. */
    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CANAL_AVISOS_ID, name, importance).apply {
                description = descriptionText
                setSound(
                    sonidoNotificacionUri(context),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.deleteNotificationChannel(CANAL_AVISOS_ANTERIOR_ID)
            notificationManager.deleteNotificationChannel(CANAL_FCM_POR_DEFECTO_ID)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private const val ID_BIENVENIDA_VENDEDOR = 7001

    /** Aviso local: el usuario acaba de volverse vendedor (pago simulado / mes gratis). */
    @SuppressLint("MissingPermission")
    fun mostrarBienvenidaVendedor(context: Context) {
        if (!tienePermiso(context)) return
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            data = Uri.parse("nearhouse://account_admin")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, ID_BIENVENIDA_VENDEDOR, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CANAL_AVISOS_ID)
            .setSmallIcon(R.drawable.ic_notificaciones)
            .setContentTitle(AppStrings.get(R.string.seller_welcome_title))
            .setContentText(AppStrings.get(R.string.seller_welcome_body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(sonidoNotificacionUri(context)) // Android 7 o menor (sin canales)
            .build()
        NotificationManagerCompat.from(context).notify(ID_BIENVENIDA_VENDEDOR, notification)
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