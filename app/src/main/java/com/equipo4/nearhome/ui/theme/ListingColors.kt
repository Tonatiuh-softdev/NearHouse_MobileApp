package com.equipo4.nearhome.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.equipo4.nearhome.domain.model.ListingStatus

/** Colores de etiquetas y avisos del módulo de anuncios (soporta modo claro y modo oscuro). */
@Immutable
data class TagColors(val container: Color, val content: Color)

enum class NoticeTone { WARNING, SUCCESS, DANGER, INFO, NEUTRAL }

private fun palette(tone: NoticeTone, isDark: Boolean): TagColors = if (isDark) {
    when (tone) {
        NoticeTone.WARNING -> TagColors(Color(0xFF382A10), Color(0xFFFBBF24))
        NoticeTone.SUCCESS -> TagColors(Color(0xFF143821), Color(0xFF4ADE80))
        NoticeTone.DANGER -> TagColors(Color(0xFF3B1818), Color(0xFFF87171))
        NoticeTone.INFO -> TagColors(Color(0xFF122840), Color(0xFF60A5FA))
        NoticeTone.NEUTRAL -> TagColors(Color(0xFF242C35), Color(0xFF94A3B8))
    }
} else {
    when (tone) {
        NoticeTone.WARNING -> TagColors(Color(0xFFFFF1E0), Color(0xFFC2620A))
        NoticeTone.SUCCESS -> TagColors(Color(0xFFE3F5EA), Color(0xFF1F7A45))
        NoticeTone.DANGER -> TagColors(Color(0xFFFDE8E8), Color(0xFFC62828))
        NoticeTone.INFO -> TagColors(Color(0xFFE8F0F8), Color(0xFF34597F))
        NoticeTone.NEUTRAL -> TagColors(Color(0xFFEEF0F3), Color(0xFF566070))
    }
}

@Composable
fun noticeColors(tone: NoticeTone): TagColors {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return palette(tone, isDark)
}

/** Tono de cada estado. null = anuncio eliminado. */
fun ListingStatus?.tone(): NoticeTone = when (this) {
    ListingStatus.PENDIENTE -> NoticeTone.WARNING
    ListingStatus.ACTIVO -> NoticeTone.SUCCESS
    ListingStatus.RECHAZADO -> NoticeTone.DANGER
    ListingStatus.DESACTIVADO -> NoticeTone.NEUTRAL
    ListingStatus.RENTADO_VENDIDO -> NoticeTone.INFO
    null -> NoticeTone.DANGER
}

@Composable
fun listingStatusColors(status: ListingStatus?): TagColors = noticeColors(status.tone())

/** Color de acento (botones principales) del módulo de administración: azul del Figma. */
@Composable
fun adminAccent(): Color = Color(0xFF41688F)

@Composable
fun onAdminAccent(): Color = Color.White
