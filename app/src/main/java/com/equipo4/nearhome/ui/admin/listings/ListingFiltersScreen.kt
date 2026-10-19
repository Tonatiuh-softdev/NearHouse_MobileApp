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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo4.nearhome.R
import com.equipo4.nearhome.domain.model.ListingStatus
import com.equipo4.nearhome.domain.model.ListingType
import com.equipo4.nearhome.domain.model.PropertyType
import com.equipo4.nearhome.ui.theme.NoticeTone
import com.equipo4.nearhome.ui.theme.adminAccent
import com.equipo4.nearhome.ui.theme.onAdminAccent

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ListingFiltersScreen(
    viewModel: AdminListingsViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onApplied: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    // Borrador: no se aplica hasta tocar "Aplicar". La flecha de regreso lo descarta.
    var draft by remember { mutableStateOf(state.filters) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(R.string.filters_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            pluralStringResource(R.plurals.listings_filters_selected, draft.selectedCount, draft.selectedCount),
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
                OutlinedButton(
                    onClick = { draft = ListingFilters(statuses = emptySet()) },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, adminAccent()),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = adminAccent()),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) { Text(stringResource(R.string.filters_clear)) }
                Button(
                    onClick = {
                        viewModel.applyFilters(draft)
                        onApplied()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = adminAccent(), contentColor = onAdminAccent()),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) { Text(stringResource(R.string.filters_apply)) }
            }
        },
        modifier = Modifier.imePadding(),
        containerColor = MaterialTheme.colorScheme.background
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            FilterGroup(stringResource(R.string.filters_status)) {
                ListingStatus.entries.forEach { s ->
                    CountChip(
                        label = s.label(),
                        count = state.statusCounts[s] ?: 0,
                        selected = s in draft.statuses,
                        onClick = { draft = draft.copy(statuses = draft.statuses.toggle(s)) }
                    )
                }
            }
            FilterGroup(stringResource(R.string.filters_mode)) {
                ListingType.entries.forEach { m ->
                    CountChip(
                        label = m.label(),
                        count = state.modalityCounts[m] ?: 0,
                        selected = m in draft.modalities,
                        onClick = { draft = draft.copy(modalities = draft.modalities.toggle(m)) }
                    )
                }
            }
            FilterGroup(stringResource(R.string.filters_type)) {
                PropertyType.entries.forEach { t ->
                    CountChip(
                        label = t.label(),
                        count = state.typeCounts[t] ?: 0,
                        selected = t in draft.types,
                        onClick = { draft = draft.copy(types = draft.types.toggle(t)) }
                    )
                }
            }
            // Orden: solo se puede elegir uno y no cuenta como filtro.
            Column {
                SectionTitle(stringResource(R.string.filters_order))
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SortOrder.entries.forEach { o ->
                        OrderRow(
                            text = o.label(),
                            selected = draft.order == o,
                            onClick = { draft = draft.copy(order = o) }
                        )
                    }
                }
            }
            NoticeBanner(tone = NoticeTone.INFO, text = filterSummaryNote(draft))
        }
    }
}

private fun <T> Set<T>.toggle(item: T): Set<T> = if (item in this) this - item else this + item

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterGroup(title: String, content: @Composable FlowRowScope.() -> Unit) {
    Column {
        SectionTitle(title)
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

/** Chip con el nombre y la cantidad en un círculo. Seleccionado: azul claro con borde azul. */
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

/** Fila de "Orden por fecha": seleccionada con check azul y fondo azul claro. */
@Composable
private fun OrderRow(text: String, selected: Boolean, onClick: () -> Unit) {
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
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}