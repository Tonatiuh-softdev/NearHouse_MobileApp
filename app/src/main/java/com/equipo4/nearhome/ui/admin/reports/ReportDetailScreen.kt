package com.equipo4.nearhome.ui.admin.reports

import com.equipo4.nearhome.ui.common.DetailBlocksSkeleton
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.remote.evidenciaReporteUrl
import com.equipo4.nearhome.domain.model.ReportStatus
import com.equipo4.nearhome.domain.model.ReportType
import com.equipo4.nearhome.ui.admin.users_crud.UserAvatar
import com.equipo4.nearhome.ui.publicacion.detail.FullScreenPhotoViewer
import com.equipo4.nearhome.ui.theme.NoticeTone
import com.equipo4.nearhome.ui.theme.noticeColors
import androidx.compose.runtime.saveable.rememberSaveable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    reportId: String,
    onBackClick: () -> Unit = {},
    onNavigateToConfirmation: (String) -> Unit = {},
    onPropertyClick: (String) -> Unit = {},
    onUserClick: (Int) -> Unit = {}
) {
    val viewModel: ReportDetailViewModel = viewModel(
        factory = ReportDetailViewModelFactory(reportId)
    )
    val report by viewModel.report.collectAsState()
    val targetProperty by viewModel.targetProperty.collectAsState()
    val reporterId by viewModel.reporterId.collectAsState()
    val sending by viewModel.sending.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current
    // Las evidencias necesitan el token: se manda en el encabezado de cada imagen
    val token = remember { SessionManager(context).getToken().orEmpty() }
    // Acción elegida: "PROCEDENTE" o "DESCARTADO". Null = no se muestra el diálogo
    var accion by remember { mutableStateOf<String?>(null) }
    // Evidencia abierta a pantalla completa (null = cerrado)
    var evidenceViewerIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    val subtitleStatus = report?.let { r ->
        val statusLabel = if (r.status == ReportStatus.PENDIENTE) {
            stringResource(R.string.status_pending)
        } else {
            stringResource(R.string.status_attended)
        }
        "${r.type.label} · $statusLabel"
    } ?: stringResource(R.string.report_detail_fallback)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.report_detail_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = subtitleStatus,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            if (report?.status == ReportStatus.PENDIENTE) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
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
                            onClick = { accion = "PROCEDENTE" },
                            enabled = !sending,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.report_action_procede),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { accion = "DESCARTADO" },
                            enabled = !sending,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.report_common_discard),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        val currentReport = report
        if (currentReport == null) {
            DetailBlocksSkeleton(modifier = Modifier.fillMaxSize().padding(innerPadding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Encabezado: tipo, fecha, motivo y descripción
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ReportTypeTag(currentReport.type)
                            Text(
                                text = currentReport.date,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = stringResource(R.string.motive_title),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = currentReport.motive,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = currentReport.description,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Resultado de la resolución y la nota del administrador
                if (currentReport.status == ReportStatus.ATENDIDO && !currentReport.result.isNullOrEmpty()) {
                    val statusColors = noticeColors(NoticeTone.SUCCESS)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = statusColors.container)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.report_detail_result_title),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColors.content
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = currentReport.result.orEmpty(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = statusColors.content
                            )
                            currentReport.attendedDate?.let {
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = it,
                                    fontSize = 12.sp,
                                    color = statusColors.content.copy(alpha = 0.85f)
                                )
                            }
                            currentReport.note?.takeIf { it.isNotBlank() }?.let {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = it,
                                    fontSize = 13.sp,
                                    color = statusColors.content
                                )
                            }
                        }
                    }
                }

                // Evidencias (imágenes con token)
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
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val cantidad = currentReport.evidences.size
                            Text(
                                text = if (cantidad == 1) {
                                    stringResource(R.string.report_detail_files_count_one)
                                } else {
                                    stringResource(R.string.report_detail_files_count_other, cantidad)
                                },
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            itemsIndexed(currentReport.evidences) { index, evidenciaId ->
                                Card(
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clickable { evidenceViewerIndex = index },
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(1.dp)
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(evidenciaReporteUrl(evidenciaId))
                                            .addHeader("Authorization", "Bearer $token")
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = stringResource(R.string.report_evidence_cd),
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }

                        // Evidencias a pantalla completa
                        evidenceViewerIndex?.let { start ->
                            val requests = currentReport.evidences.map { id ->
                                ImageRequest.Builder(context)
                                    .data(evidenciaReporteUrl(id))
                                    .addHeader("Authorization", "Bearer $token")
                                    .crossfade(true)
                                    .build()
                            }
                            FullScreenPhotoViewer(
                                images = requests,
                                initialPage = start,
                                onDismiss = { evidenceViewerIndex = null }
                            )
                        }
                    }
                }

                // Quién reportó (solo lo ve el administrador)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.report_reporter_title),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(10.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                reporterId?.let { id -> Modifier.clickable { onUserClick(id) } } ?: Modifier
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentReport.reportedByName ?: stringResource(R.string.report_account_deleted),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                currentReport.reportedByEmail?.let {
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = it,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            // Solo se puede abrir el perfil si se encontró la cuenta
                            if (reporterId != null) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = stringResource(R.string.user_profile_view),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Publicación reportada
                if (currentReport.type == ReportType.PUBLICACION) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.reported_publication),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(10.dp))

                        targetProperty?.let { propiedad ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPropertyClick(propiedad.id) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (propiedad.images.isNotEmpty()) {
                                        AsyncImage(
                                            model = propiedad.images.first(),
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
                                            text = stringResource(R.string.report_rent_price_format, propiedad.price.toInt()),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = propiedad.title.lowercase().replaceFirstChar { it.uppercase() },
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = propiedad.location,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } ?: Text(
                            text = stringResource(R.string.report_publication_unavailable),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Quién publicó el anuncio reportado
                        val sellerId = targetProperty?.sellerId
                        if (sellerId != null) {
                            val sellerName = targetProperty?.sellerName
                                ?: stringResource(R.string.profile_default_name)
                            Spacer(Modifier.height(10.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onUserClick(sellerId) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    UserAvatar(
                                        name = sellerName,
                                        photoUrl = targetProperty?.sellerPhotoUrl,
                                        size = 40.dp
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(R.string.listing_advertiser),
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = sellerName,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = stringResource(R.string.user_profile_view),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }

    accion?.let { estado ->
        ResolveReportDialog(
            procede = estado == "PROCEDENTE",
            enviando = sending,
            onDismiss = { accion = null },
            onConfirm = { nota, retirar ->
                viewModel.resolver(estado, nota, retirar) {
                    accion = null
                    onNavigateToConfirmation(reportId)
                }
            }
        )
    }
}

/**
 * Pide la nota (obligatoria). En "Procede" hay un interruptor para retirar la publicación,
 * que pide confirmación antes de enviar.
 */
@Composable
private fun ResolveReportDialog(
    procede: Boolean,
    enviando: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (nota: String, retirarPublicacion: Boolean) -> Unit
) {
    var nota by rememberSaveable { mutableStateOf("") }
    var retirar by rememberSaveable { mutableStateOf(false) }
    var confirmarRetiro by remember { mutableStateOf(false) }
    val notaValida = nota.trim().isNotEmpty()
    val titulo = stringResource(if (procede) R.string.report_action_procede else R.string.report_common_discard)

    AlertDialog(
        onDismissRequest = { if (!enviando) onDismiss() },
        title = { Text(titulo) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nota,
                    onValueChange = { if (it.length <= 300) nota = it },
                    label = { Text(stringResource(R.string.report_note_label)) },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = stringResource(R.string.report_note_seller_notice),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (procede) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.report_remove_listing),
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = retirar,
                            onCheckedChange = { retirar = it },
                            enabled = !enviando
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = notaValida && !enviando,
                onClick = {
                    val notaLimpia = nota.trim()
                    if (procede && retirar) {
                        confirmarRetiro = true
                    } else {
                        onConfirm(notaLimpia, false)
                    }
                }
            ) {
                Text(titulo)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !enviando) {
                Text(stringResource(R.string.report_common_cancel))
            }
        }
    )

    if (confirmarRetiro) {
        AlertDialog(
            onDismissRequest = { confirmarRetiro = false },
            title = { Text(stringResource(R.string.report_remove_confirm_title)) },
            text = { Text(stringResource(R.string.report_remove_confirm_body)) },
            confirmButton = {
                TextButton(
                    enabled = !enviando,
                    onClick = {
                        confirmarRetiro = false
                        onConfirm(nota.trim(), true)
                    }
                ) {
                    Text(stringResource(R.string.report_remove_listing))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarRetiro = false }) {
                    Text(stringResource(R.string.report_common_cancel))
                }
            }
        )
    }
}