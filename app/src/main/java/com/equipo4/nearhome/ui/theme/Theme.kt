package com.equipo4.nearhome.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

// Colores del Figma (azul, fondo gris claro, tarjetas blancas).
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF41688F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3ECF6),
    onPrimaryContainer = Color(0xFF1F3F61),
    secondary = Color(0xFF566070),
    secondaryContainer = Color(0xFFE3ECF6),
    onSecondaryContainer = Color(0xFF1F3F61),
    tertiary = Pink40,
    background = Color(0xFFF4F6F9),
    onBackground = Color(0xFF1B2430),
    surface = Color.White,
    onSurface = Color(0xFF1B2430),
    surfaceVariant = Color(0xFFE9EDF2),
    onSurfaceVariant = Color(0xFF677282),
    outline = Color(0xFFB8C0CC),
    outlineVariant = Color(0xFFE1E5EB),
    error = Color(0xFFC62828)
)

@Composable
fun NearHomeTheme(
    // la app se ve clara.
    darkTheme: Boolean = false,
    // Apagado: con true los colores salen del fondo de pantalla del celular.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}