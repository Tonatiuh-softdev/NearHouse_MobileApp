package com.equipo4.nearhome.ui.home.map

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo4.nearhome.R
import com.equipo4.nearhome.domain.model.ListingType
import com.equipo4.nearhome.domain.model.Property
import com.equipo4.nearhome.domain.model.PropertyType
import com.equipo4.nearhome.ui.common.NearHomeBottomBar
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

private val NavyColor = Color(0xFF0B2C4D)
private val LightGrayBg = Color(0xFFF3F4F6)
private val BorderColor = Color(0xFFE5E7EB)
private val BadgeBlueBg = Color(0xFFEBF5FF)
private val BadgeBlueText = Color(0xFF1E88E5)
private val TextDark = Color(0xFF1F2937)
private val TextGray = Color(0xFF6B7280)

/** Tamaño de los pines en el mapa (dp). Si se ven grandes o chicos, se ajusta aquí. */
private const val PIN_WIDTH_DP = 36
private const val PIN_HEIGHT_DP = 50

/**
 * Fuente única de color por tipo de propiedad. Los indicadores del filtro usan estos colores,
 * que fueron muestreados del cuerpo de cada pin (ic_pin_*.png) para que coincidan con el mapa.
 */
object PinColors {
    val Todo = Color(0xFF000000)
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

private fun typeLabel(type: PropertyType) = when (type) {
    PropertyType.CASA -> "Casas"
    PropertyType.TERRENO -> "Terrenos"
    PropertyType.DEPARTAMENTO -> "Departamentos"
}

/**
 * Convierte un drawable (PNG o vector) en un ícono de marcador con el tamaño indicado en dp.
 * Debe llamarse cuando el mapa ya está listo (por ejemplo, dentro del contenido de GoogleMap).
 */
private fun scaledPinIcon(
    context: Context,
    @DrawableRes resId: Int,
    widthDp: Int,
    heightDp: Int
): BitmapDescriptor {
    val density = context.resources.displayMetrics.density
    val widthPx = (widthDp * density).roundToInt()
    val heightPx = (heightDp * density).roundToInt()
    val drawable = ContextCompat.getDrawable(context, resId)
        ?: return BitmapDescriptorFactory.defaultMarker()
    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    drawable.setBounds(0, 0, widthPx, heightPx)
    drawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
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
        containerColor = Color.White
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            MapHeader(
                searchQuery = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                onToggleToList = onToggleToList
            )
            FilterRow(
                uiState = uiState,
                onFilterClick = onFilterClick,
                onTypeToggled = viewModel::onTypeToggled,
                onSortSelected = viewModel::onSortSelected
            )
            Spacer(Modifier.height(6.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                GoogleMapView(
                    properties = visibleProperties,
                    onMarkerClick = viewModel::onMarkerSelected
                )

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = NavyColor) }
                }

