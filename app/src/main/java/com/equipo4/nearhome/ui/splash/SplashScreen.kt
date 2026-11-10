package com.equipo4.nearhome.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.ui.common.nearHouseLogoLayers
import com.equipo4.nearhome.util.NearHouseSounds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Splash con la animación original (casa+pin con "pop" y luego el trazo dibujándose de
 * izquierda a derecha), aplicada al logo nuevo. Fondo y logo salen del tema (claro/oscuro).
 * Tiempos tomados de nearhouse_splash.json (60 fps).
 */
@Composable
fun SplashScreen(onFinished: (isLoggedIn: Boolean) -> Unit) {
    val context = LocalContext.current
    val session = remember { SessionManager(context) }
    val (baseLogo, swooshLogo) = nearHouseLogoLayers()

    // Casa + pin
    val baseAlpha = remember { Animatable(0f) }
    val baseScale = remember { Animatable(0.8f) }
    val baseRise = remember { Animatable(1f) }   // 1 = abajo, 0 = en su lugar
    // Trazo
    val swooshReveal = remember { Animatable(0f) } // 0 = oculto, 1 = totalmente dibujado

    LaunchedEffect(Unit) {
        NearHouseSounds.play(context, NearHouseSounds.Sound.CARGA)
        coroutineScope {
            launch { baseAlpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing)) }
            launch {
                baseScale.animateTo(1f, keyframes {
                    durationMillis = 630
                    0.8f at 0
                    1.03f at 400 using FastOutSlowInEasing
                    1f at 630 using FastOutSlowInEasing
                })
            }
            launch { baseRise.animateTo(0f, tween(600, easing = FastOutSlowInEasing)) }
            launch {
                delay(550)
                swooshReveal.animateTo(1f, tween(750, easing = FastOutSlowInEasing))
            }
        }
        val isLoggedIn = withContext(Dispatchers.IO) { session.isLoggedIn() }
        delay(700)
        onFinished(isLoggedIn)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Box(modifier = Modifier.width(220.dp).aspectRatio(768f / 862f)) {
            Image(
                painter = baseLogo,
                contentDescription = "NearHouse Logo",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = baseAlpha.value
                        scaleX = baseScale.value
                        scaleY = baseScale.value
                        translationY = baseRise.value * size.height * 0.047f
                    },
            )
            Image(
                painter = swooshLogo,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    // El trazo se "dibuja" en su lugar, de izquierda a derecha, con borde suave
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                    .drawWithContent {
                        drawContent()
                        val feather = 0.3f
                        val p = swooshReveal.value
                        val a = (p * (1f + feather) - feather).coerceIn(0f, 1f)
                        val b = (p * (1f + feather)).coerceIn(0f, 1f)
                        drawRect(
                            brush = Brush.horizontalGradient(
                                0f to Color.Black,
                                a to Color.Black,
                                b to Color.Transparent,
                                1f to Color.Transparent,
                            ),
                            blendMode = BlendMode.DstIn,
                        )
                    },
            )
        }
    }
}
