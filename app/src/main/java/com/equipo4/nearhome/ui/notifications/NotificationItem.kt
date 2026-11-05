package com.equipo4.nearhome.ui.notifications

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.equipo4.nearhome.R
import com.equipo4.nearhome.domain.model.Notificacion
import com.equipo4.nearhome.domain.model.TipoNotificacion

@Composable
fun NotificationItem(
    notificacion: Notificacion,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    val backgroundColor = if (notificacion.leida) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val relativeTime = DateUtils.getRelativeTimeSpanString(
        notificacion.timestampMs,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE
    ).toString()

    val (titulo, texto) = getNotificationText(notificacion)
    val readStatusText = if (!notificacion.leida) "No leída" else ""
    val accessibilityDescription = "$titulo. $texto. $relativeTime. $readStatusText"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(16.dp)
            .clearAndSetSemantics {
                contentDescription = accessibilityDescription
            },
        verticalAlignment = Alignment.Top
    ) {
        // Imagen o Ícono a la izquierda (64 dp)
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            when (notificacion.tipo) {
                TipoNotificacion.PAGO -> {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                TipoNotificacion.BIENVENIDA -> {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                else -> {
                    if (!notificacion.imagenUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = notificacion.imagenUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Contenido a la derecha
        Column(
            modifier = Modifier.weight(1f)
        ) {
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
        TipoNotificacion.EN_REVISION -> Pair(
            stringResource(R.string.notif_title_in_review),
            stringResource(R.string.notif_body_in_review, notif.tituloPropiedad ?: "")
        )
        TipoNotificacion.APROBADA -> Pair(
            stringResource(R.string.notif_title_approved),
            stringResource(R.string.notif_body_approved, notif.tituloPropiedad ?: "")
        )
        TipoNotificacion.RECHAZADA -> Pair(
            stringResource(R.string.notif_title_rejected),
            stringResource(R.string.notif_body_rejected, notif.tituloPropiedad ?: "", notif.motivoRechazo ?: "")
        )
        TipoNotificacion.PAGO -> Pair(
            stringResource(R.string.notif_title_payment),
            stringResource(R.string.notif_body_payment, notif.nombreUsuario ?: "", notif.nombrePlan ?: "")
        )
        TipoNotificacion.BIENVENIDA -> Pair(
            stringResource(R.string.notif_title_welcome),
            stringResource(R.string.notif_body_welcome, notif.nombreUsuario ?: "")
        )
        TipoNotificacion.NOVEDADES_BUSQUEDA -> Pair(
            stringResource(R.string.notif_title_search_updates),
            stringResource(R.string.notif_body_search_updates)
        )
        TipoNotificacion.ENCONTRAMOS_ALGO -> Pair(
            stringResource(R.string.notif_title_found_for_you),
            stringResource(R.string.notif_body_found_for_you, notif.criterioBusqueda ?: "")
        )
        TipoNotificacion.RECIEN_PUBLICADAS -> Pair(
            stringResource(R.string.notif_title_recently_published),
            stringResource(R.string.notif_body_recently_published)
        )
    }
}