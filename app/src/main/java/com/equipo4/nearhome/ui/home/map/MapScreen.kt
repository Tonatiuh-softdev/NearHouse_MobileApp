package com.equipo4.nearhome.ui.home.map

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import kotlin.math.roundToInt
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo4.nearhome.R
import com.equipo4.nearhome.domain.model.ListingType
import com.equipo4.nearhome.domain.model.Property
import com.equipo4.nearhome.domain.model.PropertyType
import com.equipo4.nearhome.ui.common.NearHomeBottomBar
import com.equipo4.nearhome.ui.home.filter.PropertyFilterManager
import com.equipo4.nearhome.ui.theme.nearHouseColors
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.luminance
import com.google.maps.android.compose.ComposeMapColorScheme

private const val PIN_WIDTH_DP = 28
private const val PIN_HEIGHT_DP = 39

object PinColors {
    val Todo = Color(0xFF808080)
    val Casa = Color(0xFFE30B0C)
    val Terreno = Color(0xFF94A945)
    val Departamento = Color(0xFF3136BD)

    fun of(type: PropertyType) = when (type) {
        PropertyType.CASA -> Casa
        PropertyType.TERRENO -> Terreno
        PropertyType.DEPARTAMENTO -> Departamento
    }
}

private fun pinRes(type: PropertyType) = when (type) {
    PropertyType.CASA -> R.drawable.ic_pin_casa
    PropertyType.TERRENO -> R.drawable.ic_pin_terreno
    PropertyType.DEPARTAMENTO -> R.drawable.ic_pin_departamento
}

@Composable
private fun typeLabel(type: PropertyType) = when (type) {
    PropertyType.CASA -> stringResource(R.string.map_type_houses)
    PropertyType.TERRENO -> stringResource(R.string.map_type_lands)
    PropertyType.DEPARTAMENTO -> stringResource(R.string.map_type_apartments)
}

private fun scaledPinBitmap(
    context: Context,
    @DrawableRes resId: Int,
    widthDp: Int,
    heightDp: Int
): Bitmap {
    val density = context.resources.displayMetrics.density
    val widthPx = (widthDp * density).roundToInt()
    val heightPx = (heightDp * density).roundToInt()
    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    ContextCompat.getDrawable(context, resId)?.let { drawable ->
        val canvas = android.graphics.Canvas(bitmap)
        drawable.setBounds(0, 0, widthPx, heightPx)
        drawable.draw(canvas)
    }
    return bitmap
}

private data class PropertyClusterItem(
    val property: Property,
    override val position: LatLng
) : ClusterItem {
    override val title: String? = property.title
    override val snippet: String? = null
    override val zIndex: Float? = 0f
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel(),
    onToggleToList: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    onPropertyClick: (String) -> Unit = {},
    onBottomTabSelected: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val visibleProperties = uiState.visibleProperties

    Scaffold(
        bottomBar = { NearHomeBottomBar(selectedTab = 0, onTabSelected = onBottomTabSelected) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            MapHeader(
                searchQuery = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                onToggleToList = onToggleToList
            )
            FilterRow(
                uiState = uiState,
                onTypeToggled = viewModel::onTypeToggled,
                onFilterClick = onFilterClick
            )
            Spacer(Modifier.height(6.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                GoogleMapView(
                    properties = visibleProperties,
                    onMarkerClick = viewModel::onMarkerSelected
                )

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
                }

                uiState.errorMessage?.let { message ->
                    Surface(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 6.dp
                    ) {
                        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(message, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = viewModel::loadProperties,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) { Text(stringResource(R.string.map_retry)) }
                        }
                    }
                }
            }
        }
    }

    uiState.selectedProperty?.let { property ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = viewModel::onSheetDismissed,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            PropertyPreviewSheet(
                property = property,
                onClose = viewModel::onSheetDismissed,
                onToggleSave = { viewModel.toggleSaveProperty(property.id) },
                onClick = {
                    viewModel.onSheetDismissed()
                    onPropertyClick(property.id)
                }
            )
        }
    }
}

