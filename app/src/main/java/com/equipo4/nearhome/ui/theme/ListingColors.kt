package com.equipo4.nearhome.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.equipo4.nearhome.domain.model.ListingStatus

/** Colores de etiquetas y avisos del módulo de anuncios (versión clara, como en el Figma). */
@Immutable
data class TagColors(val container: Color, val content: Color)

enum class NoticeTone { WARNING, SUCCESS, DANGER, INFO, NEUTRAL }

private fun palette(tone: NoticeTone): TagColors = when (tone) {
    NoticeTone.WARNING -> TagColors(Color(0xFFFFF1E0), Color(0xFFC2620A))
    NoticeTone.SUCCESS -> TagColors(Color(0xFFE3F5EA), Color(0xFF1F7A45))
    NoticeTone.DANGER -> TagColors(Color(0xFFFDE8E8), Color(0xFFC62828))
    NoticeTone.INFO -> TagColors(Color(0xFFE8F0F8), Color(0xFF34597F))
    NoticeTone.NEUTRAL -> TagColors(Color(0xFFEEF0F3), Color(0xFF566070))
}

@Composable
fun noticeColors(tone: NoticeTone): TagColors = palette(tone)

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