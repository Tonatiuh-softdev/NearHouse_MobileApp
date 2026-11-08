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
fun MyListingsHeaderSkeleton(
    modifier: Modifier = Modifier
) {
    SkeletonContainer(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Barra superior vacía para alineación
            Spacer(Modifier.height(48.dp))

            // Avatar de 90dp
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .shimmerSkeleton()
            )

            Spacer(Modifier.height(12.dp))

            // Nombre
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerSkeleton()
            )

            Spacer(Modifier.height(8.dp))

            // Correo
            Box(
                modifier = Modifier
                    .width(190.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerSkeleton()
            )

            Spacer(Modifier.height(6.dp))

            // Teléfono
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerSkeleton()
            )

            Spacer(Modifier.height(16.dp))

            // Botón Crear Anuncio
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shimmerSkeleton()
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}