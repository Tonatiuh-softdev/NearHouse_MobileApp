package com.equipo4.nearhome.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Bloque gris con efecto shimmer, base de todos los skeletons de esta página. */
@Composable
private fun SkeletonBlock(modifier: Modifier = Modifier, shape: Shape = RoundedCornerShape(8.dp)) {
    Box(modifier = modifier.clip(shape).shimmerSkeleton())
}

/**
 * Skeleton de listas (notificaciones, usuarios, anuncios del admin...):
 * avatar circular + dos líneas de texto por fila.
 */
@Composable
fun ListRowsSkeleton(
    modifier: Modifier = Modifier,
    count: Int = 7,
    avatarSize: Dp = 48.dp
) {
    SkeletonContainer(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            repeat(count) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SkeletonBlock(Modifier.size(avatarSize), CircleShape)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SkeletonBlock(Modifier.fillMaxWidth(0.55f).height(14.dp))
                        SkeletonBlock(Modifier.fillMaxWidth(0.9f).height(12.dp))
                    }
                }
            }
        }
    }
}

/** Skeleton de pantallas de detalle (usuario, reporte...): encabezado, título y bloques de texto. */
@Composable
fun DetailBlocksSkeleton(modifier: Modifier = Modifier) {
    SkeletonContainer(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SkeletonBlock(Modifier.size(64.dp), CircleShape)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SkeletonBlock(Modifier.fillMaxWidth(0.6f).height(18.dp))
                    SkeletonBlock(Modifier.fillMaxWidth(0.4f).height(12.dp))
                }
            }
            SkeletonBlock(Modifier.fillMaxWidth().height(110.dp), RoundedCornerShape(14.dp))
            SkeletonBlock(Modifier.fillMaxWidth().height(110.dp), RoundedCornerShape(14.dp))
            SkeletonBlock(Modifier.fillMaxWidth(0.8f).height(14.dp))
            SkeletonBlock(Modifier.fillMaxWidth(0.95f).height(14.dp))
            SkeletonBlock(Modifier.fillMaxWidth(0.6f).height(14.dp))
        }
    }
}

/** Skeleton de las tarjetas de planes de suscripción. */
@Composable
fun PlanCardsSkeleton(modifier: Modifier = Modifier, count: Int = 3) {
    SkeletonContainer(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SkeletonBlock(Modifier.fillMaxWidth(0.7f).height(20.dp))
            repeat(count) {
                SkeletonBlock(Modifier.fillMaxWidth().height(140.dp), RoundedCornerShape(16.dp))
            }
        }
    }
}
