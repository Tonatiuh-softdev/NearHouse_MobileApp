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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo4.nearhome.R
import com.equipo4.nearhome.domain.model.PropertyType
import com.equipo4.nearhome.ui.common.FakeMapPreview

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    IconButton(onClick = { showBottomSheet = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menú de opciones")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            when (val state = uiState) {
                is DetailPublicacionUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DetailPublicacionUiState.Error -> {
                    Text(
                        text = state.mensaje,
                        color = Color.Red,
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
                            containerColor = Color.White
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
            // Etiqueta para Vendedor
            if (pub.esPropietario) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(Color(0xFF007AFF), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Tu Publicación",
                        color = Color.White,
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
                        contentDescription = "Fotos",
                        tint = if (selectedTab == 0) Color(0xFF007AFF) else Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (selectedTab == 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(Color(0xFF007AFF))
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
                        contentDescription = "Ubicación",
                        tint = if (selectedTab == 1) Color(0xFF007AFF) else Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (selectedTab == 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(Color(0xFF007AFF))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- PRECIO Y TIPO DE PROPIEDAD (ORDEN CONDICIONADO POR ROL) ---
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
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = pub.direccion,
                    color = Color.Gray,
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

            // --- FILA DE ESPECIFICACIONES CON LOS NUEVOS ÍCONOS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FeatureItem(iconRes = R.drawable.ic_regla_medicion, label = "${pub.metrosCuadrados} m²")
                FeatureItem(iconRes = R.drawable.ic_habitaciones, label = "${pub.recamaras} recamaras")
                FeatureItem(iconRes = R.drawable.ic_bathrooms, label = "${pub.banos} baños")
                FeatureItem(iconRes = R.drawable.ic_cocheras, label = "${pub.cocheras} cocheras")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Título y Descripción
            Text(
                text = pub.titulo.uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = pub.descripcion,
                fontSize = 13.sp,
                color = Color.DarkGray,
                maxLines = if (isExpandedDescription || pub.esPropietario) Int.MAX_VALUE else 3
            )

            if (!pub.esPropietario && !isExpandedDescription) {
                Text(
                    text = "Leer más",
                    color = Color(0xFF007AFF),
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
                text = "Más detalles",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    pub.caracteristicasColumna1.forEach { item ->
                        Text(text = item, fontSize = 13.sp, color = Color.Gray)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    pub.caracteristicasColumna2.forEach { item ->
                        Text(text = item, fontSize = 13.sp, color = Color.Gray)
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
                    Text(text = "¿Tienes algún problema con este anuncio? ", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        text = "Repórtalo aquí",
                        fontSize = 12.sp,
                        color = Color(0xFF007AFF),
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
                            data = Uri.parse("https://api.whatsapp.com/send?phone=${pub.telefonoVendedor}&text=Hola,%20estoy%20interesado%20en%20tu%20publicaci%C3%B3n")
                        }
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "WhatsApp", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:${pub.emailVendedor}")
                            putExtra(Intent.EXTRA_SUBJECT, "Consulta sobre publicación: ${pub.titulo}")
                        }
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B5B84)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Correo", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PrecioSection(precio: Double, esRenta: Boolean) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = if (esRenta) "Renta MX \$${String.format("%,.0f", precio)}" else "MX \$${String.format("%,.0f", precio)}",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.Black
        )
        if (esRenta) {
            Text(
                text = " /mes",
                fontSize = 13.sp,
                color = Color.Gray,
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
            tint = Color.Unspecified,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = tipo, fontSize = 13.sp, color = Color.Gray)
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
                tint = Color.Unspecified,
                modifier = Modifier.size(18.dp)
            )
        } else if (vectorIcon != null) {
            Icon(
                imageVector = vectorIcon,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 11.sp, color = Color.Gray)
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
            text = "Herramientas",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
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
                Icon(Icons.Outlined.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "Editar")
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCompartir() }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "Compartir")
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
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = if (estaGuardado) "Guardado" else "Guardar")
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCompartir() }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "Compartir")
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onReportar() }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Flag, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "Reportar")
            }
        }
    }
}

@Composable
private fun PhotoGallery(
    images: List<Int>,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val totalPages = images.ifEmpty { listOf(1) }.size
    val pagerState = rememberPagerState(pageCount = { totalPages })

    Box(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val imageRes = images.getOrNull(page) ?: R.drawable.prop_depto_sala
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        content()
    }
}