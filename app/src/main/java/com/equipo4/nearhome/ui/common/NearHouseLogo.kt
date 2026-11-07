package com.equipo4.nearhome.ui.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.local.SettingsManager

/** true si la app está en modo oscuro (preferencia manual, o el sistema si no hay). */
@Composable
fun isNearHouseDark(): Boolean {
    val manual by SettingsManager.darkModeState.collectAsState()
    return manual ?: isSystemInDarkTheme()
}

/** Logo completo según el modo actual. */
@Composable
fun nearHouseLogoPainter(): Painter =
    painterResource(if (isNearHouseDark()) R.drawable.ic_nearhouse_logo_dark else R.drawable.ic_nearhouse_logo_light)

/** Capas del logo para animarlo: (casa + pin, trazo) según el modo actual. */
@Composable
fun nearHouseLogoLayers(): Pair<Painter, Painter> {
    val dark = isNearHouseDark()
    return Pair(
        painterResource(if (dark) R.drawable.ic_nearhouse_logo_base_dark else R.drawable.ic_nearhouse_logo_base),
        painterResource(if (dark) R.drawable.ic_nearhouse_logo_swoosh_dark else R.drawable.ic_nearhouse_logo_swoosh),
    )
}
