package com.equipo4.nearhome.ui.admin.listings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.domain.model.AdminListing
import com.equipo4.nearhome.domain.model.ListingAction
import com.equipo4.nearhome.domain.model.StatusChange
import com.equipo4.nearhome.ui.theme.NoticeTone
import com.equipo4.nearhome.ui.theme.adminAccent
import com.equipo4.nearhome.ui.theme.noticeColors
import com.equipo4.nearhome.ui.theme.onAdminAccent

/**
 * Confirmación de cada acción del administrador: aprobar, rechazar, desactivar,
 * reactivar y eliminar (según los mockups).
 * Si el API responde bien avisa con [onConfirmed]; al eliminar, con [onDeleted].
 * Si falla, muestra el error y se queda en la pantalla.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingActionScreen(
    listingId: String,
    action: ListingAction,
    onBackClick: () -> Unit = {},
    onConfirmed: (String) -> Unit = {},
    onDeleted: (String) -> Unit = {}
) {
    val viewModel: ListingActionViewModel = viewModel(
        key = "listing_action_${listingId}_${action.name}",
        factory = viewModelFactory { initializer { ListingActionViewModel(listingId, action) } }
    )
    val form by viewModel.form.collectAsState()
    val submitting by viewModel.submitting.collectAsState()
    val error by viewModel.error.collectAsState()
    val listing = viewModel.listing

    val context = LocalContext.current
    val nombreSesion = SessionManager(context).getUserName()
    val actor = nombreSesion?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.listing_default_actor)

    val mensajeExito = when (action) {
        ListingAction.APROBAR -> stringResource(R.string.listing_msg_approved)
        ListingAction.RECHAZAR -> stringResource(R.string.listing_msg_rejected)
        ListingAction.DESACTIVAR -> stringResource(R.string.listing_msg_deactivated)
        ListingAction.REACTIVAR -> stringResource(R.string.listing_msg_reactivated)
        ListingAction.ELIMINAR -> stringResource(R.string.listing_msg_deleted, listingId)
    }

    val danger = noticeColors(NoticeTone.DANGER).content
    val accent = adminAccent()
    val destructiva = action == ListingAction.RECHAZAR || action == ListingAction.ELIMINAR
    val puedeConfirmar = listing != null && !submitting && formularioValido(action, form)
    val colorConfirmar = if (destructiva) danger else accent
    val contenidoConfirmar = if (destructiva) Color.White else onAdminAccent()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(tituloRes(action)),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (destructiva) danger else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            stringResource(subtituloRes(action)),
                            fontSize = 12.sp,
                            color = if (destructiva) danger else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick, enabled = !submitting) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            FixedActionBar {
                OutlinedButton(
                    onClick = onBackClick,
                    enabled = !submitting,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, accent),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = accent),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) { Text(stringResource(R.string.listing_action_cancel)) }

                Button(
                    onClick = {
                        viewModel.confirm(actor) {
                            if (action == ListingAction.ELIMINAR) onDeleted(mensajeExito)
                            else onConfirmed(mensajeExito)
                        }
                    },
                    enabled = puedeConfirmar,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorConfirmar,
                        contentColor = contenidoConfirmar,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.weight(1.4f).height(48.dp)
                ) {
                    if (submitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(stringResource(confirmarRes(action)), maxLines = 1)
                    }
                }
            }
        },
        modifier = Modifier.imePadding(),
        containerColor = MaterialTheme.colorScheme.background
    ) { inner ->
        if (listing == null) return@Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CabeceraAccion(action)
            ListingSummaryCard(listing)
            ContenidoAccion(action, listing, form, viewModel)
            error?.let {
                NoticeBanner(tone = NoticeTone.DANGER, text = it, icon = Icons.Filled.Warning)
            }
        }
    }
}

/** Regla de validación: la misma que usa el ViewModel para no enviar datos incompletos. */
private fun formularioValido(action: ListingAction, form: ActionForm): Boolean = when (action) {
    ListingAction.APROBAR -> form.checks.all { it }
    ListingAction.RECHAZAR -> form.text.trim().length >= MIN_REJECT_CHARS
    ListingAction.DESACTIVAR -> form.text.isNotBlank()
    ListingAction.REACTIVAR -> true
    ListingAction.ELIMINAR -> form.text.isNotBlank() && form.acknowledged
}

