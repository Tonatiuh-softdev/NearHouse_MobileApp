package com.equipo4.nearhome.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.equipo4.nearhome.R
import androidx.compose.material3.Text

/** Navegación global: 0 Inicio, 1 Guardados, 2 Notificaciones, 3 Perfil. */
@Composable
fun NearHomeBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    // Nuevo parámetro opcional para el contador de notificaciones
    unreadCount: Int = 0
) {
    var lastClickTime by remember { mutableStateOf(0L) }

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
                    IconButton(
                        onClick = {
                            val now = System.currentTimeMillis()
                            // Si es otra pestaña, cambia inmediatamente. Si es la misma, aplica debounce.
                            if (index != selectedTab || now - lastClickTime > 400) {
                                lastClickTime = now
                                onTabSelected(index)
                            }
                        }
                    ) {
                        // Si es la pestaña de notificaciones (índice 2), mostramos el Badge
                        if (index == 2 && unreadCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge {
                                        Text(text = if (unreadCount > 9) "9+" else unreadCount.toString())
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(if (isSelected) selected else unselected),
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp).scale(if (isSelected) scaleFactor else 1.0f)
                                )
                            }
                        } else {
                            // Ícono normal para el resto de pestañas
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
}