package com.equipo4.nearhome.ui.notifications

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.equipo4.nearhome.MainActivity
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.local.SettingsManager
import com.equipo4.nearhome.data.repository.Repositories
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NearHouseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // 1. Extraer datos
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "NearHouse"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: ""
        val tipo = remoteMessage.data["tipo"] ?: ""
        val idPublicacion = remoteMessage.data["idPublicacion"] ?: ""

        // 2. Mostrar notificación
        mostrarNotificacion(title, body, tipo, idPublicacion)
    }

    private fun mostrarNotificacion(title: String, body: String, tipo: String, idPub: String) {
        // 3. DEEP LINKING: Decidir qué pantalla abrir según el tipo
        val rutaDestino = when (tipo) {
            "BIENVENIDA" -> "home"
            "PLAN_CONTRATADO" -> "account_admin"

            "PUBLICACION_EN_REVISION",
            "PUBLICACION_RECHAZADA",
            "PUBLICACION_RETIRADA" -> "my_listings"

            "PUBLICACION_APROBADA",
            "REPORTE_ENVIADO",
            "REPORTE_ATENDIDO",
            "REPORTE_DESCARTADO" ->
                if (idPub.isNotEmpty()) "publication_detail/$idPub" else "home"

            else -> "home"
        }

        // Creamos un Intent hacia MainActivity. Le pasamos la ruta como un dato extra (Uri).
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            // Enviamos la ruta en el "data" del intent
            data = Uri.parse("nearhouse://$rutaDestino")
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(), // ID único para que no se sobreescriban
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, NotificacionesHelper.CANAL_AVISOS_ID)
            .setSmallIcon(R.drawable.ic_notificaciones)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound(NotificacionesHelper.sonidoNotificacionUri(this)) // Android 7 o menor (sin canales)
            .setContentIntent(pendingIntent) // Aquí enlazamos el toque

        if (NotificacionesHelper.tienePermiso(this)) {
            NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Nuevo token generado")

        val scope = CoroutineScope(Dispatchers.IO)
        val session = SessionManager(this)
        if (session.isLoggedIn()) {
            val settings = SettingsManager(this)
            val idioma = if (settings.getLanguage() == SettingsManager.DEFAULT_LANGUAGE) "es" else "en"
            scope.launch {
                Repositories.notificaciones.registrarDispositivo(token, idioma)
            }
        }
    }
}