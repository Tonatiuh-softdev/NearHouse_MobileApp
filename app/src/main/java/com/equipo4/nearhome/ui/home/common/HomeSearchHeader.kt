package com.equipo4.nearhome.ui.home.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo4.nearhome.R
import com.equipo4.nearhome.ui.common.nearHouseLogoPainter
import com.equipo4.nearhome.ui.theme.nearHouseColors

/**
 * Encabezado ÚNICO de Home (logo + barra de búsqueda + botón lista/mapa).
 * Lo usan la vista de lista y la de mapa para que no cambie de posición, tamaño ni diseño.
 */
@Composable
fun HomeSearchHeader(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    toggleIcon: Painter,
    toggleContentDescription: String,
    onToggle: () -> Unit,
    onHomeClick: () -> Unit = {}
) {
    var lastToggleTime by remember { mutableStateOf(0L) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Image(
            painter = nearHouseLogoPainter(),
            contentDescription = stringResource(R.string.logo_cd),
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onHomeClick() }
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.nearHouseColors.searchBarBorder,
                    shape = RoundedCornerShape(24.dp)
                )
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.nearHouseColors.searchBarBg)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (searchQuery.isEmpty()) {
                    Text(
                        text = stringResource(R.string.home_search_location),
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                painter = toggleIcon,
                contentDescription = toggleContentDescription,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(18.dp)
                    .clickable {
                        val now = System.currentTimeMillis()
                        if (now - lastToggleTime > 400) {
                            lastToggleTime = now
                            onToggle()
                        }
                    }
            )
        }
    }
}
