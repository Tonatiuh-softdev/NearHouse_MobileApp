package com.equipo4.nearhome.ui.common

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription

/**
 * Modifier personalizado para aplicar un efecto de carga tipo Skeleton (Shimmer)
 * sin librerías externas.
 */
fun Modifier.shimmerSkeleton(
    showSkeleton: Boolean = true
): Modifier = composed {
    if (!showSkeleton) return@composed this

    val baseColor = MaterialTheme.colorScheme.surfaceVariant
    val highlightColor = baseColor.copy(alpha = 0.4f)

    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            baseColor,
            highlightColor,
            baseColor
        ),
        start = Offset(x = translateAnim - 300f, y = translateAnim - 300f),
        end = Offset(x = translateAnim, y = translateAnim)
    )

    this.background(brush = brush)
}

/**
 * Contenedor para envolver pantallas o secciones en estado esqueleto.
 * Permite que TalkBack / lector de pantalla emita un único anuncio ("Cargando").
 */
@Composable
fun SkeletonContainer(
    modifier: Modifier = Modifier,
    contentDescriptionText: String = "Cargando",
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.clearAndSetSemantics {
            contentDescription = contentDescriptionText
        }
    ) {
        content()
    }
}