package com.equipo4.nearhome.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo4.nearhome.ui.common.NearHomeBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: AdminPanelViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToUsers: () -> Unit = {},
    onNavigateToAds: () -> Unit = {},
    onBottomTabSelected: (Int) -> Unit = {}
) {
    val pendingCount by viewModel.pendingCount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Panel de administración",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
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
        },
        bottomBar = { NearHomeBottomBar(selectedTab = 3, onTabSelected = onBottomTabSelected) },
        containerColor = Color(0xFFF9FAFB)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Text(
                text = "Gestión de NearHouse",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Revisa la actividad de la comunidad y gestiona el contenido de la plataforma.",
                fontSize = 13.sp,
                color = Color(0xFF6B7280)
            )

            Spacer(Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    AdminOptionRow(
                        icon = Icons.Outlined.Assignment,
                        title = "Reportes",
                        subtitle = "Pendientes de revisión",
                        badgeCount = pendingCount,
                        showDivider = true,
                        onClick = onNavigateToReports
                    )
                    AdminOptionRow(
                        icon = Icons.Outlined.People,
                        title = "Usuarios",
                        subtitle = "Consulta y gestiona perfiles",
                        badgeCount = 0,
                        showDivider = true,
                        onClick = onNavigateToUsers
                    )
                    AdminOptionRow(
                        icon = Icons.Outlined.Home,
                        title = "Anuncios",
                        subtitle = "Administra todos los anuncios publicados",
                        badgeCount = 0,
                        showDivider = false,
                        onClick = onNavigateToAds
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminOptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badgeCount: Int,
    showDivider: Boolean,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFFF3F4F6), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF0B2C4D),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            }

            if (badgeCount > 0) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color(0xFFEF4444), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeCount.toString(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(20.dp)
            )
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color(0xFFE5E7EB)
            )
        }
    }
}