@Composable
private fun GoogleMapView(
    properties: List<Property>,
    onMarkerClick: (String) -> Unit
) {
    val context = LocalContext.current
    val manzanillo = LatLng(19.08, -104.32)
    val scope = rememberCoroutineScope()

    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(manzanillo, 13f)
    }

    val pinBitmaps = remember {
        PropertyType.entries.associateWith { type ->
            scaledPinBitmap(context, pinRes(type), PIN_WIDTH_DP, PIN_HEIGHT_DP).asImageBitmap()
        }
    }

    val clusterItems = remember(properties) {
        properties.mapNotNull { property ->
            val lat = property.latitude
            val lng = property.longitude
            if (lat != null && lng != null) PropertyClusterItem(property, LatLng(lat, lng)) else null
        }
    }

    val mapProperties = remember(isDarkTheme) {
        if (isDarkTheme) {
            val styleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark)
            MapProperties(mapStyleOptions = styleOptions)
        } else {
            MapProperties()
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        mapColorScheme = if (isDarkTheme) ComposeMapColorScheme.DARK else ComposeMapColorScheme.LIGHT
    ) {
        Clustering(
            items = clusterItems,
            onClusterClick = { cluster ->
                scope.launch {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(
                            cluster.position,
                            cameraPositionState.position.zoom + 2f
                        )
                    )
                }
                true
            },
            onClusterItemClick = { item ->
                onMarkerClick(item.property.id)
                true
            },
            clusterContent = { cluster ->
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    border = BorderStroke(2.dp, Color.White),
                    shadowElevation = 4.dp
                ) {
                    Box(modifier = Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = cluster.size.toString(),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            clusterItemContent = { item ->
                Image(
                    bitmap = pinBitmaps.getValue(item.property.type),
                    contentDescription = item.property.title,
                    modifier = Modifier.size(PIN_WIDTH_DP.dp, PIN_HEIGHT_DP.dp)
                )
            },
            onClusterManager = { manager ->
                (manager.renderer as? DefaultClusterRenderer<*>)?.minClusterSize = 2
            }
        )
    }
}

@Composable
private fun MapHeader(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onToggleToList: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_nearhouse_logo),
            contentDescription = stringResource(R.string.logo_cd),
            modifier = Modifier.size(40.dp).clip(CircleShape)
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .clip(RoundedCornerShape(23.dp))
                .background(MaterialTheme.nearHouseColors.searchBarBg)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (searchQuery.isEmpty()) {
                    Text(stringResource(R.string.map_search_location), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.FormatListBulleted,
                contentDescription = stringResource(R.string.map_view_list),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp).noRippleClickable { onToggleToList() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterRow(
    uiState: MapUiState,
    onTypeToggled: (PropertyType?) -> Unit,
    onFilterClick: () -> Unit
) {
    val filterState by PropertyFilterManager.filterState.collectAsState()
    val activeCount = filterState.getSelectedCount()
    val hasFilters = activeCount > 0

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val typeLabelText = when (uiState.selectedTypes.size) {
            0 -> stringResource(R.string.map_type_todo)
            1 -> typeLabel(uiState.selectedTypes.first())
            else -> stringResource(R.string.map_type_types, uiState.selectedTypes.size)
        }
        TypeFilterChip(
            label = typeLabelText,
            selectedTypes = uiState.selectedTypes,
            onTypeToggled = onTypeToggled
        )

        FilterChip(
            selected = hasFilters,
            onClick = onFilterClick,
            label = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_filtrar),
                        contentDescription = stringResource(R.string.home_filter_btn),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = stringResource(R.string.home_filter_btn), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    if (hasFilters) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = activeCount.toString(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            },
            shape = RoundedCornerShape(8.dp),
            colors = FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.nearHouseColors.filterChipBg,
                selectedContainerColor = MaterialTheme.nearHouseColors.filterChipSelectedBg,
                labelColor = MaterialTheme.colorScheme.onSurface,
                selectedLabelColor = MaterialTheme.colorScheme.primary
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = hasFilters,
                borderColor = MaterialTheme.colorScheme.outline,
                selectedBorderColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun TypeFilterChip(
    label: String,
    selectedTypes: Set<PropertyType>,
    onTypeToggled: (PropertyType?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var chipHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Box(modifier = Modifier.onSizeChanged { chipHeight = with(density) { it.height.toDp() } }) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.width(120.dp),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.nearHouseColors.filterChipBg,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(0.dp, -chipHeight),
            shape = RoundedCornerShape(12.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.width(210.dp)
        ) {
            TypeCheckRow(
                label = stringResource(R.string.map_type_todo),
                color = PinColors.Todo,
                checked = selectedTypes.isEmpty(),
                trailing = {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.common_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp).noRippleClickable { expanded = false }
                    )
                }
            ) { onTypeToggled(null) }

            listOf(PropertyType.CASA, PropertyType.TERRENO, PropertyType.DEPARTAMENTO).forEach { type ->
                TypeCheckRow(
                    label = typeLabel(type),
                    color = PinColors.of(type),
                    checked = type in selectedTypes
                ) { onTypeToggled(type) }
            }
        }
    }
}

@Composable
private fun TypeCheckRow(
    label: String,
    color: Color,
    checked: Boolean,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .noRippleClickable(onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (checked) color else MaterialTheme.colorScheme.surface)
                .border(2.dp, color, RoundedCornerShape(6.dp))
        )
        Spacer(Modifier.width(14.dp))
        Text(label, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        trailing?.invoke()
    }
}