                uiState.errorMessage?.let { message ->
                    Surface(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 6.dp
                    ) {
                        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(message, color = TextDark, fontSize = 14.sp, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = viewModel::loadProperties,
                                colors = ButtonDefaults.buttonColors(containerColor = NavyColor)
                            ) { Text("Reintentar") }
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
            containerColor = Color.White
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

/**
 * Mapa real de Google Maps, centrado en Manzanillo. Un marcador por propiedad visible,
 * con el pin de su tipo. El clic se consume (devuelve true) para que Google no muestre
 * su globo de información por defecto.
 */
@Composable
private fun GoogleMapView(
    properties: List<Property>,
    onMarkerClick: (String) -> Unit
) {
    val context = LocalContext.current
    val manzanillo = LatLng(19.08, -104.32)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(manzanillo, 13f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        // Un ícono por tipo, creado una sola vez (ya con el mapa listo).
        val pinIcons = remember {
            PropertyType.entries.associateWith { type ->
                scaledPinIcon(context, pinRes(type), PIN_WIDTH_DP, PIN_HEIGHT_DP)
            }
        }

        properties.forEach { property ->
            val latitude = property.latitude
            val longitude = property.longitude

            if (latitude != null && longitude != null) {
                key(property.id) {
                    Marker(
                        state = rememberUpdatedMarkerState(position = LatLng(latitude, longitude)),
                        title = property.title,
                        icon = pinIcons[property.type],
                        onClick = {
                            onMarkerClick(property.id)
                            true // consume el evento: sin globo de información por defecto
                        }
                    )
                }
            }
        }
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
            contentDescription = "Logo NearHome",
            modifier = Modifier.size(40.dp).clip(CircleShape)
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .clip(RoundedCornerShape(23.dp))
                .background(LightGrayBg)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (searchQuery.isEmpty()) {
                    Text("Busca por ubicación", fontSize = 13.sp, color = Color.Gray, maxLines = 1)
                }
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.width(8.dp))
            // En Mapa el ícono muestra "lista" para regresar a la vista de Lista.
            Icon(
                imageVector = Icons.Default.FormatListBulleted,
                contentDescription = "Ver lista",
                tint = Color.Black,
                modifier = Modifier.size(20.dp).noRippleClickable { onToggleToList() }
            )
        }
    }
}

@Composable
private fun FilterRow(
    uiState: MapUiState,
    onFilterClick: () -> Unit,
    onTypeToggled: (PropertyType?) -> Unit,
    onSortSelected: (SortOption) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Filtrar: no despliega nada, redirige a su propia pantalla (pendiente de conectar en NearHouseApp)
        OutlinedButton(
            onClick = onFilterClick,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, BorderColor),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(painterResource(R.drawable.ic_filtrar), contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text("Filtrar", fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }

        // Ordenar
        DropdownChip(label = "Ordenar", iconRes = R.drawable.ic_ordenar) { dismiss ->
            SortOption.entries.filter { it != SortOption.NONE }.forEach { option ->
            }
        }

        // Tipo de propiedad: "Todo" por defecto, multi-selección con indicador de color
        val typeLabelText = when (uiState.selectedTypes.size) {
            0 -> "Todo"
            1 -> typeLabel(uiState.selectedTypes.first())
            else -> "${uiState.selectedTypes.size} tipos"
        }
        TypeFilterChip(
            label = typeLabelText,
            selectedTypes = uiState.selectedTypes,
            onTypeToggled = onTypeToggled
        )
    }
}

@Composable
private fun DropdownChip(
    label: String,
    iconRes: Int?,
    menuContent: @Composable ColumnScope.(dismiss: () -> Unit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, BorderColor),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            if (iconRes != null) {
                Icon(painterResource(iconRes), contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = Color.White
        ) { menuContent { expanded = false } }
    }
}

/**
 * Chip "Todo" con panel como el mockup: el panel se abre sobre el chip, la primera fila es "Todo"
 * con chevron, y cada opción lleva un checkbox cuadrado con borde del color del tipo
 * (relleno cuando está seleccionado).
 */
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
        // Ancho FIJO: si el chip cambiara de ancho con la etiqueta ("Todo", "Casas", "2 tipos"...),
        // el panel se reposicionaría cada vez que se cambia el filtro.
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.width(120.dp),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, BorderColor),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
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
            containerColor = Color.White,
            border = BorderStroke(1.dp, BorderColor),
            modifier = Modifier.width(210.dp)
        ) {
            // Fila "Todo" (con chevron, como el chip abierto del mockup)
            TypeCheckRow(
                label = "Todo",
                color = PinColors.Todo,
                checked = selectedTypes.isEmpty(),
                trailing = {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Cerrar",
                        tint = Color(0xFF9CA3AF),
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
                .background(if (checked) color else Color.White)
                .border(2.dp, color, RoundedCornerShape(6.dp))
        )
        Spacer(Modifier.width(14.dp))
        Text(label, fontSize = 16.sp, color = TextDark, modifier = Modifier.weight(1f))
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
                "Detalle de la propiedad",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.align(Alignment.Center)
            )
            Icon(
                Icons.Default.Close,
                contentDescription = "Cerrar",
                tint = TextDark,
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
                        Text("Imagen ${page + 1}", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
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
                    contentDescription = "Guardar propiedad",
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
                Text("MX \$${formatCurrency(property.price)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
                if (property.listingType == ListingType.RENTA) {
                    Text(" / mes", fontSize = 14.sp, color = TextGray, modifier = Modifier.padding(bottom = 2.dp))
                }
            }
            Surface(color = BadgeBlueBg, shape = RoundedCornerShape(6.dp)) {
                Text(
                    property.listingType.name,
                    color = BadgeBlueText, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(property.title.uppercase(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(R.drawable.ic_pin_localizacion), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(property.location, fontSize = 12.sp, color = TextGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            val (typeText, typeIcon) = when (property.type) {
                PropertyType.DEPARTAMENTO -> "Dept." to R.drawable.ic_departamento
                PropertyType.CASA -> "Casa" to R.drawable.ic_casa_relleno_abajo
                PropertyType.TERRENO -> "Terreno" to R.drawable.ic_terreno
            }
            FeatureItem(typeIcon, typeText)
            if (property.type == PropertyType.TERRENO) {
                if (property.frontMeters != null && property.depthMeters != null) {
                    FeatureItem(R.drawable.ic_regla_medicion, "${property.frontMeters.toInt()}x${property.depthMeters.toInt()} m")
                }
            } else {
                property.bedrooms?.let { FeatureItem(R.drawable.ic_habitaciones, "$it recámaras") }
                property.bathrooms?.let { FeatureItem(R.drawable.ic_bathrooms, "$it baños") }
                property.garages?.let { FeatureItem(R.drawable.ic_cocheras, "$it cocheras") }
            }
        }
    }
}

@Composable
private fun FeatureItem(iconRes: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(painterResource(iconRes), contentDescription = null, tint = TextGray, modifier = Modifier.size(15.dp))
        Text(text, fontSize = 12.sp, color = TextGray)
    }
}

private fun formatCurrency(amount: Double): String =
    NumberFormat.getNumberInstance(Locale.US).format(amount.toLong())

/** clickable sin ripple: evita el rectángulo gris oscuro al presionar pines, el sheet, etc. */
@Composable
private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier =
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )