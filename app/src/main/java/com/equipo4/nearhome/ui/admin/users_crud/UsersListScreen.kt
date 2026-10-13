package com.equipo4.nearhome.ui.admin.users_crud

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo4.nearhome.domain.model.UserAccountRole
import com.equipo4.nearhome.domain.model.UserAccountStatus
import com.equipo4.nearhome.ui.common.NearHomeBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersListScreen(
    viewModel: UsersListViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    onUserClick: (String) -> Unit = {},
    onBottomTabSelected: (Int) -> Unit = {}
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedRole by viewModel.selectedRole.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val activeFiltersCount by viewModel.activeFiltersCount.collectAsState()
    val users by viewModel.filteredUsers.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()

    var showRoleMenu by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }

    val isFiltered = searchQuery.isNotBlank() || activeFiltersCount > 0
    val subtitleText = if (isFiltered) {
        val resultsText = if (users.size == 1) "1 resultado" else "${users.size} resultados"
        resultsText
    } else {
        "$totalCount cuentas registradas"
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color.White)) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Usuarios",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937)
                            )
                            Text(
                                text = subtitleText,
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

                // Buscador
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = viewModel::onSearchQueryChanged,
                        placeholder = {
                            Text("Buscar por nombre, correo o teléfono", fontSize = 14.sp, color = Color(0xFF9CA3AF))
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF6B7280))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda", tint = Color(0xFF6B7280))
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0B2C4D),
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Botones / Filtros rápidos (Rol, Estatus, Filtros a la derecha)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Botón Rol
                    Box {
                        FilterChip(
                            selected = selectedRole != null,
                            onClick = { showRoleMenu = true },
                            label = {
                                Text(if (selectedRole != null) selectedRole!!.label else "Rol: Todos")
                            },
                            trailingIcon = {
                                if (selectedRole != null) {
                                    IconButton(
                                        onClick = { viewModel.onRoleSelected(null) },
                                        modifier = Modifier.size(16.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Quitar filtro rol", modifier = Modifier.size(12.dp))
                                    }
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White,
                                selectedContainerColor = Color(0xFFE0F2FE),
                                labelColor = Color(0xFF1F2937),
                                selectedLabelColor = Color(0xFF0369A1)
                            )
                        )
                        DropdownMenu(
                            expanded = showRoleMenu,
                            onDismissRequest = { showRoleMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todos") },
                                onClick = {
                                    viewModel.onRoleSelected(null)
                                    showRoleMenu = false
                                }
                            )
                            UserAccountRole.entries.forEach { role ->
                                DropdownMenuItem(
                                    text = { Text(role.label) },
                                    onClick = {
                                        viewModel.onRoleSelected(role)
                                        showRoleMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // 2. Botón Estatus
                    Box {
                        FilterChip(
                            selected = selectedStatus != null,
                            onClick = { showStatusMenu = true },
                            label = {
                                Text(if (selectedStatus != null) selectedStatus!!.label else "Estatus: Todos")
                            },
                            trailingIcon = {
                                if (selectedStatus != null) {
                                    IconButton(
                                        onClick = { viewModel.onStatusSelected(null) },
                                        modifier = Modifier.size(16.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Quitar filtro estatus", modifier = Modifier.size(12.dp))
                                    }
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White,
                                selectedContainerColor = Color(0xFFDCFCE7),
                                labelColor = Color(0xFF1F2937),
                                selectedLabelColor = Color(0xFF15803D)
                            )
                        )
                        DropdownMenu(
                            expanded = showStatusMenu,
                            onDismissRequest = { showStatusMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todos") },
                                onClick = {
                                    viewModel.onStatusSelected(null)
                                    showStatusMenu = false
                                }
                            )
                            UserAccountStatus.entries.forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status.label) },
                                    onClick = {
                                        viewModel.onStatusSelected(status)
                                        showStatusMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // 3. Botón Filtros Avanzados (a la derecha)
                    FilterChip(
                        selected = activeFiltersCount > 0,
                        onClick = onFilterClick,
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Outlined.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Filtros")
                                if (activeFiltersCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .background(Color(0xFF0B2C4D), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = activeFiltersCount.toString(),
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.White,
                            selectedContainerColor = Color(0xFFE0F2FE),
                            labelColor = Color(0xFF1F2937),
                            selectedLabelColor = Color(0xFF0369A1)
                        )
                    )
                }
                HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)
            }
        },
        bottomBar = { NearHomeBottomBar(selectedTab = 3, onTabSelected = onBottomTabSelected) },
        containerColor = Color(0xFFF9FAFB)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (users.isEmpty()) {
                EmptyUsersState(onClearFilters = viewModel::clearSearchAndFilters)
            } else {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Mostrando ${users.size} usuarios · Más recientes primero",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(users, key = { it.id }) { user ->
                        UserCardItem(
                            user = user,
                            onClick = { onUserClick(user.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyUsersState(onClearFilters: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .background(Color(0xFFE5E7EB).copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Group,
                contentDescription = null,
                tint = Color(0xFF0B2C4D),
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "No encontramos usuarios",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "No hay cuentas que coincidan con la búsqueda y los filtros actuales. Prueba con otro dato o limpia los filtros.",
            fontSize = 14.sp,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            onClick = onClearFilters,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0B2C4D)),
            border = BorderStroke(1.dp, Color(0xFF0B2C4D))
        ) {
            Text(
                text = "Limpiar búsqueda y filtros",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(16.dp))

        Surface(
            color = Color.White,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Si aún no existen usuarios registrados, este espacio mostrará las nuevas cuentas cuando se creen.",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}
