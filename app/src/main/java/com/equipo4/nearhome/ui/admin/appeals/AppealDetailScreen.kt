package com.equipo4.nearhome.ui.admin.appeals

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.equipo4.nearhome.data.remote.evidenciaApelacionUrl
import com.equipo4.nearhome.domain.model.Appeal
import com.equipo4.nearhome.domain.model.AppealStatus
import com.equipo4.nearhome.domain.model.AppealType
import com.equipo4.nearhome.domain.model.Property
import com.equipo4.nearhome.ui.admin.AvisoAccionBloqueada
import com.equipo4.nearhome.ui.admin.users_crud.UserAvatar
import com.equipo4.nearhome.ui.common.DetailBlocksSkeleton
import com.equipo4.nearhome.ui.publicacion.detail.FullScreenPhotoViewer
import com.equipo4.nearhome.ui.theme.NoticeTone
import com.equipo4.nearhome.ui.theme.noticeColors

/**
 * Detalle de una apelación. Mismo orden y estilo que el detalle de un reporte:
 * explicación, resultado (si ya se resolvió), imágenes, quién apela, la decisión apelada y los botones.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppealDetailScreen(
    appealId: String,
    onBackClick: () -> Unit = {},
    onNavigateToConfirmation: (String) -> Unit = {},
    onPropertyClick: (String) -> Unit = {},
    onUserClick: (Int) -> Unit = {}
) {
    val viewModel: AppealDetailViewModel = viewModel(factory = AppealDetailViewModelFactory(appealId))
    val appeal by viewModel.appeal.collectAsState()
    val targetProperty by viewModel.targetProperty.collectAsState()
    val sending by viewModel.sending.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current
    // Las imágenes necesitan el token: se manda en el encabezado de cada una
    val token = remember { SessionManager(context).getToken().orEmpty() }
    // Un administrador no resuelve sus propias apelaciones ni las de una decisión que él tomó (el API también lo bloquea)
    val miId = remember { SessionManager(context).getUserId() }
    val esPropia = miId != null && (appeal?.appellantId == miId || appeal?.measureById == miId)
    // Acción elegida: "ACEPTADA" (revertir) o "RECHAZADA" (mantener). Null = no se muestra el diálogo
    var accion by remember { mutableStateOf<String?>(null) }
    // Imagen abierta a pantalla completa (null = cerrado)
    var evidenceViewerIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    val subtitle = appeal?.let { a ->
        val estado = stringResource(
            when (a.status) {
                AppealStatus.PENDIENTE -> R.string.status_pending
                AppealStatus.ACEPTADA -> R.string.appeal_status_accepted
                AppealStatus.RECHAZADA -> R.string.appeal_status_rejected
            }
        )
        "${a.type.label} · $estado"
    } ?: stringResource(R.string.report_detail_fallback)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.appeal_detail_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = subtitle,
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
            if (appeal?.status == AppealStatus.PENDIENTE && esPropia) {
                AvisoAccionBloqueada(stringResource(R.string.admin_conflict_appeal))
            } else if (appeal?.status == AppealStatus.PENDIENTE) {
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
                            onClick = { accion = "ACEPTADA" },
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
                                text = stringResource(R.string.appeal_action_revert),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { accion = "RECHAZADA" },
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
                                text = stringResource(R.string.appeal_action_keep),
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
        val actual = appeal
        if (actual == null) {
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
                // Encabezado: tipo, fecha y la explicación de quien apela
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
                            AppealTypeTag(actual.type)
                            Text(
                                text = actual.date,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.appeal_explanation_title),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = actual.description,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Resultado: decisión revertida (verde) o mantenida (gris), con la respuesta
                if (actual.status != AppealStatus.PENDIENTE) ResultCard(actual)

                // Imágenes (con token)
                if (actual.evidences.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SectionTitle(stringResource(R.string.evidences_title))
                            val cantidad = actual.evidences.size
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
                            itemsIndexed(actual.evidences) { index, evidenciaId ->
                                Card(
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clickable { evidenceViewerIndex = index },
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(1.dp)
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(evidenciaApelacionUrl(evidenciaId))
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

                        evidenceViewerIndex?.let { start ->
                            val requests = actual.evidences.map { id ->
                                ImageRequest.Builder(context)
                                    .data(evidenciaApelacionUrl(id))
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

                // Quién apela
                Column(modifier = Modifier.fillMaxWidth()) {
                    SectionTitle(stringResource(R.string.appeal_appellant_title))
                    Spacer(Modifier.height(10.dp))
                    PersonCard(
                        name = actual.appellantName,
                        photoUrl = actual.appellantPhotoUrl,
                        caption = actual.appellantEmail,
                        onClick = { onUserClick(actual.appellantId) }
                    )
                }

                // La decisión apelada
                Column(modifier = Modifier.fillMaxWidth()) {
                    SectionTitle(stringResource(R.string.appeal_decision_title))
                    Spacer(Modifier.height(10.dp))
                    DecisionCard(actual)
                    if (actual.type == AppealType.PUBLICACION) {
                        Spacer(Modifier.height(10.dp))
                        targetProperty?.let { propiedad ->
                            PropertyRow(propiedad, onClick = { onPropertyClick(propiedad.id) })
                        } ?: Text(
                            text = actual.publicationTitle ?: stringResource(R.string.report_publication_unavailable),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }

    accion?.let { estado ->
        ResolveAppealDialog(
            revertir = estado == "ACEPTADA",
            tipo = appeal?.type ?: AppealType.PUBLICACION,
            enviando = sending,
            onDismiss = { accion = null },
            onConfirm = { nota ->
                viewModel.resolver(estado, nota) {
                    accion = null
                    onNavigateToConfirmation(appealId)
                }
            }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

/** Resultado de la apelación. Verde si se revirtió la decisión; gris si se mantuvo. */
@Composable
private fun ResultCard(appeal: Appeal) {
    val colors = noticeColors(if (appeal.status == AppealStatus.ACEPTADA) NoticeTone.SUCCESS else NoticeTone.NEUTRAL)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.container)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.report_detail_result_title),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.content
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(
                    if (appeal.status == AppealStatus.ACEPTADA) R.string.appeal_result_reverted
                    else R.string.appeal_result_kept
                ),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.content
            )
            val porQuien = listOfNotNull(appeal.resolvedDate, appeal.resolvedBy).joinToString(" · ")
            if (porQuien.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(text = porQuien, fontSize = 12.sp, color = colors.content.copy(alpha = 0.85f))
            }
            appeal.note?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(8.dp))
                Text(text = it, fontSize = 13.sp, color = colors.content)
            }
        }
    }
}

