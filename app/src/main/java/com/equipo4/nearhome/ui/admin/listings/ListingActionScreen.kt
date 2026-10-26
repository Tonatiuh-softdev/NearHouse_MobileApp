package com.equipo4.nearhome.ui.admin.listings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.equipo4.nearhome.domain.model.AdminListing
import com.equipo4.nearhome.domain.model.ListingAction
import com.equipo4.nearhome.ui.theme.NoticeTone
import com.equipo4.nearhome.ui.theme.adminAccent
import com.equipo4.nearhome.ui.theme.noticeColors
import com.equipo4.nearhome.ui.theme.onAdminAccent
import com.equipo4.nearhome.data.local.SessionManager

/**
 * Pantalla de confirmación. Las cinco acciones comparten la misma estructura
 * (icono, pregunta, resumen, cambio de estado y botones); solo cambia la parte del medio.
 *
 * [onConfirmed] recibe el mensaje verde para mostrar en el Detalle.
 * [onDeleted] recibe el mensaje para la lista (el anuncio ya no existe).
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
    val listing = viewModel.listing
    val context = LocalContext.current
    val actor = remember<String?> {
        SessionManager(context).getUserName()
    }?.takeIf { it.isNotBlank() } ?: stringResource(R.string.listing_default_actor)

    val successMessage = when (action) {
        ListingAction.APROBAR -> stringResource(R.string.listing_msg_approved)
        ListingAction.RECHAZAR -> stringResource(R.string.listing_msg_rejected)
        ListingAction.DESACTIVAR -> stringResource(R.string.listing_msg_deactivated)
        ListingAction.REACTIVAR -> stringResource(R.string.listing_msg_reactivated)
        ListingAction.ELIMINAR -> stringResource(R.string.listing_msg_deleted, listingId)
    }
    val isValid = isActionFormValid(action, form)
    val danger = noticeColors(NoticeTone.DANGER).content
    val accent = adminAccent()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(action.titleRes()),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (action == ListingAction.RECHAZAR || action == ListingAction.ELIMINAR) danger
                        else MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, accent),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = accent),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) { Text(stringResource(R.string.listing_action_cancel)) }

                val destructive = action == ListingAction.ELIMINAR || action == ListingAction.RECHAZAR
                Button(
                    onClick = {
                        if (viewModel.confirm(actor)) {
                            if (action == ListingAction.ELIMINAR) onDeleted(successMessage)
                            else onConfirmed(successMessage)
                        }
                    },
                    enabled = isValid && listing != null,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (destructive) danger else accent,
                        contentColor = if (destructive) androidx.compose.ui.graphics.Color.White else onAdminAccent(),
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.weight(1.4f).height(48.dp)
                ) { Text(stringResource(action.confirmRes()), maxLines = 1) }
            }
        },
        // El teclado empuja la barra de botones para que no los tape.
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
            Header(action)
            ListingSummaryCard(listing)
            StatusChangeRow(from = listing.status, to = action.targetStatus)
            ActionSpecifics(action, listing, form, viewModel)
        }
    }
}

/** Ícono en un círculo claro a la izquierda, pregunta y explicación a la derecha. */
@Composable
private fun Header(action: ListingAction) {
    val (icon, tone) = when (action) {
        ListingAction.APROBAR -> Icons.Filled.CheckCircle to NoticeTone.SUCCESS
        ListingAction.RECHAZAR -> Icons.Filled.Cancel to NoticeTone.DANGER
        ListingAction.DESACTIVAR -> Icons.Filled.PauseCircle to NoticeTone.WARNING
        ListingAction.REACTIVAR -> Icons.Filled.PlayCircle to NoticeTone.SUCCESS
        ListingAction.ELIMINAR -> Icons.Filled.Delete to NoticeTone.DANGER
    }
    val colors = noticeColors(tone)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp).background(colors.container, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = colors.content, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                stringResource(action.questionRes()),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                stringResource(action.bodyRes()),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Tarjeta blanca con borde fino, igual que en el Figma. */
@Composable
private fun InfoCard(
    verticalSpacing: androidx.compose.ui.unit.Dp = 8.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(verticalSpacing)) {
            content()
        }
    }
}

