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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.equipo4.nearhome.ui.theme.NoticeTone
import com.equipo4.nearhome.ui.theme.adminAccent
import com.equipo4.nearhome.ui.theme.noticeColors
import com.equipo4.nearhome.ui.theme.onAdminAccent

/**
 * Confirmación de Aprobar o Rechazar. Llama al API y espera la respuesta:
 * si sale bien avisa con [onConfirmed]; si falla muestra el error y se queda aquí.
 * [onDeleted] ya no se usa (se conserva para no romper la navegación).
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
    val actor = remember<String?> {
        SessionManager(context).getUserName()
    }?.takeIf { it.isNotBlank() } ?: stringResource(R.string.listing_default_actor)

    val successMessage = when (action) {
        ListingAction.APROBAR -> stringResource(R.string.listing_msg_approved)
        else -> stringResource(R.string.listing_msg_rejected)
    }
    val isValid = isActionFormValid(action, form)
    val danger = noticeColors(NoticeTone.DANGER).content
    val accent = adminAccent()
    val destructive = action == ListingAction.RECHAZAR

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(action.titleRes()),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (destructive) danger else MaterialTheme.colorScheme.onSurface
                    )
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
                    onClick = { viewModel.confirm(actor) { onConfirmed(successMessage) } },
                    enabled = isValid && listing != null && !submitting,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (destructive) danger else accent,
                        contentColor = if (destructive) Color.White else onAdminAccent(),
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
                        Text(stringResource(action.confirmRes()), maxLines = 1)
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
            Header(action)
            ListingSummaryCard(listing)
            StatusChangeRow(from = listing.status, to = action.targetStatus)
            ActionSpecifics(action, form, viewModel)
            error?.let {
                NoticeBanner(tone = NoticeTone.DANGER, text = it, icon = Icons.Filled.Warning)
            }
        }
    }
}

@Composable
private fun Header(action: ListingAction) {
    val (icon, tone) = when (action) {
        ListingAction.APROBAR -> Icons.Filled.CheckCircle to NoticeTone.SUCCESS
        else -> Icons.Filled.Cancel to NoticeTone.DANGER
    }
    val colors = noticeColors(tone)
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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
private fun ActionSpecifics(action: ListingAction, form: ActionForm, vm: ListingActionViewModel) {
    when (action) {
        ListingAction.APROBAR -> InfoCard(verticalSpacing = 4.dp) {
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

        else -> ReasonField(
            value = form.text,
            onValueChange = vm::setText,
            label = stringResource(R.string.listing_reject_label),
            isValid = isActionFormValid(action, form),
            errorText = if (form.text.isBlank()) stringResource(R.string.listing_reject_error_required)
            else stringResource(R.string.listing_reject_error_min, MIN_REJECT_CHARS),
            hint = stringResource(R.string.listing_reject_hint, MIN_REJECT_CHARS)
        )
    }
}

private fun ListingAction.titleRes(): Int =
    if (this == ListingAction.APROBAR) R.string.listing_approve_title else R.string.listing_reject_title

private fun ListingAction.questionRes(): Int =
    if (this == ListingAction.APROBAR) R.string.listing_approve_question else R.string.listing_reject_question

private fun ListingAction.bodyRes(): Int =
    if (this == ListingAction.APROBAR) R.string.listing_approve_body else R.string.listing_reject_body

private fun ListingAction.confirmRes(): Int =
    if (this == ListingAction.APROBAR) R.string.listing_approve_confirm else R.string.listing_reject_confirm