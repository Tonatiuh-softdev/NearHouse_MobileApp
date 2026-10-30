package com.equipo4.nearhome.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.equipo4.nearhome.R
import com.equipo4.nearhome.domain.model.ListingType
import com.equipo4.nearhome.domain.model.Property
import com.equipo4.nearhome.domain.model.PropertyType
import com.equipo4.nearhome.ui.theme.nearHouseColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PropertyCard(
    property: Property,
    onClick: () -> Unit,
    onToggleSave: () -> Unit = {},
    showSaveButton: Boolean = true,
    badge: (@Composable () -> Unit)? = null
) {
    val totalPages = property.images.ifEmpty { listOf(1) }.size
    val pagerState = rememberPagerState(pageCount = { totalPages })
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            PropertyImageCarousel(
                property = property,
                pagerState = pagerState,
                coroutineScope = coroutineScope,
                totalPages = totalPages,
                onToggleSave = onToggleSave,
                showSaveButton = showSaveButton,
                badge = badge,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            )

            PropertyInfoContent(property = property)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PropertyImageCarousel(
    property: Property,
    pagerState: PagerState,
    coroutineScope: CoroutineScope,
    totalPages: Int,
    onToggleSave: () -> Unit,
    showSaveButton: Boolean,
    badge: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            // La foto puede ser una URL del API (String) o un recurso de los datos de prueba (Int)
            val image = property.images.getOrNull(page)
            if (image != null) {
                AsyncImage(
                    model = image,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (property.type == PropertyType.TERRENO) Color(0xFF53624E)
                        else Color(0xFF7A7265)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.property_image_format, page + 1),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 15.sp
                )
            }
        }

        if (showSaveButton) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { onToggleSave() },
                contentAlignment = Alignment.Center
            ) {
                val bookmarkIcon = if (property.isSaved) {
                    R.drawable.ic_guardar_relleno_abajo
                } else {
                    R.drawable.ic_guardar_abajo
                }
                Icon(
                    painter = painterResource(id = bookmarkIcon),
                    contentDescription = stringResource(R.string.property_save_cd),
                    tint = Color.White,
                    modifier = Modifier
                        .size(18.dp)
                        .scale(if (property.isSaved) 1.45f else 1.0f)
                )
            }
        }

        if (badge != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) { badge() }
        }

        if (pagerState.currentPage > 0) {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp)
                    .size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.common_previous),
                    tint = Color.White
                )
            }
        }

        if (pagerState.currentPage < totalPages - 1) {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp)
                    .size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.common_next),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun PropertyInfoContent(
    property: Property
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
    ) {
        // Fila 1: Precio + Tag RENTA / VENTA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val formattedPrice = formatCurrency(property.price)

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = stringResource(R.string.property_price_format, formattedPrice),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (property.listingType == ListingType.RENTA) {
                    Text(
                        text = stringResource(R.string.property_per_month),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            Surface(
                color = MaterialTheme.nearHouseColors.tagRentaBg,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = property.listingType.name,
                    color = MaterialTheme.nearHouseColors.tagRentaText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Fila 2: Título
        Text(
            text = property.title.uppercase(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Fila 3: PIN DE LOCALIZACIÓN
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_pin_localizacion),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = property.location,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Fila 4: Características dinámicas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (typeText, typeIcon) = when (property.type) {
                PropertyType.DEPARTAMENTO -> stringResource(R.string.property_short_apartment) to R.drawable.ic_departamento
                PropertyType.CASA -> stringResource(R.string.property_short_house) to R.drawable.ic_casa_relleno_abajo
                PropertyType.TERRENO -> stringResource(R.string.property_short_land) to R.drawable.ic_terreno
            }

            FeatureItem(iconRes = typeIcon, text = typeText)

            if (property.type == PropertyType.TERRENO) {
                if (property.frontMeters != null && property.depthMeters != null) {
                    FeatureItem(
                        iconRes = R.drawable.ic_regla_medicion,
                        text = "${property.frontMeters.toInt()}x${property.depthMeters.toInt()} m"
                    )
                } else if (property.areaSqM > 0) {
                    // Los terrenos del API traen los m², no frente y fondo
                    FeatureItem(
                        iconRes = R.drawable.ic_regla_medicion,
                        text = "${property.areaSqM} m²"
                    )
                }
            } else {
                property.bedrooms?.let {
                    FeatureItem(
                        iconRes = R.drawable.ic_habitaciones,
                        text = stringResource(R.string.property_bedrooms_format, it)
                    )
                }
                property.bathrooms?.let {
                    FeatureItem(
                        iconRes = R.drawable.ic_bathrooms,
                        text = stringResource(R.string.property_bathrooms_format, it)
                    )
                }
                property.garages?.let {
                    FeatureItem(
                        iconRes = R.drawable.ic_cocheras,
                        text = stringResource(R.string.property_garages_format, it)
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureItem(
    iconRes: Int,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = text,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    return formatter.format(amount.toLong())
}