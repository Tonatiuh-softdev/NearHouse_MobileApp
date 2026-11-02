package com.equipo4.nearhome.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * Replica exactamente las dimensiones y estructura de la PropertyCard real.
 */
@Composable
fun PropertyCardSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Placeholder de la Imagen Principal (Foto)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .shimmerSkeleton()
            )

            // Contenido textual inferior
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Fila superior: Precio y Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .shimmerSkeleton()
                    )
                    Box(
                        modifier = Modifier
                            .width(70.dp)
                            .height(22.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .shimmerSkeleton()
                    )
                }

                // Título de la propiedad
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerSkeleton()
                )

                // Ubicación / Colonia
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerSkeleton()
                )

                // Fila de características (Recámaras, Baños, m²)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .width(50.dp)
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerSkeleton()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Lista de 3 a 4 tarjetas esqueleto para la carga inicial.
 */
@Composable
fun PropertyListSkeleton(
    count: Int = 3,
    modifier: Modifier = Modifier
) {
    SkeletonContainer(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(count) {
                PropertyCardSkeleton()
            }
        }
    }
}