package com.equipo4.nearhome.ui.profile.seller

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.equipo4.nearhome.R
import com.equipo4.nearhome.ui.common.NearHomeBottomBar
import com.equipo4.nearhome.ui.common.PropertyCard
import com.equipo4.nearhome.ui.profile.UserRole
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.MoreVert

@Composable
fun MyListingsScreen(
    viewModel: MyListingsViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onCreateListingClick: () -> Unit = {},
    onPropertyClick: (String) -> Unit = {},
    onBottomTabSelected: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Anuncio seleccionado para el panel de gestión
    var selected by remember { mutableStateOf<MyListingItem?>(null) }

    // Cada vez que se muestra la pantalla se piden los anuncios, para ver su estado actual
    LaunchedEffect(Unit) { viewModel.loadMyListings() }

    // Regla de control de acceso: Si el usuario en sesión es COMPRADOR, regresa a Cuenta.
    LaunchedEffect(uiState.role) {
        if (uiState.role == UserRole.COMPRADOR) {
            onBackClick()
        }
    }

    selected?.let { item ->
        ManageListingSheet(item = item, onDismiss = { selected = null })
    }

    Scaffold(
        bottomBar = { NearHomeBottomBar(selectedTab = 3, onTabSelected = onBottomTabSelected) },
        containerColor = MaterialTheme.colorScheme.background // Adaptable a modo oscuro/claro
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Sección Encabezado
            item {
                HeaderSection(
                    uiState = uiState,
                    onBackClick = onBackClick,
                    onEditProfileClick = onEditProfileClick,
                    onCreateListingClick = onCreateListingClick
                )
            }

            // Sección Lista, carga, error o Estado Vacío
            when {
                uiState.isLoading && uiState.listings.isEmpty() -> item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }

                uiState.loadError && uiState.listings.isEmpty() -> item {
                    LoadErrorMessage(onRetry = viewModel::loadMyListings)
                }

                uiState.listings.isEmpty() -> item { EmptyListingsMessage() }

                else -> items(
                    items = uiState.listings,
                    key = { it.property.id }
                ) { item ->
                    MyListingCard(
                        item = item,
                        onClick = { onPropertyClick(item.property.id) },
                        onMenuClick = { selected = item }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(
    uiState: MyListingsUiState,
    onBackClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onCreateListingClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Barra Superior
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
            IconButton(onClick = onEditProfileClick) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = stringResource(R.string.cd_edit_profile),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Avatar: foto de perfil o ícono por defecto
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.userPhotoUrl != null) {
                AsyncImage(
                    model = uiState.userPhotoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(50.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Nombre completo
        Text(
            text = uiState.userName.ifBlank { stringResource(R.string.profile_default_name) },
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(6.dp))

        // Correo electrónico
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Email,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = uiState.userEmail,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(4.dp))

        // Teléfono
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Phone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = uiState.userPhone,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(16.dp))

        // Botón "Crear anuncio +"
        Button(
            onClick = onCreateListingClick,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .width(180.dp)
                .height(42.dp)
        ) {
            Text(
                text = stringResource(R.string.action_create_listing),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

/** Tarjeta de un anuncio del vendedor: la misma del home, con su estado, el menú de tres puntos y, si lo rechazaron, el motivo. */
@Composable
private fun MyListingCard(item: MyListingItem, onClick: () -> Unit, onMenuClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        PropertyCard(
            property = item.property,
            onClick = onClick,
            showSaveButton = false,
            badge = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusChip(status = item.status)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.45f))
                            .clickable { onMenuClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.manage_listing_options_cd),
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        )

        if (item.status == ListingStatus.REJECTED && !item.rejectionReason.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.listing_history_reason_format, item.rejectionReason),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(10.dp)
            )
        }
    }
}

/** Panel de gestión. Por ahora los botones no hacen nada: falta conectar los endpoints. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManageListingSheet(
    item: MyListingItem,
    onDismiss: () -> Unit
) {
    var confirmDelete by remember { mutableStateOf(false) }
    val danger = Color(0xFFDC2626)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.manage_listing_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            PropertyCard(
                property = item.property,
                onClick = {},
                showSaveButton = false,
                badge = { StatusChip(status = item.status) }
            )
            Button(
                onClick = { /* pendiente: editar */ },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) { Text(stringResource(R.string.manage_listing_edit)) }

            OutlinedButton(
                onClick = { /* pendiente: pausar o reactivar */ },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(
                    stringResource(
                        if (item.isActive) R.string.manage_listing_pause else R.string.manage_listing_resume
                    )
                )
            }
            Button(
                onClick = { confirmDelete = true },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = danger,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) { Text(stringResource(R.string.manage_listing_delete)) }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.manage_listing_delete_question)) },
            text = { Text(stringResource(R.string.manage_listing_delete_warning)) },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false /* pendiente: eliminar */ }) {
                    Text(stringResource(R.string.manage_listing_confirm), color = danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(stringResource(R.string.listing_action_cancel))
                }
            }
        )
    }
}

@Composable
private fun StatusChip(status: ListingStatus, modifier: Modifier = Modifier) {
    Surface(
        color = Color(status.colorHex),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = status.labelRes),
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun LoadErrorMessage(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.my_listings_load_error),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        OutlinedButton(onClick = onRetry, shape = RoundedCornerShape(10.dp)) {
            Text(stringResource(R.string.create_listing_retry))
        }
    }
}

@Composable
private fun EmptyListingsMessage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.empty_listings_message),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}