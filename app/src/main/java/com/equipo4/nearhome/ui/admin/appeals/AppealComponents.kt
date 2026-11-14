package com.equipo4.nearhome.ui.admin.appeals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo4.nearhome.R
import com.equipo4.nearhome.domain.model.Appeal
import com.equipo4.nearhome.domain.model.AppealStatus
import com.equipo4.nearhome.domain.model.AppealType

/** Etiqueta del tipo, con los mismos colores que la de los reportes (anuncio naranja, cuenta azul). */
@Composable
fun AppealTypeTag(type: AppealType) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val (bg, textColor, icon) = when (type) {
        AppealType.PUBLICACION -> if (isDark) {
            Triple(Color(0xFF382314), Color(0xFFFB923C), Icons.Outlined.Home)
        } else {
            Triple(Color(0xFFFFF7ED), Color(0xFFC2410C), Icons.Outlined.Home)
        }
        AppealType.SUSPENSION -> if (isDark) {
            Triple(Color(0xFF122840), Color(0xFF60A5FA), Icons.Outlined.Block)
        } else {
            Triple(Color(0xFFEFF6FF), Color(0xFF1D4ED8), Icons.Outlined.Block)
        }
    }

    Surface(color = bg, shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = textColor, modifier = Modifier.size(14.dp))
            Text(text = type.label, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

/** Resultado de una apelación resuelta: aceptada (verde, como "Atendido") o rechazada (gris). */
@Composable
fun AppealStatusBadge(status: AppealStatus) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val aceptada = status == AppealStatus.ACEPTADA
    val bg = when {
        aceptada && isDark -> Color(0xFF143821)
        aceptada -> Color(0xFFDCFCE7)
        isDark -> Color(0xFF242C35)
        else -> Color(0xFFF1F5F9)
    }
    val textColor = when {
        aceptada && isDark -> Color(0xFF4ADE80)
        aceptada -> Color(0xFF15803D)
        isDark -> Color(0xFF94A3B8)
        else -> Color(0xFF475569)
    }

    Surface(color = bg, shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (aceptada) Icons.Default.CheckCircle else Icons.Outlined.RemoveCircleOutline,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = stringResource(if (aceptada) R.string.appeal_status_accepted else R.string.appeal_status_rejected),
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/** Renglón de la lista de apelaciones. Mismo diseño que el de los reportes. */
@Composable
fun AppealCardItem(
    appeal: Appeal,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppealTypeTag(appeal.type)

                if (appeal.status == AppealStatus.PENDIENTE) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = stringResource(R.string.common_view_detail),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    AppealStatusBadge(appeal.status)
                }
            }

            Spacer(Modifier.height(10.dp))

            // Qué se apela: el anuncio retirado o la suspensión de la cuenta
            Text(
                text = appeal.publicationTitle ?: stringResource(R.string.appeal_decision_account),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = appeal.appellantName,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = appeal.date,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoCamera,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = pluralStringResource(R.plurals.evidences_count, appeal.evidenceCount, appeal.evidenceCount),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
