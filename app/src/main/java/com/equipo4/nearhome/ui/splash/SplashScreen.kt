package com.equipo4.nearhome.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.local.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Pantalla de carga: reproduce la animación Lottie (~2.4 s) mientras se hacen las
 * validaciones iniciales (sesión guardada y conexión a internet). Navega cuando
 * terminan AMBAS cosas, es decir, lo que tarde más.
 *
 * @param onFinished true si hay sesión iniciada (ir a Home), false si no (ir a Login).
 */
@Composable
fun SplashScreen(onFinished: (isLoggedIn: Boolean) -> Unit) {
    val context = LocalContext.current
    val session = remember { SessionManager(context) }

    val compositionResult = rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.nearhouse_splash)
    )
    val progress by animateLottieCompositionAsState(
        composition = compositionResult.value,
        iterations = 1
    )

    LaunchedEffect(Unit) {
        // 1) Validaciones iniciales en segundo plano
        val (isLoggedIn, hasInternet) = withContext(Dispatchers.IO) {
            session.isLoggedIn() to session.hasInternet()
        }
        // hasInternet queda disponible para mostrar un aviso más adelante;
        // por ahora no bloquea el arranque (la app puede abrir sin conexión).
        @Suppress("UNUSED_VARIABLE") val unused = hasInternet

        // 2) Esperar a que termine la animación (o a que falle la carga del JSON)
        snapshotFlow { progress >= 1f || compositionResult.isFailure }.first { it }


        onFinished(isLoggedIn)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = compositionResult.value,
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}
