package com.equipo4.nearhome.ui.home.detail

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.equipo4.nearhome.R
import com.equipo4.nearhome.domain.model.ListingType
import com.equipo4.nearhome.domain.model.Property
import com.equipo4.nearhome.domain.model.PropertyType
import com.equipo4.nearhome.ui.theme.nearHouseColors
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun PropertyDetailScreen(
    property: Property,
    onBackClick: () -> Unit,
    onToggleSave: () -> Unit,
    onSellerClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onEmailClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.detail_back_cd)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onToggleSave) {
                        Icon(
                            painter = painterResource(
                                if (property.isSaved) R.drawable.ic_guardar_relleno_abajo else R.drawable.ic_guardar_abajo
                            ),
                            contentDescription = stringResource(R.string.detail_save_cd),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            ContactBar(
                canWhatsApp = !property.sellerPhone.isNullOrBlank(),
                canEmail = !property.sellerEmail.isNullOrBlank(),
                onWhatsAppClick = onWhatsAppClick,
                onEmailClick = onEmailClick
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { PhotoGallery(property) }
            item { PriceHeader(property, Modifier.padding(horizontal = 16.dp)) }
            item {
                SellerCard(
                    property = property,
                    onClick = onSellerClick,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            item { LocationSection(property, Modifier.padding(horizontal = 16.dp)) }
            item { FeaturesSection(property, Modifier.padding(horizontal = 16.dp)) }
            item { DescriptionSection(property, Modifier.padding(horizontal = 16.dp)) }
            item { DetailsSection(property, Modifier.padding(horizontal = 16.dp)) }
        }
    }
}

// ---------- Galería ----------

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoGallery(property: Property) {
    val count = property.images.size.coerceAtLeast(1)
    val pagerState = rememberPagerState(pageCount = { count })

    Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            val model = property.images.getOrNull(page)
            if (model != null) {
                AsyncImage(
                    model = model,
                    contentDescription = property.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(placeholderColor(property.type)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(typeIcon(property.type)),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color.Black.copy(alpha = 0.55f)
        ) {
            Text(
                text = "${pagerState.currentPage + 1} / $count",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

// ---------- Precio y título ----------

@Composable
private fun PriceHeader(property: Property, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = typeLabel(property.type),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = stringResource(R.string.property_price_format, formatMoney(property.price)),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (property.listingType == ListingType.RENTA) {
                Text(
                    text = stringResource(R.string.property_per_month),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Spacer(Modifier.weight(1f))
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
        Spacer(Modifier.height(8.dp))
        Text(
            text = property.title.uppercase(),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ---------- Anunciante ----------

@Composable
private fun SellerCard(
    property: Property,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val name = property.sellerName?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.detail_seller_fallback)

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InitialsAvatar(name = name, size = 48.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (property.isOwner) {
                        stringResource(R.string.detail_seller_owner)
                    } else {
                        stringResource(R.string.detail_seller_profile)
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InitialsAvatar(name: String, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials(name),
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.36f).sp
        )
    }
}

// ---------- Ubicación con Google Maps ----------

@Composable
private fun LocationSection(property: Property, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_pin_localizacion),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = property.location,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        val lat = property.latitude
        val lng = property.longitude
        if (lat != null && lng != null) {
            val latLng = LatLng(lat, lng)
            val context = LocalContext.current
            val cameraState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(latLng, 15f)
            }
            val icon = remember(property.type) { pinIcon(context, property.type) }

            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp)),
                cameraPositionState = cameraState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    scrollGesturesEnabled = false,
                    zoomGesturesEnabled = false,
                    rotationGesturesEnabled = false,
                    tiltGesturesEnabled = false,
                    mapToolbarEnabled = false
                )
            ) {
                Marker(state = rememberMarkerState(position = latLng), icon = icon)
            }
        }
    }
}

// ---------- Características ----------

@Composable
private fun FeaturesSection(property: Property, modifier: Modifier = Modifier) {
    val area = property.superficieSqM?.toInt() ?: property.areaSqM

    val features = buildList<Pair<Int, String>> {
        if (area > 0) {
            add(R.drawable.ic_regla_medicion to stringResource(R.string.detail_area_format, area))
        }
        if (property.type == PropertyType.TERRENO) {
            val front = property.frontMeters
            val depth = property.depthMeters
            if (front != null && depth != null) {
                add(
                    R.drawable.ic_regla_medicion to
                            stringResource(R.string.detail_dimensions_format, front.toInt(), depth.toInt())
                )
            }
        } else {
            property.bedrooms?.let {
                add(R.drawable.ic_habitaciones to stringResource(R.string.property_bedrooms_format, it))
            }
            property.bathrooms?.let {
                add(R.drawable.ic_bathrooms to stringResource(R.string.property_bathrooms_format, it))
            }
            property.garages?.let {
                add(R.drawable.ic_cocheras to stringResource(R.string.property_garages_format, it))
            }
        }
    }
    if (features.isEmpty()) return

    Column(modifier = modifier) {
        SectionTitle(stringResource(R.string.detail_section_features))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            features.forEach { (icon, text) -> FeatureChip(icon, text) }
        }
    }
}

@Composable
private fun FeatureChip(iconRes: Int, text: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ---------- Descripción ----------

@Composable
private fun DescriptionSection(property: Property, modifier: Modifier = Modifier) {
    val text = property.description?.takeIf { it.isNotBlank() } ?: return
    var expanded by rememberSaveable { mutableStateOf(false) }
    val isLong = text.length > 180

    Column(modifier = modifier) {
        SectionTitle(stringResource(R.string.detail_section_description))
        Text(
            text = text,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = if (expanded || !isLong) Int.MAX_VALUE else 4,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.animateContentSize()
        )
        if (isLong) {
            Text(
                text = stringResource(
                    if (expanded) R.string.detail_read_less else R.string.detail_read_more
                ),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .clickable { expanded = !expanded }
            )
        }
    }
}

// ---------- Más detalles ----------

@Composable
private fun DetailsSection(property: Property, modifier: Modifier = Modifier) {
    val rows = buildList<Pair<String, String>> {
        when (property.type) {
            PropertyType.DEPARTAMENTO -> {
                property.piso?.let {
                    add(
                        stringResource(R.string.detail_floor) to
                                if (it == 0) stringResource(R.string.detail_ground_floor) else it.toString()
                    )
                }
                add(stringResource(R.string.detail_furnished) to yesNo(property.amueblado))
                add(stringResource(R.string.detail_elevator) to yesNo(property.elevador))
            }
            PropertyType.CASA -> {
                property.niveles?.let {
                    add(stringResource(R.string.detail_levels) to it.toString())
                }
                add(stringResource(R.string.detail_garden_patio) to yesNo(property.jardinPatio))
            }
            PropertyType.TERRENO -> {
                property.usoSuelo?.let { uso ->
                    add(stringResource(R.string.detail_land_use) to uso)
                }
            }
        }
        if (property.listingType == ListingType.RENTA && property.type != PropertyType.TERRENO) {
            add(
                stringResource(R.string.detail_pets) to
                        stringResource(
                            if (property.mascotasPermitidas) R.string.detail_pets_allowed
                            else R.string.detail_pets_not_allowed
                        )
            )
        }
        property.rules?.takeIf { it.isNotBlank() }?.let {
            add(stringResource(R.string.detail_rules) to it)
        }
    }
    if (rows.isEmpty()) return

    Column(modifier = modifier) {
        SectionTitle(stringResource(R.string.detail_section_details))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column {
                rows.forEachIndexed { index, (label, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = value,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.End,
                            modifier = Modifier.padding(start = 16.dp).weight(1f, fill = false)
                        )
                    }
                    if (index < rows.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

// ---------- Barra de contacto ----------

@Composable
private fun ContactBar(
    canWhatsApp: Boolean,
    canEmail: Boolean,
    onWhatsAppClick: () -> Unit,
    onEmailClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onWhatsAppClick,
                enabled = canWhatsApp,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF25D366),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.detail_whatsapp), fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onEmailClick,
                enabled = canEmail,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.detail_email), fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ---------- Helpers ----------

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun typeLabel(type: PropertyType): String = stringResource(
    when (type) {
        PropertyType.CASA -> R.string.detail_type_house
        PropertyType.DEPARTAMENTO -> R.string.detail_type_apartment
        PropertyType.TERRENO -> R.string.detail_type_land
    }
)

private fun typeIcon(type: PropertyType) = when (type) {
    PropertyType.CASA -> R.drawable.ic_casa_relleno_abajo
    PropertyType.DEPARTAMENTO -> R.drawable.ic_departamento
    PropertyType.TERRENO -> R.drawable.ic_terreno
}

private fun placeholderColor(type: PropertyType) =
    if (type == PropertyType.TERRENO) Color(0xFF53624E) else Color(0xFF7A7265)

@Composable
private fun yesNo(value: Boolean): String =
    stringResource(if (value) R.string.detail_yes else R.string.detail_no)

private fun initials(name: String): String =
    name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }

private fun formatMoney(amount: Double): String =
    NumberFormat.getNumberInstance(Locale.US).format(amount.toLong())

private fun pinIcon(context: Context, type: PropertyType): BitmapDescriptor {
    val res = when (type) {
        PropertyType.CASA -> R.drawable.ic_pin_casa
        PropertyType.TERRENO -> R.drawable.ic_pin_terreno
        PropertyType.DEPARTAMENTO -> R.drawable.ic_pin_departamento
    }
    val density = context.resources.displayMetrics.density
    val width = (28 * density).roundToInt()
    val height = (39 * density).roundToInt()
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    ContextCompat.getDrawable(context, res)?.apply {
        setBounds(0, 0, width, height)
        draw(Canvas(bitmap))
    }
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}