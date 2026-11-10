package com.equipo4.nearhome.ui.notifications

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.repository.formatearFechaPlan
import com.equipo4.nearhome.domain.model.Notificacion
import com.equipo4.nearhome.domain.model.TipoNotificacion
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val FORMATO_FECHA_NOTIFICACION = DateTimeFormatter.ofPattern("d/M/yy")

/**
 * Cuándo llegó el aviso: "Ahora mismo", "Hace 5 min", "Hace 3 h"
 * o la fecha (D/M/AA) si ya pasó un día.
 */
@Composable
private fun tiempoNotificacion(timestampMs: Long): String {
    val minutos = (System.currentTimeMillis() - timestampMs).coerceAtLeast(0L) / 60_000L
    val horas = minutos / 60L
    return when {
        minutos < 1L -> stringResource(R.string.notif_time_now)
        horas < 1L -> stringResource(R.string.notif_time_minutes, minutos.toInt())
        horas < 24L -> stringResource(R.string.notif_time_hours, horas.toInt())
        else -> Instant.ofEpochMilli(timestampMs)
            .atZone(ZoneId.systemDefault())
            .format(FORMATO_FECHA_NOTIFICACION)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotificationItem(
    notificacion: Notificacion,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val backgroundColor = if (notificacion.leida) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val relativeTime = tiempoNotificacion(notificacion.timestampMs)

    val (titulo, texto) = getNotificationText(notificacion)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Ícono o Imagen a la izquierda (64 dp)
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (!notificacion.imagenUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = notificacion.imagenUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Íconos según tipo si no hay imagen
                val icon = when (notificacion.tipo) {
                    TipoNotificacion.BIENVENIDA -> Icons.Default.Celebration
                    TipoNotificacion.PLAN_CONTRATADO -> Icons.Default.CheckCircle
                    TipoNotificacion.PUBLICACION_APROBADA -> Icons.Default.CheckCircle
                    TipoNotificacion.PUBLICACION_RECHAZADA,
                    TipoNotificacion.PUBLICACION_RETIRADA -> Icons.Default.Error
                    TipoNotificacion.REPORTE_ENVIADO,
                    TipoNotificacion.REPORTE_ATENDIDO,
                    TipoNotificacion.REPORTE_DESCARTADO -> Icons.Default.Report
                    else -> Icons.Default.Home
                }
                val tint = when (notificacion.tipo) {
                    TipoNotificacion.PUBLICACION_APROBADA -> MaterialTheme.colorScheme.primary
                    TipoNotificacion.PUBLICACION_RECHAZADA,
                    TipoNotificacion.PUBLICACION_RETIRADA -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = tint
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Contenido
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = relativeTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = texto,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun getNotificationText(notif: Notificacion): Pair<String, String> {
    return when (notif.tipo) {
        TipoNotificacion.BIENVENIDA -> Pair(
            stringResource(R.string.notif_title_welcome),
            stringResource(R.string.notif_body_welcome, notif.nombreUsuario ?: "")
        )
        TipoNotificacion.PLAN_CONTRATADO -> Pair(
            stringResource(R.string.notif_title_plan),
            stringResource(R.string.notif_body_plan, notif.plan ?: "", formatearFechaPlan(notif.fechaPlanIso))
        )
        TipoNotificacion.PUBLICACION_EN_REVISION -> Pair(
            stringResource(R.string.notif_title_in_review),
            stringResource(R.string.notif_body_in_review, notif.tituloPropiedad ?: "")
        )
        TipoNotificacion.PUBLICACION_APROBADA -> Pair(
            stringResource(R.string.notif_title_approved),
            stringResource(R.string.notif_body_approved, notif.tituloPropiedad ?: "")
        )
        TipoNotificacion.PUBLICACION_RECHAZADA -> Pair(
            stringResource(R.string.notif_title_rejected),
            stringResource(R.string.notif_body_rejected, notif.tituloPropiedad ?: "", notif.motivo ?: "")
        )
        TipoNotificacion.REPORTE_ENVIADO -> Pair(
            stringResource(R.string.notif_title_report_sent),
            stringResource(R.string.notif_body_report_sent, notif.tituloPropiedad ?: "")
        )
        TipoNotificacion.REPORTE_ATENDIDO -> Pair(
            stringResource(R.string.notif_title_report_attended),
            stringResource(R.string.notif_body_report_attended, notif.tituloPropiedad ?: "")
        )
        TipoNotificacion.REPORTE_DESCARTADO -> Pair(
            stringResource(R.string.notif_title_report_discarded),
            stringResource(R.string.notif_body_report_discarded, notif.tituloPropiedad ?: "")
        )
        TipoNotificacion.PUBLICACION_RETIRADA -> Pair(
            stringResource(R.string.notif_title_removed),
            stringResource(R.string.notif_body_removed, notif.tituloPropiedad ?: "", notif.motivo ?: "")
        )
    }
}