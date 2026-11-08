package com.equipo4.nearhome.ui.subscription

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.equipo4.nearhome.ui.theme.NearHouseBgDark
import com.equipo4.nearhome.ui.theme.NearHouseOnSurfaceDark
import com.equipo4.nearhome.ui.theme.NearHouseOnSurfaceVariantDark
import com.equipo4.nearhome.ui.theme.NearHouseOutlineDark
import com.equipo4.nearhome.ui.theme.NearHousePrimaryDark
import com.equipo4.nearhome.ui.theme.NearHouseSurfaceDark

/** Elige el color claro o el oscuro según el tema activo de la app. */
@Composable
private fun themed(light: Color, dark: Color): Color =
    if (MaterialTheme.colorScheme.background.luminance() < 0.5f) dark else light

/**
 * Colores de las pantallas de suscripción y cobro. En modo claro conservan el aspecto de siempre
 * y en modo oscuro usan la paleta oscura de NearHouse.
 */
internal object SubscriptionColors {
    val screenBg: Color
        @Composable get() = themed(Color(0xFFF9FAFB), NearHouseBgDark)
    val surface: Color
        @Composable get() = themed(Color.White, NearHouseSurfaceDark)
    val textPrimary: Color
        @Composable get() = themed(Color(0xFF1F2937), NearHouseOnSurfaceDark)
    val textLabel: Color
        @Composable get() = themed(Color(0xFF374151), Color(0xFFE2E8F0))
    val textBody: Color
        @Composable get() = themed(Color(0xFF4B5563), Color(0xFFCBD5E1))
    val textMuted: Color
        @Composable get() = themed(Color(0xFF6B7280), NearHouseOnSurfaceVariantDark)
    val border: Color
        @Composable get() = themed(Color(0xFFE5E7EB), NearHouseOutlineDark)
    val inputBorder: Color
        @Composable get() = themed(Color(0xFFD1D5DB), Color(0xFF475569))
    val inputBg: Color
        @Composable get() = themed(Color.White, Color(0xFF1E2630))
    val brand: Color
        @Composable get() = themed(Color(0xFF0B2C4D), NearHousePrimaryDark)
    val successBannerBg: Color
        @Composable get() = themed(Color(0xFFDCFCE7), Color(0xFF12301F))
    val successIcon: Color
        @Composable get() = themed(Color(0xFF15803D), Color(0xFF4ADE80))
    val errorText: Color
        @Composable get() = themed(Color(0xFFB91C1C), Color(0xFFFCA5A5))
    val errorStrong: Color
        @Composable get() = themed(Color(0xFFDC2626), Color(0xFFF87171))
}