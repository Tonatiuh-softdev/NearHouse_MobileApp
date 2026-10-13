package com.equipo4.nearhome.ui.admin.users_crud

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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo4.nearhome.R
import com.equipo4.nearhome.domain.model.UserAccountRole
import com.equipo4.nearhome.domain.model.UserAccountStatus

@OptIn(ExperimentalMaterial3Api::class)
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
        "Sin filtros seleccionados"
    } else {
        context.resources.getQuantityString(R.plurals.selected_filters_count, filtersCount, filtersCount)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Filtrar usuarios",
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
                            contentDescription = "Regresar y descartar",
                            tint = Color(0xFF1F2937)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
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
                        onClick = { onApplyFilters(selectedRoles, selectedStatuses) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B2C4D))
                    ) {
                        Text(
                            text = "Aplicar filtros · $liveCount resultados",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = viewModel::clearAllFilters,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0B2C4D)),
                        border = BorderStroke(1.dp, Color(0xFF0B2C4D))
                    ) {
                        Text(
                            text = "Limpiar filtros",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFF9FAFB)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Sección Rol
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rol",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = "Selección múltiple",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }

            // Tarjetas de rol
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(UserAccountRole.COMPRADOR, UserAccountRole.VENDEDOR, UserAccountRole.ADMINISTRADOR).forEach { role ->
                    val isSelected = role in selectedRoles
                    val count = viewModel.getRoleCount(role, allUsers)

                    Surface(
                        modifier = Modifier.clickable { viewModel.toggleRole(role) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0xFFF0F4F8) else Color.White,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) Color(0xFF0B2C4D) else Color(0xFFD1D5DB)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = role.label,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) Color(0xFF0B2C4D) else Color(0xFF1F2937)
                            )
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .background(if (isSelected) Color(0xFF0B2C4D) else Color(0xFFE5E7EB), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = count.toString(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF374151)
                                )
                            }
                        }
                    }
                }
            }

            // Sección Estatus
            Text(
                text = "Estatus",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            // Casillas de estatus
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    UserAccountStatus.REGISTRADO,
                    UserAccountStatus.PENDIENTE_VERIFICACION,
                    UserAccountStatus.VERIFICADO_ACTIVO,
                    UserAccountStatus.BLOQUEADO
                ).forEach { status ->
                    val isSelected = status in selectedStatuses
                    val count = viewModel.getStatusCount(status, allUsers)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleStatus(status) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFF0F4F8) else Color.White
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) Color(0xFF0B2C4D) else Color(0xFFE5E7EB)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { viewModel.toggleStatus(status) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF0B2C4D),
                                    uncheckedColor = Color(0xFF9CA3AF)
                                )
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = status.label,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1F2937),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = count.toString(),
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Nota resumen informativa
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
                    val summaryText = buildString {
                        append("Se mostrarán ")
                        if (selectedRoles.isEmpty()) {
                            append("usuarios de cualquier rol")
                        } else {
                            append(selectedRoles.joinToString(" y ") { it.label.lowercase() })
                        }
                        if (selectedStatuses.isNotEmpty()) {
                            append(" con estatus ")
                            append(selectedStatuses.joinToString(" o ") { it.label.lowercase() })
                        }
                        append(".")
                    }
                    Text(
                        text = summaryText,
                        fontSize = 13.sp,
                        color = Color(0xFF4B5563)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
