package com.equipo4.nearhome.ui.profile.seller

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.equipo4.nearhome.ui.admin.listings.NoticeBanner
import com.equipo4.nearhome.ui.common.DangerSnackbarHost
import com.equipo4.nearhome.ui.common.DeleteListingDialog
import com.equipo4.nearhome.ui.common.MyListingsHeaderSkeleton
import com.equipo4.nearhome.ui.common.NearHomeBottomBar
import com.equipo4.nearhome.ui.common.NoConnectionState
import com.equipo4.nearhome.ui.common.PropertyCard
import com.equipo4.nearhome.ui.common.PropertyListSkeleton
import com.equipo4.nearhome.ui.theme.NoticeTone

@Composable
fun MyListingsScreen(
    viewModel: MyListingsViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onCreateListingClick: () -> Unit = {},
    onEditListingClick: (String) -> Unit = {},
    onPropertyClick: (String) -> Unit = {},
    /** Sin plan vigente: Editar o Reactivar mandan a la pantalla de planes. */
    onPlanRequired: () -> Unit = {},
    onBottomTabSelected: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    // Anuncio seleccionado para el panel de gestión
    var selected by remember { mutableStateOf<MyListingItem?>(null) }

    // Cada vez que se muestra la pantalla se piden los anuncios, para ver su estado actual
    LaunchedEffect(Unit) { viewModel.loadMyListings() }

    // Aviso de anuncio eliminado (o del error)
    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            viewModel.messageShown()
        }
    }

    selected?.let { item ->
        ManageListingSheet(
            item = item,
            puedePublicar = uiState.puedePublicar,
            onDismiss = { selected = null },
            onEdit = {
                selected = null
                onEditListingClick(item.property.id)
            },
            onToggleActive = {
                selected = null
                viewModel.setListingActive(item.property.id, activa = !item.isActive)
            },
            onPlanRequired = {
                selected = null
                onPlanRequired()
            },
            onDelete = {
                selected = null
                viewModel.deleteListing(item.property.id)
            }
        )
    }

    Scaffold(
        bottomBar = { NearHomeBottomBar(selectedTab = 3, onTabSelected = onBottomTabSelected) },
        snackbarHost = { DangerSnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (uiState.isLoading && uiState.listings.isEmpty()) {
                item {
                    MyListingsHeaderSkeleton()
                }
                item {
                    PropertyListSkeleton(count = 3)
                }
            } else {
                item {
                    HeaderSection(
                        uiState = uiState,
                        onBackClick = onBackClick,
                        onEditProfileClick = onEditProfileClick,
                        onCreateListingClick = onCreateListingClick
                    )
                }

                // Comprador que antes fue vendedor: sus anuncios se ven, pero no están publicados
                if (!uiState.puedePublicar && uiState.listings.isNotEmpty()) {
                    item {
                        NoticeBanner(
                            tone = NoticeTone.WARNING,
                            text = stringResource(R.string.my_listings_hidden_notice),
                            icon = Icons.Filled.Info,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                }

                when {
                    uiState.loadError && uiState.listings.isEmpty() -> item {
                        NoConnectionState(onRetry = viewModel::loadMyListings, isRetrying = uiState.isRetrying)
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

        Text(
            text = uiState.userName.ifBlank { stringResource(R.string.profile_default_name) },
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(6.dp))

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

@Composable
private fun MyListingCard(
    item: MyListingItem,
    onClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val paused = !item.isActive
    val ocultoPorPlan = item.ocultaPorLimite && !paused

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .then(if (paused || ocultoPorPlan) Modifier.alpha(0.55f) else Modifier)
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
                    when {
                        item.desactivadaPorAdmin -> AdminDeactivatedChip()
                        paused -> PausedChip()
                        ocultoPorPlan -> HiddenByPlanChip()
                        else -> StatusChip(status = item.status)
                    }
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

        if (ocultoPorPlan) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.listing_hidden_by_plan_notice),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(10.dp)
            )
        }

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

@Composable
private fun PausedChip(modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFF6B7280),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.status_paused),
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/** Publicado, pero no se muestra en Inicio porque excede el límite del plan. */
@Composable
private fun HiddenByPlanChip(modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFFB45309),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.status_hidden_by_plan),
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun AdminDeactivatedChip(modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFFDC2626),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.status_deactivated_by_admin),
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManageListingSheet(
    item: MyListingItem,
    puedePublicar: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit,
    onPlanRequired: () -> Unit,
    onDelete: () -> Unit
) {
    var confirmDelete by remember { mutableStateOf(false) }
    val danger = Color(0xFFDC2626)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val canToggleActive = item.status == ListingStatus.PUBLISHED

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
                onClick = { if (puedePublicar) onEdit() else onPlanRequired() },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(stringResource(R.string.manage_listing_edit))
            }

            if (item.desactivadaPorAdmin) {
                // Lo desactivó un administrador: el vendedor solo puede ver el motivo
                Text(
                    text = stringResource(R.string.listing_deactivated_by_admin_notice, item.motivoDesactivacion.orEmpty()),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(10.dp)
                )
            } else {
                OutlinedButton(
                    onClick = {
                        // Pausar siempre se puede; reactivar necesita plan vigente
                        if (!item.isActive && !puedePublicar) onPlanRequired() else onToggleActive()
                    },
                    enabled = canToggleActive,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(
                        stringResource(
                            if (item.isActive) R.string.manage_listing_pause
                            else R.string.manage_listing_resume
                        )
                    )
                }
            }

            Button(
                onClick = { confirmDelete = true },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = danger,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(stringResource(R.string.manage_listing_delete))
            }
        }
    }

    if (confirmDelete) {
        DeleteListingDialog(
            onDismiss = { confirmDelete = false },
            onConfirm = {
                confirmDelete = false
                onDelete()
            }
        )
    }
}