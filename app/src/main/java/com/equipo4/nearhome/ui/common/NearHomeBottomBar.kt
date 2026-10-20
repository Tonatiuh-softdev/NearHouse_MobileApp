package com.equipo4.nearhome.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.equipo4.nearhome.R

/** Navegación global: 0 Inicio, 1 Guardados, 2 Notificaciones, 3 Perfil. */
@Composable
fun NearHomeBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val navIcons = listOf(
                    Triple(R.drawable.ic_casa_abajo, R.drawable.ic_casa_relleno_abajo, 1.0f),
                    Triple(R.drawable.ic_guardar_abajo, R.drawable.ic_guardar_relleno_abajo, 1.45f),
                    Triple(R.drawable.ic_campanita_abajo, R.drawable.ic_campanita_relleno_abajo, 1.0f),
                    Triple(R.drawable.ic_user_abajo, R.drawable.ic_user_relleno_abajo, 1.0f)
                )
                navIcons.forEachIndexed { index, (unselected, selected, scaleFactor) ->
                    val isSelected = selectedTab == index
                    IconButton(onClick = { onTabSelected(index) }) {
                        Icon(
                            painter = painterResource(if (isSelected) selected else unselected),
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp).scale(if (isSelected) scaleFactor else 1.0f)
                        )
                    }
                }
            }
        }
    }
}