/** La decisión que se apeló: motivo, quién y cuándo, y en suspensiones hasta cuándo dura. */
@Composable
private fun DecisionCard(appeal: Appeal) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(
                    if (appeal.type == AppealType.PUBLICACION) R.string.appeal_decision_listing
                    else R.string.appeal_decision_account
                ),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (appeal.type == AppealType.SUSPENSION) {
                Text(
                    text = when {
                        !appeal.suspensionActive -> stringResource(R.string.appeal_suspension_ended)
                        appeal.suspensionUntil != null -> stringResource(R.string.suspended_until, appeal.suspensionUntil)
                        else -> stringResource(R.string.suspended_indefinite)
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Text(
                text = stringResource(R.string.motive_title),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = appeal.measureReason.orEmpty(),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            val porQuien = listOfNotNull(appeal.measureByName, appeal.measureDate).joinToString(" · ")
            if (porQuien.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.appeal_decision_by, porQuien),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Tarjeta de una persona con foto (o iniciales), como la del anunciante en el detalle de un reporte. */
@Composable
private fun PersonCard(name: String, photoUrl: String?, caption: String?, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
            UserAvatar(name = name, photoUrl = photoUrl, size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                caption?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(2.dp))
                    Text(text = it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = stringResource(R.string.user_profile_view),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** El anuncio apelado, con el mismo diseño que en el detalle de un reporte. */
@Composable
private fun PropertyRow(propiedad: Property, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
}

/**
 * Pide la respuesta para quien apeló (obligatoria, la va a ver). Explica qué pasa al confirmar:
 * revertir vuelve a publicar el anuncio o quita la suspensión; mantener deja todo igual.
 */
@Composable
private fun ResolveAppealDialog(
    revertir: Boolean,
    tipo: AppealType,
    enviando: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (nota: String) -> Unit
) {
    var nota by rememberSaveable { mutableStateOf("") }
    val titulo = stringResource(if (revertir) R.string.appeal_action_revert else R.string.appeal_action_keep)
    val efecto = stringResource(
        when {
            !revertir -> R.string.appeal_keep_effect
            tipo == AppealType.SUSPENSION -> R.string.appeal_revert_effect_account
            else -> R.string.appeal_revert_effect_listing
        }
    )

    AlertDialog(
        onDismissRequest = { if (!enviando) onDismiss() },
        title = { Text(titulo) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = efecto, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                OutlinedTextField(
                    value = nota,
                    onValueChange = { if (it.length <= 300) nota = it },
                    label = { Text(stringResource(R.string.appeal_note_label)) },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = stringResource(R.string.appeal_note_notice),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = nota.trim().isNotEmpty() && !enviando,
                onClick = { onConfirm(nota.trim()) }
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
}
