package com.equipo4.nearhome.ui.admin.users_crud

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo4.nearhome.R
import com.equipo4.nearhome.domain.model.UserAccountRole
import com.equipo4.nearhome.domain.model.UserAccountStatus
import com.equipo4.nearhome.ui.common.NearHomeBottomBar
import com.equipo4.nearhome.ui.theme.nearHouseColors

/** Cuántos elementos antes del final se pide la siguiente página. */
private const val UMBRAL_CARGA = 4

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersListScreen(
    viewModel: UsersListViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onUserClick: (String) -> Unit = {},
    onBottomTabSelected: (Int) -> Unit = {}
) {
    val query by viewModel.searchQuery.collectAsState()
    val role by viewModel.selectedRole.collectAsState()
    val status by viewModel.selectedStatus.collectAsState()
    val state by viewModel.uiState.collectAsState()

    var showRoleMenu by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val isFiltered = query.isNotBlank() || role != null || status != null
    val subtitle = if (isFiltered) {
        stringResource(R.string.admin_users_results, state.totalResults)
    } else {
        stringResource(R.string.admin_users_total_accounts, state.totalAccounts)
    }

    // Pide la siguiente página cuando el último elemento visible está cerca del final
    val cercaDelFinal by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val ultimoVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            info.totalItemsCount > 0 && ultimoVisible >= info.totalItemsCount - UMBRAL_CARGA
        }
    }
    LaunchedEffect(cercaDelFinal, state.users.size) {
        if (cercaDelFinal) viewModel.cargarMas()
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = stringResource(R.string.admin_users_title),
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

                // Buscador
                OutlinedTextField(
                    value = query,
                    onValueChange = viewModel::onSearchQueryChanged,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.admin_users_search_hint),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    trailingIcon = {
                        if (query.isNotBlank()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.admin_users_clear_search),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Filtros rápidos
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box {
                        FilterChip(
                            selected = role != null,
                            onClick = { showRoleMenu = true },
                            label = { Text(role?.label ?: stringResource(R.string.admin_users_role_all)) },
                            shape = RoundedCornerShape(10.dp),
                            colors = chipColors()
                        )
                        DropdownMenu(
                            expanded = showRoleMenu,
                            onDismissRequest = { showRoleMenu = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.admin_users_all), color = MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    viewModel.onRoleSelected(null)
                                    showRoleMenu = false
                                }
                            )
                            UserAccountRole.entries.forEach { r ->
                                DropdownMenuItem(
                                    text = { Text(r.label, color = MaterialTheme.colorScheme.onSurface) },
                                    onClick = {
                                        viewModel.onRoleSelected(r)
                                        showRoleMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Box {
                        FilterChip(
                            selected = status != null,
                            onClick = { showStatusMenu = true },
                            label = { Text(status?.label ?: stringResource(R.string.admin_users_status_all)) },
                            shape = RoundedCornerShape(10.dp),
                            colors = chipColors()
                        )
                        DropdownMenu(
                            expanded = showStatusMenu,
                            onDismissRequest = { showStatusMenu = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.admin_users_all), color = MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    viewModel.onStatusSelected(null)
                                    showStatusMenu = false
                                }
                            )
                            UserAccountStatus.entries.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s.label, color = MaterialTheme.colorScheme.onSurface) },
                                    onClick = {
                                        viewModel.onStatusSelected(s)
                                        showStatusMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Sin filtros extra por ahora
                    FilterChip(
                        selected = false,
                        onClick = { },
                        label = { Text(stringResource(R.string.admin_users_filters)) },
                        shape = RoundedCornerShape(10.dp),
                        colors = chipColors()
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
            }
        },
        bottomBar = { NearHomeBottomBar(selectedTab = 3, onTabSelected = onBottomTabSelected) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                state.isLoading && state.users.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                state.errorMessage != null && state.users.isEmpty() -> {
                    ErrorUsersState(mensaje = state.errorMessage.orEmpty(), onRetry = viewModel::reintentar)
                }

                state.users.isEmpty() -> {
                    EmptyUsersState(onClearFilters = viewModel::clearSearchAndFilters)
                }

                else -> {
                    Text(
                        text = stringResource(R.string.admin_users_showing, state.totalResults),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.users, key = { it.id }) { user ->
                            UserCardItem(user = user, onClick = { onUserClick(user.id) })
                        }
                        if (state.isLoadingMore) {
                            item(key = "cargando_mas") {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun chipColors() = FilterChipDefaults.filterChipColors(
    containerColor = MaterialTheme.colorScheme.surface,
    selectedContainerColor = MaterialTheme.nearHouseColors.filterChipSelectedBg,
    labelColor = MaterialTheme.colorScheme.onSurface,
    selectedLabelColor = MaterialTheme.colorScheme.primary
)

@Composable
private fun ErrorUsersState(mensaje: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.admin_users_error),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        if (mensaje.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = mensaje,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onRetry, shape = RoundedCornerShape(12.dp)) {
            Text(stringResource(R.string.admin_users_retry))
        }
    }
}

@Composable
private fun EmptyUsersState(onClearFilters: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Group,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.admin_users_empty_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.admin_users_empty_body),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        OutlinedButton(
            onClick = onClearFilters,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(stringResource(R.string.admin_users_clear_filters), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}