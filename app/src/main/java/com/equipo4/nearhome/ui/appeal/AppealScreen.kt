package com.equipo4.nearhome.ui.appeal

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo4.nearhome.R
import com.equipo4.nearhome.ui.common.NearHomeDialog
import com.equipo4.nearhome.ui.publicacion.detail.FullScreenPhotoViewer
import com.equipo4.nearhome.ui.publicacion.report.EvidenceFile
import com.equipo4.nearhome.ui.publicacion.report.EvidenceThumbnail
import com.equipo4.nearhome.util.AppStrings

/**
 * "Apelar decisión": misma estructura y estilo que la pantalla de Reportar.
 * 1. La decisión que se apela. 2. Aviso de quién la revisa. 3. Explicación. 4. Imágenes (opcional). 5. Enviar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppealScreen(
    viewModel: AppealViewModel = viewModel(),
    onBackClick: () -> Unit,
    onAppealSubmitted: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showDiscardDialog by remember { mutableStateOf(false) }
    // Índice de la imagen abierta a pantalla completa (null = cerrado)
    var previewIndex by remember { mutableStateOf<Int?>(null) }

    // Si la app se cerró y se abrió aquí, ya no hay decisión que apelar: se regresa
    LaunchedEffect(uiState.objetivo) {
        if (uiState.objetivo == null && !uiState.isSubmitted) onBackClick()
    }

    fun handleBack() {
        if (uiState.hasUnsavedChanges) showDiscardDialog = true else onBackClick()
    }

    BackHandler { handleBack() }

    // Solo imágenes
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.addEvidence(EvidenceFile(it)) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.appeal_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { handleBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.appeal_question),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                lineHeight = 26.sp,
                modifier = Modifier.fillMaxWidth(0.9f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // TARJETA 1: La decisión que se apela
            uiState.objetivo?.let { objetivo -> DecisionCard(objetivo) }

            Spacer(modifier = Modifier.height(16.dp))

            // Aviso: quién la revisa y que solo hay una apelación (mismo estilo que el aviso de Reportar)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.appeal_notice),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        lineHeight = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TARJETA 2: Explicación
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.appeal_description_title),
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = { viewModel.onDescriptionChange(it) },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.appeal_description_placeholder),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                lineHeight = 18.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            focusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Lo que falta para poder enviar
                    val faltan = MIN_PALABRAS_APELACION - uiState.palabrasDescripcion
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (faltan > 0) {
                                stringResource(R.string.appeal_words_missing, faltan)
                            } else {
                                stringResource(R.string.appeal_ready)
                            },
                            fontSize = 11.sp,
                            color = if (faltan > 0) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${uiState.description.length}/$MAX_CARACTERES_APELACION",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TARJETA 3: Imágenes (opcional, hasta 5)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.report_evidence_title),
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${uiState.evidenceFiles.size}/5",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        uiState.evidenceFiles.forEachIndexed { index, file ->
                            EvidenceThumbnail(
                                file = file,
                                onClick = { previewIndex = index },
                                onRemove = { viewModel.removeEvidence(file) }
                            )
                        }

                        if (uiState.evidenceFiles.size < 5) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                                    .clickable {
                                        pickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = stringResource(R.string.report_add_evidence_cd),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Botón Enviar
            Button(
                onClick = { viewModel.submit(context) },
                enabled = uiState.isSendEnabled && !uiState.isSending,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                )
            ) {
                if (uiState.isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = stringResource(R.string.appeal_send),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }

    previewIndex?.let { index ->
        val uris = uiState.evidenceFiles.map { it.uri }
        if (uris.isNotEmpty()) {
            FullScreenPhotoViewer(
                images = uris,
                initialPage = index,
                onDismiss = { previewIndex = null }
            )
        }
    }

    // MODAL DE ERROR
    uiState.errorMessage?.let { msg ->
        NearHomeDialog(
            title = stringResource(R.string.appeal_error_title),
            message = msg,
            confirmText = stringResource(R.string.report_common_ok),
            onConfirm = {
                val salir = uiState.salirAlCerrarError
                viewModel.clearErrorMessage()
                if (salir) onBackClick()
            },
            dismissText = null,
            onDismiss = { viewModel.clearErrorMessage() },
            destructive = true
        )
    }

    // MODAL DE ÉXITO
    if (uiState.isSubmitted) {
        val esSuspension = uiState.objetivo is ObjetivoApelacion.Suspension
        NearHomeDialog(
            title = stringResource(R.string.appeal_success_title),
            message = stringResource(
                if (esSuspension) R.string.appeal_success_message_account else R.string.appeal_success_message_listing
            ),
            confirmText = stringResource(R.string.report_success_button),
            onConfirm = onAppealSubmitted,
            dismissText = null,
            onDismiss = { }, // No se cierra tocando afuera para que lean el mensaje
            destructive = false
        )
    }

    // MODAL DE DESCARTAR
    if (showDiscardDialog) {
        NearHomeDialog(
            title = stringResource(R.string.appeal_discard_title),
            message = stringResource(R.string.report_discard_message),
            confirmText = stringResource(R.string.report_common_discard),
            onConfirm = {
                showDiscardDialog = false
                onBackClick()
            },
            dismissText = stringResource(R.string.report_common_cancel),
            onDismiss = { showDiscardDialog = false },
            destructive = true
        )
    }
}

/** La decisión que se apela, con el mismo encabezado gris que las tarjetas de Reportar. */
@Composable
private fun DecisionCard(objetivo: ObjetivoApelacion) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = stringResource(R.string.appeal_decision_title),
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                when (objetivo) {
                    is ObjetivoApelacion.Publicacion -> {
                        Text(
                            text = stringResource(R.string.appeal_decision_listing),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = objetivo.titulo,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        objetivo.motivo?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                text = stringResource(R.string.suspended_reason, it),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    is ObjetivoApelacion.Suspension -> {
                        val hasta = CuentaSuspendida(objetivo.motivo, objetivo.hasta, null, null, null)
                            .hastaTexto(AppStrings.locale())
                        Text(
                            text = stringResource(R.string.appeal_decision_account),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (hasta != null) stringResource(R.string.suspended_until, hasta)
                            else stringResource(R.string.suspended_indefinite),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (objetivo.motivo.isNotBlank()) {
                            Text(
                                text = stringResource(R.string.suspended_reason, objetivo.motivo),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
