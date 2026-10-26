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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Immutable
data class NearHouseCustomColors(
    val tagCasa: Color,
    val tagDepartamento: Color,
    val tagTerreno: Color,
    val tagRentaBg: Color,
    val tagRentaText: Color,
    val tagVentaBg: Color,
    val tagVentaText: Color,
    val tagTuPublicacionBg: Color,
    val tagTuPublicacionText: Color,
    val whatsappBtnBg: Color,
    val whatsappBtnText: Color,
    val correoBtnBg: Color,
    val correoBtnText: Color,
    val searchBarBg: Color,
    val filterChipBg: Color,
    val filterChipSelectedBg: Color,
    val cardBorder: Color
)

val LightNearHouseCustomColors = NearHouseCustomColors(
    tagCasa = Color(0xFFE30B0C),
    tagDepartamento = Color(0xFF3136BD),
    tagTerreno = Color(0xFF94A945),
    tagRentaBg = Color(0xFFEBF5FF),
    tagRentaText = Color(0xFF1E88E5),
    tagVentaBg = Color(0xFFEBF5FF),
    tagVentaText = Color(0xFF1E88E5),
    tagTuPublicacionBg = Color(0xFF007AFF),
    tagTuPublicacionText = Color.White,
    whatsappBtnBg = Color(0xFF25D366),
    whatsappBtnText = Color.White,
    correoBtnBg = Color(0xFF2B5B84),
    correoBtnText = Color.White,
    searchBarBg = Color(0xFFF3F4F6),
    filterChipBg = Color.White,
    filterChipSelectedBg = Color(0xFFE0F2FE),
    cardBorder = Color(0xFFE5E7EB)
)

val DarkNearHouseCustomColors = NearHouseCustomColors(
    tagCasa = Color(0xFFFF5252),
    tagDepartamento = Color(0xFF536DFE),
    tagTerreno = Color(0xFF8BC34A),
    tagRentaBg = Color(0xFF132F4C),
    tagRentaText = Color(0xFF64B5F6),
    tagVentaBg = Color(0xFF132F4C),
    tagVentaText = Color(0xFF64B5F6),
    tagTuPublicacionBg = Color(0xFF1976D2),
    tagTuPublicacionText = Color.White,
    whatsappBtnBg = Color(0xFF25D366),
    whatsappBtnText = Color.White,
    correoBtnBg = Color(0xFF16385C),
    correoBtnText = Color.White,
    searchBarBg = Color(0xFF1E2630),
    filterChipBg = Color(0xFF1E2630),
    filterChipSelectedBg = Color(0xFF1E3A5F),
    cardBorder = Color(0xFF2A3441)
)

val LocalNearHouseCustomColors = staticCompositionLocalOf {
    LightNearHouseCustomColors
}

val MaterialTheme.nearHouseColors: NearHouseCustomColors
    @Composable
    @ReadOnlyComposable
    get() = LocalNearHouseCustomColors.current

private val DarkColorScheme = darkColorScheme(
    primary = NearHousePrimaryDark,
    onPrimary = NearHouseOnPrimaryDark,
    primaryContainer = Color(0xFF1E3A5F),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFF9CA3AF),
    secondaryContainer = Color(0xFF1E2630),
    onSecondaryContainer = Color(0xFFE2E8F0),
    tertiary = Pink80,
    background = NearHouseBgDark,
    onBackground = NearHouseOnSurfaceDark,
    surface = NearHouseSurfaceDark,
    onSurface = NearHouseOnSurfaceDark,
    surfaceVariant = Color(0xFF1E2630),
    onSurfaceVariant = NearHouseOnSurfaceVariantDark,
    outline = NearHouseOutlineDark,
    outlineVariant = Color(0xFF334155),
    error = Color(0xFFEF5350)
)

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
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Apagado siempre según requerimiento
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

    val customColors = if (darkTheme) DarkNearHouseCustomColors else LightNearHouseCustomColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                // Íconos claros de la barra de estado en modo oscuro, oscuros en modo claro
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalNearHouseCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
