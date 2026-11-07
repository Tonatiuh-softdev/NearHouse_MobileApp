package com.equipo4.nearhome.ui.admin.users_crud

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.repository.fotoUsuarioAdminUrl
import com.equipo4.nearhome.domain.model.User
import com.equipo4.nearhome.domain.model.UserAccountRole
import com.equipo4.nearhome.domain.model.UserAccountStatus
import com.equipo4.nearhome.ui.theme.StatusActiveBgDark
import com.equipo4.nearhome.ui.theme.StatusActiveBgLight
import com.equipo4.nearhome.ui.theme.StatusActiveTextDark
import com.equipo4.nearhome.ui.theme.StatusActiveTextLight
import com.equipo4.nearhome.ui.theme.StatusSuspendedBgDark
import com.equipo4.nearhome.ui.theme.StatusSuspendedBgLight
import com.equipo4.nearhome.ui.theme.StatusSuspendedTextDark
import com.equipo4.nearhome.ui.theme.StatusSuspendedTextLight

@Composable
fun UserRoleTag(role: UserAccountRole) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = role.label,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

/** Activo (verde), Suspendido (rojo) y Desactivado (gris). */
@Composable
fun UserStatusBadge(status: UserAccountStatus, showBackground: Boolean = true) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val (bg, texto) = when (status) {
        UserAccountStatus.ACTIVO -> (
                if (isDark) StatusActiveBgDark else StatusActiveBgLight
                ) to (
                if (isDark) StatusActiveTextDark else StatusActiveTextLight
                )
        UserAccountStatus.SUSPENDIDO -> (
                if (isDark) StatusSuspendedBgDark else StatusSuspendedBgLight
                ) to (
                if (isDark) StatusSuspendedTextDark else StatusSuspendedTextLight
                )
        UserAccountStatus.DESACTIVADO ->
            MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    if (showBackground) {
        Surface(color = bg, shape = RoundedCornerShape(10.dp)) {
            Text(
                text = status.label,
                color = texto,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    } else {
        Text(text = status.label, color = texto, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun UserCardItem(
    user: User,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                name = user.name,
                photoUrl = if (user.hasPhoto) fotoUsuarioAdminUrl(user.id) else null,
                size = 50.dp
            )

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = user.email,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserRoleTag(user.role)
                    UserStatusBadge(user.status)
                }
            }

            Spacer(Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = stringResource(R.string.common_view_detail),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}