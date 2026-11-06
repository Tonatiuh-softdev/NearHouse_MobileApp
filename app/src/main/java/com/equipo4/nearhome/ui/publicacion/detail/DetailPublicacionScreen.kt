package com.equipo4.nearhome.ui.publicacion.detail

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo4.nearhome.R
import com.equipo4.nearhome.domain.model.PropertyType
import com.equipo4.nearhome.ui.common.DetailPublicacionSkeleton
import com.equipo4.nearhome.ui.common.FakeMapPreview
import com.equipo4.nearhome.ui.theme.nearHouseColors
import com.equipo4.nearhome.util.AppStrings
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlin.math.roundToInt
import androidx.compose.foundation.background
import com.google.android.gms.maps.MapsInitializer
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailPublicacionScreen(
    publicacionId: String,
    onBackClick: () -> Unit,
    onReportClick: () -> Unit,
    onEditClick: () -> Unit,
    onSellerClick: (Int) -> Unit,
    viewModel: DetailPublicacionViewModel = viewModel()
) {
    LaunchedEffect(publicacionId) {
        viewModel.cargarPublicacion(publicacionId)
    }

    val uiState by viewModel.uiState.collectAsState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var isNavigatingBack by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (!isNavigatingBack) {
                                isNavigatingBack = true
                                onBackClick()
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showBottomSheet = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.detail_options_menu_cd),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = uiState) {
                is DetailPublicacionUiState.Loading -> DetailPublicacionSkeleton()

                is DetailPublicacionUiState.Error -> Text(
                    text = state.mensaje,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )

                is DetailPublicacionUiState.Success -> {
                    val pub = state.publicacion

                    DetailContent(
                        pub = pub,
                        onReportClick = onReportClick,
                        onSellerClick = {
                            pub.sellerId?.let { onSellerClick(it) }
                        }
                    )

                    if (showBottomSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { showBottomSheet = false },
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            HerramientasBottomSheet(
                                esPropietario = pub.esPropietario,
                                estaGuardado = pub.estaGuardado,
                                onGuardar = {
                                    viewModel.toggleGuardar()
                                    showBottomSheet = false
                                },
                                onEditar = {
                                    showBottomSheet = false
                                    onEditClick()
                                },
                                onCompartir = { showBottomSheet = false },
                                onReportar = {
                                    showBottomSheet = false
                                    onReportClick()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DetailContent(
    pub: PublicacionDetail,
    onReportClick: () -> Unit,
    onSellerClick: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var isExpandedDescription by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp)
    ) {
        // --- GALERÍA ---
        PhotoGallery(
            images = pub.imagenes,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            if (pub.esPropietario) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(MaterialTheme.nearHouseColors.tagTuPublicacionBg, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.detail_your_listing),
                        color = MaterialTheme.nearHouseColors.tagTuPublicacionText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // --- CONTROL SEGMENTADO ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            TabIcon(
                icon = Icons.Outlined.PhotoCamera,
                description = stringResource(R.string.detail_photos_cd),
                selected = selectedTab == 0,
                modifier = Modifier.weight(1f).clickable { selectedTab = 0 }
            )
            TabIcon(
                icon = Icons.Outlined.LocationOn,
                description = stringResource(R.string.search_filters_location),
                selected = selectedTab == 1,
                modifier = Modifier.weight(1f).clickable { selectedTab = 1 }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- PRECIO Y TIPO ---
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            if (pub.esPropietario) {
                TipoPropiedadSection(pub.tipoPropiedad)
                Spacer(modifier = Modifier.height(4.dp))
                PrecioSection(pub.precio, pub.esRenta)
            } else {
                PrecioSection(pub.precio, pub.esRenta)
                Spacer(modifier = Modifier.height(4.dp))
                TipoPropiedadSection(pub.tipoPropiedad)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- DIRECCIÓN ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = pub.direccion,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- TARJETA DEL ANUNCIANTE ---
            SellerCard(pub = pub, onClick = onSellerClick)

            Spacer(modifier = Modifier.height(16.dp))

            // --- MAPA (real si hay coordenadas, si no el de prueba) ---
            val lat = pub.latitud
            val lng = pub.longitud
            if (lat != null && lng != null) {
                LocationMap(
                    lat = lat,
                    lng = lng,
                    type = pub.tipo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                FakeMapPreview(
                    propertyType = pub.tipo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- ESPECIFICACIONES ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (pub.metrosCuadrados > 0) {
                    FeatureItem(iconRes = R.drawable.ic_regla_medicion, label = "${pub.metrosCuadrados} m²")
                }
                if (pub.recamaras > 0) {
                    FeatureItem(iconRes = R.drawable.ic_habitaciones, label = stringResource(R.string.property_bedrooms_format, pub.recamaras))
                }
                if (pub.banos > 0) {
                    FeatureItem(iconRes = R.drawable.ic_bathrooms, label = stringResource(R.string.property_bathrooms_format, pub.banos))
                }
                if (pub.cocheras > 0) {
                    FeatureItem(iconRes = R.drawable.ic_cocheras, label = stringResource(R.string.property_garages_format, pub.cocheras))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- TÍTULO Y DESCRIPCIÓN ---
            Text(
                text = pub.titulo.uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = pub.descripcion,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = if (isExpandedDescription || pub.esPropietario) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis
            )

            if (!pub.esPropietario) {
                Text(
                    text = stringResource(
                        if (isExpandedDescription) R.string.detail_read_less else R.string.detail_read_more
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { isExpandedDescription = !isExpandedDescription }
                        .padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- MÁS DETALLES ---
            Text(
                text = stringResource(R.string.detail_more_details),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    pub.caracteristicasColumna1.forEach { item ->
                        Text(text = item, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    pub.caracteristicasColumna2.forEach { item ->
                        Text(text = item, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- REPORTE (solo comprador) ---
            if (!pub.esPropietario) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.detail_report_question),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.detail_report_here),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onReportClick() }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // --- CONTACTO ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse(
                                "https://api.whatsapp.com/send?phone=${pub.telefonoVendedor}" +
                                        "&text=${Uri.encode(AppStrings.get(R.string.detail_whatsapp_message))}"
                            )
                        }
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.nearHouseColors.whatsappBtnBg,
                        contentColor = MaterialTheme.nearHouseColors.whatsappBtnText
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.nearHouseColors.whatsappBtnText)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("WhatsApp", color = MaterialTheme.nearHouseColors.whatsappBtnText, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:${pub.emailVendedor}")
                            putExtra(Intent.EXTRA_SUBJECT, AppStrings.get(R.string.detail_email_subject, pub.titulo))
                        }
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.nearHouseColors.correoBtnBg,
                        contentColor = MaterialTheme.nearHouseColors.correoBtnText
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.nearHouseColors.correoBtnText)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.detail_email), color = MaterialTheme.nearHouseColors.correoBtnText, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ---------- Tarjeta del anunciante ----------

@Composable
private fun SellerCard(pub: PublicacionDetail, onClick: () -> Unit) {
    val name = pub.sellerName.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.detail_seller_fallback)

    Surface(
        onClick = onClick,
        enabled = pub.sellerId != null,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InitialsAvatar(name = name, size = 44.dp)
            Spacer(modifier = Modifier.width(12.dp))
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
                    text = stringResource(
                        if (pub.esPropietario) R.string.detail_seller_owner else R.string.detail_seller_profile
                    ),
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

// ---------- Mapa real ----------

@Composable
private fun LocationMap(lat: Double, lng: Double, type: PropertyType, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val latLng = LatLng(lat, lng)
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(latLng, 15f)
    }

    // Se inicializa el SDK de Maps antes de crear cualquier BitmapDescriptor
    var mapsReady by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        MapsInitializer.initialize(context.applicationContext, MapsInitializer.Renderer.LATEST) { _ ->
            mapsReady = true
        }
    }

    if (!mapsReady) {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
        return
    }

    val icon = remember(type, mapsReady) { pinIcon(context, type) }

    GoogleMap(
        modifier = modifier,
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

// ---------- Piezas reutilizadas ----------

@Composable
private fun TabIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (selected) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
private fun PrecioSection(precio: Double, esRenta: Boolean) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = if (esRenta) {
                stringResource(R.string.detail_rent_price_format, String.format("%,.0f", precio))
            } else {
                stringResource(R.string.property_price_format, String.format("%,.0f", precio))
            },
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (esRenta) {
            Text(
                text = stringResource(R.string.detail_per_month),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}

@Composable
private fun TipoPropiedadSection(tipo: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = R.drawable.ic_departamento),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = tipo, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun FeatureItem(iconRes: Int, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun HerramientasBottomSheet(
    esPropietario: Boolean,
    estaGuardado: Boolean,
    onGuardar: () -> Unit,
    onEditar: () -> Unit,
    onCompartir: () -> Unit,
    onReportar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.detail_tools),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (esPropietario) {
            SheetRow(Icons.Outlined.Edit, stringResource(R.string.create_listing_edit), onEditar)
            SheetRow(Icons.Outlined.Share, stringResource(R.string.detail_share), onCompartir)
        } else {
            SheetRow(
                icon = if (estaGuardado) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                text = stringResource(if (estaGuardado) R.string.detail_saved else R.string.detail_save),
                onClick = onGuardar
            )
            SheetRow(Icons.Outlined.Share, stringResource(R.string.detail_share), onCompartir)
            SheetRow(Icons.Outlined.Flag, stringResource(R.string.detail_report), onReportar)
        }
    }
}

@Composable
private fun SheetRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun initials(name: String): String =
    name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }

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