@Composable
private fun ActionSpecifics(
    action: ListingAction,
    listing: AdminListing,
    form: ActionForm,
    vm: ListingActionViewModel
) {
    when (action) {
        ListingAction.APROBAR -> {
            InfoCard(verticalSpacing = 4.dp) {
                Text(
                    stringResource(R.string.listing_approve_checklist_title),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val items = listOf(
                    R.string.listing_approve_check_1,
                    R.string.listing_approve_check_2,
                    R.string.listing_approve_check_3
                )
                items.forEachIndexed { i, res ->
                    CheckRow(
                        checked = form.checks[i],
                        onCheckedChange = { vm.setCheck(i, it) },
                        text = stringResource(res)
                    )
                }
            }
        }

        ListingAction.RECHAZAR -> ReasonField(
            value = form.text,
            onValueChange = vm::setText,
            label = stringResource(R.string.listing_reject_label),
            isValid = isActionFormValid(action, form),
            errorText = if (form.text.isBlank()) stringResource(R.string.listing_reject_error_required)
            else stringResource(R.string.listing_reject_error_min, MIN_REJECT_CHARS),
            hint = stringResource(R.string.listing_reject_hint, MIN_REJECT_CHARS)
        )

        ListingAction.DESACTIVAR -> {
            NoticeBanner(
                tone = NoticeTone.WARNING,
                text = stringResource(R.string.listing_deactivate_warning),
                icon = Icons.Filled.Warning
            )
            ReasonField(
                value = form.text,
                onValueChange = vm::setText,
                label = stringResource(R.string.listing_deactivate_label),
                isValid = isActionFormValid(action, form),
                errorText = stringResource(R.string.listing_deactivate_error_required)
            )
        }

        ListingAction.REACTIVAR -> {
            listing.deactivation?.let { info ->
                InfoCard {
                    Text(
                        stringResource(R.string.listing_previous_deactivation),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    InfoLine(stringResource(R.string.listing_previous_date), formatDateTime(info.timestamp))
                    InfoLine(stringResource(R.string.listing_previous_cause), info.cause)
                    InfoLine(stringResource(R.string.listing_previous_by), info.actor)
                }
            }
            NoticeBanner(tone = NoticeTone.SUCCESS, text = stringResource(R.string.listing_reactivate_notice))
        }

        ListingAction.ELIMINAR -> {
            val photos = listing.images.size
            NoticeBanner(
                tone = NoticeTone.DANGER,
                text = pluralStringResource(R.plurals.listing_delete_warning, photos, photos),
                icon = Icons.Filled.Warning
            )
            ReasonField(
                value = form.text,
                onValueChange = vm::setText,
                label = stringResource(R.string.listing_delete_label),
                isValid = form.text.isNotBlank(),
                errorText = stringResource(R.string.listing_delete_error_required)
            )
            InfoCard(verticalSpacing = 6.dp) {
                Text(
                    stringResource(R.string.listing_delete_list_title),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                BulletLine(stringResource(R.string.listing_delete_item_listing))
                BulletLine(pluralStringResource(R.plurals.listing_delete_item_photos, photos, photos))
                BulletLine(stringResource(R.string.listing_delete_item_history))
            }
            CheckRow(
                checked = form.acknowledged,
                onCheckedChange = vm::setAcknowledged,
                text = stringResource(R.string.listing_delete_acknowledge)
            )
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Column {
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

/** Lista de "Se eliminará": una X roja por elemento. */
@Composable
private fun BulletLine(text: String) {
    val danger = noticeColors(NoticeTone.DANGER).content
    Row(verticalAlignment = Alignment.Top) {
        Text("✕", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = danger)
        Spacer(Modifier.width(10.dp))
        Text(text, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun ListingAction.titleRes(): Int = when (this) {
    ListingAction.APROBAR -> R.string.listing_approve_title
    ListingAction.RECHAZAR -> R.string.listing_reject_title
    ListingAction.DESACTIVAR -> R.string.listing_deactivate_title
    ListingAction.REACTIVAR -> R.string.listing_reactivate_title
    ListingAction.ELIMINAR -> R.string.listing_delete_title
}

private fun ListingAction.questionRes(): Int = when (this) {
    ListingAction.APROBAR -> R.string.listing_approve_question
    ListingAction.RECHAZAR -> R.string.listing_reject_question
    ListingAction.DESACTIVAR -> R.string.listing_deactivate_question
    ListingAction.REACTIVAR -> R.string.listing_reactivate_question
    ListingAction.ELIMINAR -> R.string.listing_delete_question
}

private fun ListingAction.bodyRes(): Int = when (this) {
    ListingAction.APROBAR -> R.string.listing_approve_body
    ListingAction.RECHAZAR -> R.string.listing_reject_body
    ListingAction.DESACTIVAR -> R.string.listing_deactivate_body
    ListingAction.REACTIVAR -> R.string.listing_reactivate_body
    ListingAction.ELIMINAR -> R.string.listing_delete_body
}

private fun ListingAction.confirmRes(): Int = when (this) {
    ListingAction.APROBAR -> R.string.listing_approve_confirm
    ListingAction.RECHAZAR -> R.string.listing_reject_confirm
    ListingAction.DESACTIVAR -> R.string.listing_deactivate_confirm
    ListingAction.REACTIVAR -> R.string.listing_reactivate_confirm
    ListingAction.ELIMINAR -> R.string.listing_delete_confirm
}