@Composable
private fun PropertyPreviewSheet(
    property: Property,
    onClose: () -> Unit,
    onToggleSave: () -> Unit,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .noRippleClickable { onClick() }
            .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
            .navigationBarsPadding()
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            Text(
                stringResource(R.string.map_preview_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Center)
            )
            Icon(
                Icons.Default.Close,
                contentDescription = stringResource(R.string.common_close),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterEnd).size(24.dp).noRippleClickable { onClose() }
            )
        }

        val totalPages = property.images.ifEmpty { listOf(1) }.size
        val pagerState = rememberPagerState(pageCount = { totalPages })
        Box(modifier = Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(12.dp))) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                val imageRes = property.images.getOrNull(page)
                if (imageRes != null) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else Box(
                    modifier = Modifier.fillMaxSize().background(
                        if (property.type == PropertyType.TERRENO) Color(0xFF53624E) else Color(0xFF7A7265)
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(
                                when (property.type) {
                                    PropertyType.CASA -> R.drawable.ic_casa_relleno_abajo
                                    PropertyType.DEPARTAMENTO -> R.drawable.ic_departamento
                                    PropertyType.TERRENO -> R.drawable.ic_terreno
                                }
                            ),
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(stringResource(R.string.property_image_format, page + 1), color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart).padding(12.dp)
                    .clip(RoundedCornerShape(8.dp)).background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("${pagerState.currentPage + 1}/$totalPages", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd).padding(12.dp).size(36.dp)
                    .clip(CircleShape).background(Color.Black.copy(alpha = 0.4f))
                    .noRippleClickable { onToggleSave() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(if (property.isSaved) R.drawable.ic_guardar_relleno_abajo else R.drawable.ic_guardar_abajo),
                    contentDescription = stringResource(R.string.property_save_cd),
                    tint = Color.White,
                    modifier = Modifier.size(18.dp).scale(if (property.isSaved) 1.45f else 1.0f)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    stringResource(R.string.property_price_format, formatCurrency(property.price)),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (property.listingType == ListingType.RENTA) {
                    Text(
                        stringResource(R.string.property_per_month),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
            Surface(color = MaterialTheme.nearHouseColors.tagRentaBg, shape = RoundedCornerShape(6.dp)) {
                Text(
                    property.listingType.name,
                    color = MaterialTheme.nearHouseColors.tagRentaText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            property.title.uppercase(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(R.drawable.ic_pin_localizacion), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(property.location, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            val (typeText, typeIcon) = when (property.type) {
                PropertyType.DEPARTAMENTO -> stringResource(R.string.property_short_apartment) to R.drawable.ic_departamento
                PropertyType.CASA -> stringResource(R.string.property_short_house) to R.drawable.ic_casa_relleno_abajo
                PropertyType.TERRENO -> stringResource(R.string.property_short_land) to R.drawable.ic_terreno
            }
            FeatureItem(typeIcon, typeText)
            if (property.type == PropertyType.TERRENO) {
                if (property.frontMeters != null && property.depthMeters != null) {
                    FeatureItem(R.drawable.ic_regla_medicion, "${property.frontMeters.toInt()}x${property.depthMeters.toInt()} m")
                }
            } else {
                property.bedrooms?.let { FeatureItem(R.drawable.ic_habitaciones, stringResource(R.string.property_bedrooms_format, it)) }
                property.bathrooms?.let { FeatureItem(R.drawable.ic_bathrooms, stringResource(R.string.property_bathrooms_format, it)) }
                property.garages?.let { FeatureItem(R.drawable.ic_cocheras, stringResource(R.string.property_garages_format, it)) }
            }
        }
    }
}

@Composable
private fun FeatureItem(iconRes: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(painterResource(iconRes), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
        Text(text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatCurrency(amount: Double): String =
    NumberFormat.getNumberInstance(Locale.US).format(amount.toLong())

@Composable
private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier =
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
