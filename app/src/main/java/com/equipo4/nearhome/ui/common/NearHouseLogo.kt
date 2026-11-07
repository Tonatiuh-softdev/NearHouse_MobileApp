package com.equipo4.nearhome.ui.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.local.SettingsManager

/** Logo según el MISMO modo que usa NearHouseApp: preferencia manual, o el sistema si no hay. */
@Composable
fun nearHouseLogoPainter(): Painter {
    val manual by SettingsManager.darkModeState.collectAsState()
    val dark = manual ?: isSystemInDarkTheme()
    return painterResource(if (dark) R.drawable.ic_nearhouse_logo_dark else R.drawable.ic_nearhouse_logo)
}
