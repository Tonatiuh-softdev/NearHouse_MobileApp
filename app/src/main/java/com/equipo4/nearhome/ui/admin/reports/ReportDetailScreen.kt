package com.equipo4.nearhome.ui.admin.reports

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo4.nearhome.R
import com.equipo4.nearhome.domain.model.ReportStatus
import com.equipo4.nearhome.domain.model.ReportType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    reportId: String,
    onBackClick: () -> Unit = {},
    onNavigateToConfirmation: (String) -> Unit = {},
    onPropertyClick: (String) -> Unit = {}
) {
    val viewModel: ReportDetailViewModel = viewModel(
        factory = ReportDetailViewModelFactory(reportId)
    )
    val report by viewModel.report.collectAsState()
    val targetProperty by viewModel.targetProperty.collectAsState()
    val scope = rememberCoroutineScope()

    val subtitleStatus = if (report != null) {
        val typeLabel = report!!.type.label
        val statusLabel = if (report!!.status == ReportStatus.PENDIENTE) "Pendiente" else "Atendido"
        "$typeLabel · $statusLabel"
    } else {
        "Detalle"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Detalle del reporte",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                        Text(
                            text = subtitleStatus,
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = Color(0xFF1F2937)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (report != null && report!!.status == ReportStatus.PENDIENTE) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    viewModel.applyMeasure { _ ->
                                        onNavigateToConfirmation(reportId)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B2C4D))
                        ) {
                            Text(
                                text = "Aplicar medida y atender",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    viewModel.closeWithoutMeasure {
                                        onNavigateToConfirmation(reportId)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4B5563))
                        ) {
                            Text(
                                text = "Cerrar sin aplicar medida",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF9FAFB)
    ) { innerPadding ->
        if (report == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF0B2C4D))
            }
        } else {
            val currentReport = report!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tarjeta de información principal (Tipo, fecha, motivo, descripción)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ReportTypeTag(currentReport.type)
                            Text(
                                text = currentReport.date,
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = stringResource(R.string.motive_title),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6B7280)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = currentReport.motive,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = currentReport.description,
                            fontSize = 14.sp,
                            color = Color(0xFF4B5563)
                        )
                    }
                }

                // Si ya está atendido, mostrar resultado
                if (currentReport.status == ReportStatus.ATENDIDO && !currentReport.result.isNullOrEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7).copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Resultado de resolución",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = currentReport.result,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF15803D)
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = currentReport.attendedDate ?: "",
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                }

                // Evidencias (Fila deslizable / LazyRow)
                if (currentReport.evidences.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.evidences_title),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937)
                            )
                            val filesCountText = if (currentReport.evidences.size == 1) "1 archivo" else "${currentReport.evidences.size} archivos"
                            Text(
                                text = filesCountText,
                                fontSize = 13.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(currentReport.evidences) { imageRes ->
                                Card(
                                    modifier = Modifier
                                        .size(110.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(1.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = imageRes),
                                        contentDescription = "Evidencia",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }

                // Elemento reportado (Publicación o Usuario)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (currentReport.type == ReportType.PUBLICACION)
                            stringResource(R.string.reported_publication)
                        else
                            stringResource(R.string.reported_user),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Spacer(Modifier.height(10.dp))

                    if (currentReport.type == ReportType.PUBLICACION) {
                        if (targetProperty != null) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPropertyClick(targetProperty!!.id) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (targetProperty!!.images.isNotEmpty()) {
                                        Image(
                                            painter = painterResource(id = targetProperty!!.images.first()),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(68.dp)
                                                .clip(RoundedCornerShape(10.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(Modifier.width(12.dp))
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Renta MX $${targetProperty!!.price.toInt()} / mes",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1F2937)
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = targetProperty!!.title.lowercase().replaceFirstChar { it.uppercase() },
                                            fontSize = 12.sp,
                                            color = Color(0xFF6B7280),
                                            maxLines = 1
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = targetProperty!!.location,
                                            fontSize = 11.sp,
                                            color = Color(0xFF6B7280),
                                            maxLines = 1
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = Color(0xFF9CA3AF)
                                    )
                                }
                            }
                        } else {
                            Text("Publicación no disponible", fontSize = 13.sp, color = Color(0xFF6B7280))
                        }
                    } else {
                        // Usuario reportado (Logan Howlett u otro)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(Color(0xFFE5E7EB), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Person,
                                        contentDescription = null,
                                        tint = Color(0xFF4B5563),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Logan Howlett",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1F2937)
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "Arrendador · Miembro desde 2024",
                                        fontSize = 12.sp,
                                        color = Color(0xFF6B7280)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = Color(0xFF9CA3AF)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
