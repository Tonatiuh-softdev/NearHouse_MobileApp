package com.equipo4.nearhome.ui.profile.publicprofile

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.equipo4.nearhome.R
import com.equipo4.nearhome.domain.model.UserAccountRole
import com.equipo4.nearhome.ui.admin.users_crud.AdminImageLoader
import com.equipo4.nearhome.ui.admin.users_crud.UserRoleTag
import com.equipo4.nearhome.ui.admin.users_crud.UserStatusBadge
import com.equipo4.nearhome.ui.common.MyListingsHeaderSkeleton
import com.equipo4.nearhome.ui.common.PropertyCard
import com.equipo4.nearhome.ui.common.PropertyListSkeleton

/**
 * Perfil de otro usuario. Es la misma pantalla que "Mis anuncios", pero de solo lectura:
 * - sin el botón "Crear anuncio",
 * - en vez del lápiz de editar perfil va la bandera para reportar al usuario
 *   (no sale en tu propio perfil ni en el del superadministrador),
 * - sin etiquetas de estado y solo con anuncios publicados y activos.
 * Un administrador ve además los datos de la cuenta y puede ir a administrarla.
 */
@Composable
fun UserProfileScreen(
    userId: Int,
    onBackClick: () -> Unit,
    onPropertyClick: (String) -> Unit,
    onManageAccountClick: () -> Unit,
    onReportClick: () -> Unit = {}
) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: UserProfileViewModel = viewModel(
        key = "user_profile_$userId",
        factory = UserProfileViewModel.Factory(application, userId)
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (uiState.isLoading) {
                item { MyListingsHeaderSkeleton() }
                item { PropertyListSkeleton(count = 3) }
                return@LazyColumn
            }

            item {
                ProfileHeader(
                    uiState = uiState,
                    onBackClick = onBackClick,
                    onReportClick = onReportClick,
                    onManageAccountClick = onManageAccountClick
                )
            }

            if (uiState.listings.isEmpty()) {
                item { EmptyListings() }
            } else {
                // Igual que en "Mis anuncios", pero sin etiqueta de estado ni menú
                items(items = uiState.listings, key = { it.id }) { property ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        PropertyCard(
                            property = property,
                            onClick = { onPropertyClick(property.id) },
                            onToggleSave = { viewModel.toggleSaved(property) },
                            showSaveButton = !property.isOwner
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    uiState: UserProfileUiState,
    onBackClick: () -> Unit,
    onReportClick: () -> Unit,
    onManageAccountClick: () -> Unit
) {
    val account = uiState.account

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Barra superior: regresar y reportar (en "Mis anuncios" aquí va el lápiz)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            // El superadministrador no se puede reportar (el API también lo rechaza)
            if (!uiState.isOwnProfile && account?.role != UserAccountRole.SUPER_ADMIN) {
                IconButton(onClick = onReportClick) {
                    Icon(
                        imageVector = Icons.Outlined.Flag,
                        contentDescription = stringResource(R.string.user_profile_report_cd),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Avatar: foto de perfil o ícono por defecto (mismo estilo que "Mis anuncios")
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(50.dp)
            )
            if (uiState.photoUrl != null) {
                // El loader manda el token: sirve para la foto pública y para la del panel de admin.
                // Si no carga, se queda el ícono de abajo.
                AsyncImage(
                    model = uiState.photoUrl,
                    imageLoader = AdminImageLoader.get(LocalContext.current),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = uiState.name ?: stringResource(R.string.profile_default_name),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        uiState.email?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(6.dp))
            InfoRow(icon = Icons.Outlined.Email, text = it)
        }

        uiState.phone?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(4.dp))
            InfoRow(icon = Icons.Outlined.Phone, text = it)
        }

        // Datos de la cuenta: solo los ve un administrador
        if (account != null) {
            Spacer(Modifier.height(4.dp))
            InfoRow(
                icon = Icons.Outlined.CalendarMonth,
                text = stringResource(R.string.user_profile_member_since, account.registrationDate)
            )
            Spacer(Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserRoleTag(account.role)
                UserStatusBadge(account.status)
            }
        }

        if (uiState.viewerIsAdmin) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onManageAccountClick,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .widthIn(min = 180.dp)
                    .height(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ManageAccounts,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.user_profile_manage_account),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyListings() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.user_profile_empty),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}
