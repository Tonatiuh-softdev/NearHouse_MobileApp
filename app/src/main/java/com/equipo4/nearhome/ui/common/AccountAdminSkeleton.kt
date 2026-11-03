package com.equipo4.nearhome.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun AccountAdminSkeleton(
    modifier: Modifier = Modifier
) {
    SkeletonContainer(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ==================== SECCIÓN 1: DATOS DE ACCESO ====================
            // Título de la sección
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .width(140.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerSkeleton()
            )

            // Renglón 1: Email
            AccountOptionRowSkeleton()
            // Renglón 2: Teléfono
            AccountOptionRowSkeleton()
            // Renglón 3: Contraseña
            AccountOptionRowSkeleton()

            Spacer(modifier = Modifier.height(16.dp))

            // ==================== SECCIÓN 2: MEMBRESÍA Y PAGOS ====================
            // Título de la sección
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .width(160.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerSkeleton()
            )

            // Tarjeta de Plan
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .shimmerSkeleton()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ==================== SECCIÓN 3: GESTIÓN DE LA CUENTA ====================
            // Título de la sección
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .width(180.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerSkeleton()
            )

            // Desactivar cuenta
            AccountOptionRowSkeleton(hasCircleIcon = true)
            // Eliminar cuenta
            AccountOptionRowSkeleton(hasCircleIcon = true)
        }
    }
}

@Composable
private fun AccountOptionRowSkeleton(
    hasCircleIcon: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        if (hasCircleIcon) {
            // Icono circular de 40dp
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .shimmerSkeleton()
            )
            Spacer(modifier = Modifier.width(16.dp))
        } else {
            // Icono simple de 24dp
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerSkeleton()
            )
            Spacer(modifier = Modifier.width(16.dp))
        }

        // Título y Subtítulo
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerSkeleton()
            )
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(13.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerSkeleton()
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Flecha derecha
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerSkeleton()
        )
    }
}