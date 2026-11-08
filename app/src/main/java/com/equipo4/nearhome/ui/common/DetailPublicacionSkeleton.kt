package com.equipo4.nearhome.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * Esqueleto de carga animado para la pantalla de Detalle de Publicación.
 * Replica la estructura visual exacta de DetailContent.
 */
@Composable
fun DetailPublicacionSkeleton(
    modifier: Modifier = Modifier
) {
    SkeletonContainer(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // 1. Placeholder de Galería/Imagen Principal
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .shimmerSkeleton()
            )

            // 2. Control Segmentado (Pestañas Fotos / Mapa)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 32.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerSkeleton()
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 32.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerSkeleton()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                // 3. Precio y Tipo de propiedad
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .shimmerSkeleton()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerSkeleton()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Dirección / Ubicación
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerSkeleton()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 5. Mini Mapa
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .shimmerSkeleton()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 6. Fila de Especificaciones (m², Recámaras, Baños, Cocheras)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(4) {
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerSkeleton()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 7. Título de la Publicación
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerSkeleton()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 8. Líneas de Descripción
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (index == 2) 0.6f else 1f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerSkeleton()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 9. Botones de Contacto (WhatsApp / Correo)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .shimmerSkeleton()
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .shimmerSkeleton()
                    )
                }
            }
        }
    }
}