@Composable
private fun CabeceraAccion(action: ListingAction) {
    val (icono, tono) = when (action) {
        ListingAction.APROBAR -> Icons.Filled.CheckCircle to NoticeTone.SUCCESS
        ListingAction.RECHAZAR -> Icons.Filled.Cancel to NoticeTone.DANGER
        ListingAction.DESACTIVAR -> Icons.Filled.Pause to NoticeTone.WARNING
        ListingAction.REACTIVAR -> Icons.Filled.Refresh to NoticeTone.SUCCESS
        ListingAction.ELIMINAR -> Icons.Filled.Delete to NoticeTone.DANGER
    }
    val colores = noticeColors(tono)
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(48.dp).background(colores.container, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, contentDescription = null, tint = colores.content, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                stringResource(encabezadoRes(action)),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (action == ListingAction.ELIMINAR) colores.content else MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                stringResource(encabezadoCuerpoRes(action)),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ContenidoAccion(
    action: ListingAction,
    listing: AdminListing,
    form: ActionForm,
    vm: ListingActionViewModel
) {
    val danger = noticeColors(NoticeTone.DANGER).content

    when (action) {
        ListingAction.APROBAR -> {
            StatusChangeRow(from = listing.status, to = action.targetStatus)
            InfoCard {
                Text(
                    stringResource(R.string.listing_approve_checklist_title),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                listOf(
                    R.string.listing_approve_check_1,
                    R.string.listing_approve_check_2,
                    R.string.listing_approve_check_3
                ).forEachIndexed { i, res ->
                    CheckRow(
                        checked = form.checks[i],
                        onCheckedChange = { vm.setCheck(i, it) },
                        text = stringResource(res)
                    )
                }
            }
            AvisoConTitulo(
                tone = NoticeTone.SUCCESS,
                texto = stringResource(R.string.listing_approve_history_note)
            )
        }

        ListingAction.RECHAZAR -> {
            ReasonField(
                value = form.text,
                onValueChange = vm::setText,
                label = stringResource(R.string.listing_reject_label_required),
                isValid = formularioValido(action, form),
                errorText = if (form.text.isBlank()) {
                    stringResource(R.string.listing_reject_error_required)
                } else {
                    stringResource(R.string.listing_reject_error_min, MIN_REJECT_CHARS)
                },
                hint = stringResource(R.string.listing_reject_placeholder)
            )
            AvisoConTitulo(
                tone = NoticeTone.WARNING,
                titulo = stringResource(R.string.listing_reject_saved_title),
                texto = stringResource(R.string.listing_reject_saved_body)
            )
            StatusChangeRow(from = listing.status, to = action.targetStatus)
            if (!formularioValido(action, form)) {
                Text(
                    stringResource(R.string.listing_reject_blocked),
                    fontSize = 12.sp,
                    color = danger
                )
            }
        }

        ListingAction.DESACTIVAR -> {
            AvisoConTitulo(
                tone = NoticeTone.WARNING,
                titulo = stringResource(R.string.listing_deactivate_warning_title),
                texto = stringResource(R.string.listing_deactivate_warning_body)
            )
            ReasonField(
                value = form.text,
                onValueChange = vm::setText,
                label = stringResource(R.string.listing_deactivate_label_required),
                isValid = formularioValido(action, form),
                errorText = stringResource(R.string.listing_deactivate_error_required),
                hint = stringResource(R.string.listing_deactivate_placeholder)
            )
            StatusChangeRow(from = listing.status, to = action.targetStatus)
            Text(
                stringResource(R.string.listing_deactivate_note),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        ListingAction.REACTIVAR -> {
            // El cambio más reciente que trae motivo es la última desactivación
            listing.history
                .filter { it.reason != null }
                .maxByOrNull { it.timestamp }
                ?.let { AvisoDesactivacionPrevia(it) }
            StatusChangeRow(from = listing.status, to = action.targetStatus)
            AvisoConTitulo(
                tone = NoticeTone.SUCCESS,
                texto = stringResource(R.string.listing_reactivate_note)
            )
        }

        ListingAction.ELIMINAR -> {
            AvisoConTitulo(
                tone = NoticeTone.DANGER,
                titulo = stringResource(R.string.listing_delete_alert_title),
                texto = stringResource(R.string.listing_delete_alert_body),
                icono = Icons.Filled.Warning
            )
            ReasonField(
                value = form.text,
                onValueChange = vm::setText,
                label = stringResource(R.string.listing_delete_label_required),
                isValid = formularioValido(action, form),
                errorText = stringResource(R.string.listing_delete_error_required),
                hint = stringResource(R.string.listing_delete_placeholder)
            )
            InfoCard {
                Text(
                    stringResource(R.string.listing_delete_list_title),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                FilaEliminacion(stringResource(R.string.listing_delete_row_info))
                FilaEliminacion(
                    pluralStringResource(
                        R.plurals.listing_delete_row_gallery,
                        listing.images.size,
                        listing.images.size
                    )
                )
                FilaEliminacion(stringResource(R.string.listing_delete_row_contacts))
            }
            ConfirmacionEliminar(
                marcada = form.acknowledged,
                onToggle = { vm.setAcknowledged(!form.acknowledged) }
            )
            Text(
                stringResource(R.string.listing_delete_footnote),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            content()
        }
    }
}

/** Aviso de color. El título es opcional. */
@Composable
private fun AvisoConTitulo(
    tone: NoticeTone,
    texto: String,
    titulo: String? = null,
    icono: ImageVector = Icons.Filled.Info
) {
    val colores = noticeColors(tone)
    Surface(
        color = colores.container,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, colores.content.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Icon(icono, contentDescription = null, tint = colores.content, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                titulo?.let {
                    Text(it, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colores.content)
                }
                Text(texto, fontSize = 12.sp, color = colores.content)
            }
        }
    }
}

/** Motivo de la desactivación anterior, con fecha y quién lo registró. */
@Composable
private fun AvisoDesactivacionPrevia(cambio: StatusChange) {
    val colores = noticeColors(NoticeTone.WARNING)
    Surface(
        color = colores.container,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, colores.content.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.listing_previous_deactivation),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colores.content,
                    modifier = Modifier.weight(1f)
                )
                Text(formatDateTime(cambio.timestamp), fontSize = 11.sp, color = colores.content)
            }
            Text(
                cambio.reason.orEmpty(),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "${stringResource(R.string.listing_previous_by)} ${cambio.actor}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FilaEliminacion(texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Filled.Close,
            contentDescription = null,
            tint = noticeColors(NoticeTone.DANGER).content,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(texto, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun ConfirmacionEliminar(marcada: Boolean, onToggle: () -> Unit) {
    val danger = noticeColors(NoticeTone.DANGER).content
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = noticeColors(NoticeTone.DANGER).container,
        border = BorderStroke(1.dp, danger),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = marcada,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(checkedColor = danger)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(R.string.listing_delete_acknowledge),
                fontSize = 13.sp,
                color = danger
            )
        }
    }
}

private fun tituloRes(action: ListingAction): Int = when (action) {
    ListingAction.APROBAR -> R.string.listing_approve_title
    ListingAction.RECHAZAR -> R.string.listing_reject_title
    ListingAction.DESACTIVAR -> R.string.listing_deactivate_title
    ListingAction.REACTIVAR -> R.string.listing_reactivate_title
    ListingAction.ELIMINAR -> R.string.listing_delete_title
}

private fun subtituloRes(action: ListingAction): Int = when (action) {
    ListingAction.APROBAR -> R.string.listing_approve_subtitle
    ListingAction.RECHAZAR -> R.string.listing_reject_subtitle
    ListingAction.DESACTIVAR -> R.string.listing_deactivate_subtitle
    ListingAction.REACTIVAR -> R.string.listing_reactivate_subtitle
    ListingAction.ELIMINAR -> R.string.listing_delete_subtitle
}

private fun encabezadoRes(action: ListingAction): Int = when (action) {
    ListingAction.APROBAR -> R.string.listing_approve_header
    ListingAction.RECHAZAR -> R.string.listing_reject_header
    ListingAction.DESACTIVAR -> R.string.listing_deactivate_header
    ListingAction.REACTIVAR -> R.string.listing_reactivate_header
    ListingAction.ELIMINAR -> R.string.listing_delete_header
}

private fun encabezadoCuerpoRes(action: ListingAction): Int = when (action) {
    ListingAction.APROBAR -> R.string.listing_approve_header_body
    ListingAction.RECHAZAR -> R.string.listing_reject_header_body
    ListingAction.DESACTIVAR -> R.string.listing_deactivate_header_body
    ListingAction.REACTIVAR -> R.string.listing_reactivate_header_body
    ListingAction.ELIMINAR -> R.string.listing_delete_header_body
}

private fun confirmarRes(action: ListingAction): Int = when (action) {
    ListingAction.APROBAR -> R.string.listing_approve_confirm
    ListingAction.RECHAZAR -> R.string.listing_reject_confirm
    ListingAction.DESACTIVAR -> R.string.listing_deactivate_confirm
    ListingAction.REACTIVAR -> R.string.listing_reactivate_confirm
    ListingAction.ELIMINAR -> R.string.listing_delete_confirm
}