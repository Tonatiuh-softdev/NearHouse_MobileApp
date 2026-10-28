package com.equipo4.nearhome.ui.publicacion.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo4.nearhome.R
import com.equipo4.nearhome.domain.model.PropertyType
import com.equipo4.nearhome.ui.common.FakeMapPreview
import com.equipo4.nearhome.ui.theme.nearHouseColors
import com.equipo4.nearhome.util.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailPublicacionScreen(
    publicacionId: String,
    onBackClick: () -> Unit,
    onReportClick: () -> Unit,
    onEditClick: () -> Unit,
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
                is DetailPublicacionUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is DetailPublicacionUiState.Error -> {
                    Text(
                        text = state.mensaje,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is DetailPublicacionUiState.Success -> {
                    val pub = state.publicacion

                    DetailContent(
                        pub = pub,
                        onReportClick = onReportClick
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
                                onCompartir = {
                                    showBottomSheet = false
                                },
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
    onReportClick: () -> Unit
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
        // --- CARRUSEL DE IMÁGENES (componente compartido) ---
        PhotoGallery(
            images = pub.imagenes,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            // Etiqueta para Vendedor ("Tu Publicación")
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
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { selectedTab = 0 },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoCamera,
                        contentDescription = stringResource(R.string.detail_photos_cd),
                        tint = if (selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (selectedTab == 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { selectedTab = 1 },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = stringResource(R.string.search_filters_location),
                        tint = if (selectedTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (selectedTab == 1) {
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

        Spacer(modifier = Modifier.height(16.dp))

        // --- PRECIO Y TIPO DE PROPIEDAD ---
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

            // Dirección
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

            // Mini Mapa
            FakeMapPreview(
                propertyType = when (pub.tipoPropiedad.lowercase()) {
                    "casa" -> PropertyType.CASA
                    "terreno" -> PropertyType.TERRENO
                    else -> PropertyType.DEPARTAMENTO
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- FILA DE ESPECIFICACIONES ---
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

            // Título y Descripción
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
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = if (isExpandedDescription || pub.esPropietario) Int.MAX_VALUE else 3
            )

            if (!pub.esPropietario && !isExpandedDescription) {
                Text(
                    text = stringResource(R.string.detail_read_more),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { isExpandedDescription = true }
                        .padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Lista dinámica de características
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

            // Franja de reporte (solo Comprador)
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

            // Botones de Contacto
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://api.whatsapp.com/send?phone=${pub.telefonoVendedor}&text=${Uri.encode(AppStrings.get(R.string.detail_whatsapp_message))}")
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
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.nearHouseColors.whatsappBtnText)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "WhatsApp", color = MaterialTheme.nearHouseColors.whatsappBtnText, fontWeight = FontWeight.Bold)
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
                    Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = MaterialTheme.nearHouseColors.correoBtnText)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.detail_email), color = MaterialTheme.nearHouseColors.correoBtnText, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PrecioSection(precio: Double, esRenta: Boolean) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = if (esRenta) stringResource(R.string.detail_rent_price_format, String.format("%,.0f", precio)) else stringResource(R.string.property_price_format, String.format("%,.0f", precio)),
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
private fun FeatureItem(
    iconRes: Int? = null,
    vectorIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        } else if (vectorIcon != null) {
            Icon(
                imageVector = vectorIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEditar() }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = stringResource(R.string.create_listing_edit), color = MaterialTheme.colorScheme.onSurface)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCompartir() }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Share, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = stringResource(R.string.detail_share), color = MaterialTheme.colorScheme.onSurface)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onGuardar() }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (estaGuardado) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (estaGuardado) stringResource(R.string.detail_saved) else stringResource(R.string.detail_save),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCompartir() }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Share, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = stringResource(R.string.detail_share), color = MaterialTheme.colorScheme.onSurface)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onReportar() }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = stringResource(R.string.detail_report), color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}