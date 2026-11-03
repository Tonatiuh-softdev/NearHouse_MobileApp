package com.equipo4.nearhome.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun ProfileHeaderSkeleton(
    modifier: Modifier = Modifier
) {
    SkeletonContainer(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto de perfil (Círculo de 60dp)
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .shimmerSkeleton()
            )
            Spacer(Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Nombre de usuario (Línea de 140dp x 18dp)
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerSkeleton()
                )
                // Tag de Rol (Caja de 80dp x 20dp con esquinas redondeadas)
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .shimmerSkeleton()
                )
            }
        }
    }
}