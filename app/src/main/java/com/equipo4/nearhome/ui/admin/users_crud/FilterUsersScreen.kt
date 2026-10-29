package com.equipo4.nearhome.ui.admin.users_crud

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo4.nearhome.R
import com.equipo4.nearhome.domain.model.UserAccountRole
import com.equipo4.nearhome.domain.model.UserAccountStatus
import com.equipo4.nearhome.ui.admin.listings.FixedActionBar
import com.equipo4.nearhome.ui.admin.listings.NoticeBanner
import com.equipo4.nearhome.ui.theme.NoticeTone
import com.equipo4.nearhome.ui.theme.adminAccent
import com.equipo4.nearhome.ui.theme.onAdminAccent

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterUsersScreen(
    viewModel: FilterUsersViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onApplyFilters: (Set<UserAccountRole>, Set<UserAccountStatus>) -> Unit = { _, _ -> }
) {
    val selectedRoles by viewModel.selectedRoles.collectAsState()
    val selectedStatuses by viewModel.selectedStatuses.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val filtersCount by viewModel.selectedFiltersCount.collectAsState()
    val liveCount by viewModel.liveFilteredCount.collectAsState()

    val context = LocalContext.current
    val subtitleText = if (filtersCount == 0) {
        stringResource(R.string.filter_users_subtitle_none)
    } else {
        context.resources.getQuantityString(R.plurals.selected_filters_count, filtersCount, filtersCount)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(R.string.filter_users_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            subtitleText,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onApplyFilters(selectedRoles, selectedStatuses) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = adminAccent(), contentColor = onAdminAccent()),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) { Text(stringResource(R.string.filter_users_apply_format, liveCount)) }
                    OutlinedButton(
                        onClick = viewModel::clearAllFilters,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, adminAccent()),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = adminAccent()),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) { Text(stringResource(R.string.filter_users_clear)) }
                }
            }
        },
        modifier = Modifier.imePadding(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitle(stringResource(R.string.filter_users_role_title))
                    Text(
                        stringResource(R.string.filter_users_multiple_selection),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        UserAccountRole.COMPRADOR,
                        UserAccountRole.VENDEDOR,
                        UserAccountRole.ADMINISTRADOR
                    ).forEach { role ->
                        CountChip(
                            label = role.label,
                            count = viewModel.getRoleCount(role, allUsers),
                            selected = role in selectedRoles,
                            onClick = { viewModel.toggleRole(role) }
                        )
                    }
                }
            }

            Column {
                SectionTitle(stringResource(R.string.filter_users_status_title))
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        UserAccountStatus.REGISTRADO,
                        UserAccountStatus.PENDIENTE_VERIFICACION,
                        UserAccountStatus.VERIFICADO_ACTIVO,
                        UserAccountStatus.BLOQUEADO
                    ).forEach { status ->
                        CheckCountRow(
                            text = status.label,
                            count = viewModel.getStatusCount(status, allUsers),
                            selected = status in selectedStatuses,
                            onClick = { viewModel.toggleStatus(status) }
                        )
                    }
                }
            }

            val wordAnd = stringResource(R.string.listing_word_and)
            val wordOr = stringResource(R.string.listing_word_or)
            val summaryPrefix = stringResource(R.string.filter_users_summary_prefix)
            val summaryAnyRole = stringResource(R.string.filter_users_summary_any_role)
            val summaryWithStatus = stringResource(R.string.filter_users_summary_with_status)
            val summaryText = buildString {
                append(summaryPrefix)
                if (selectedRoles.isEmpty()) {
                    append(summaryAnyRole)
                } else {
                    append(selectedRoles.joinToString(wordAnd) { it.label.lowercase() })
                }
                if (selectedStatuses.isNotEmpty()) {
                    append(summaryWithStatus)
                    append(selectedStatuses.joinToString(wordOr) { it.label.lowercase() })
                }
                append(".")
            }
            NoticeBanner(tone = NoticeTone.INFO, text = summaryText)
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun CountChip(label: String, count: Int, selected: Boolean, onClick: () -> Unit) {
    val accent = adminAccent()
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (selected) accent else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
                    .background(
                        if (selected) accent else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    )
                    .padding(horizontal = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    count.toString(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (selected) onAdminAccent() else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CheckCountRow(text: String, count: Int, selected: Boolean, onClick: () -> Unit) {
    val accent = adminAccent()
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (selected) accent else MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selected) {
                Box(
                    modifier = Modifier.size(22.dp).background(accent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = onAdminAccent(),
                        modifier = Modifier.size(14.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                        .padding(1.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(5.dp))
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                count.toString(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}