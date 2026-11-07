package com.equipo4.nearhome.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.ui.common.nearHouseLogoPainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Pantalla de carga con animación de entrada única usando el logo adaptable según el tema.
 *
 * @param onFinished true si hay sesión iniciada (ir a Home), false si no (ir a Login).
 */
@Composable
fun SplashScreen(onFinished: (isLoggedIn: Boolean) -> Unit) {
    val context = LocalContext.current
    val session = remember { SessionManager(context) }

    // Animaciones de entrada para el logo único
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.65f) }
    val translateY = remember { Animatable(30f) }

    LaunchedEffect(Unit) {
        // Ejecutar las animaciones de aparición del logo único
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            )
        }
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            )
        }
        launch {
            translateY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            )
        }

        val isLoggedIn = withContext(Dispatchers.IO) {
            session.isLoggedIn()
        }

        delay(1800) // Duración de la animación en pantalla
        onFinished(isLoggedIn)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = nearHouseLogoPainter(),
            contentDescription = "NearHouse Logo",
            modifier = Modifier
                .size(240.dp)
                .graphicsLayer {
                    this.alpha = alpha.value
                    this.scaleX = scale.value
                    this.scaleY = scale.value
                    this.translationY = translateY.value
                },
        )
    }
}
