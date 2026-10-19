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
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.equipo4.nearhome.R
import com.equipo4.nearhome.domain.model.AdminListing
import com.equipo4.nearhome.domain.model.ListingAction
import com.equipo4.nearhome.domain.model.StatusChange
import com.equipo4.nearhome.ui.publicacion.detail.PhotoGallery
import com.equipo4.nearhome.ui.theme.NoticeTone
import com.equipo4.nearhome.ui.theme.adminAccent
import com.equipo4.nearhome.ui.theme.listingStatusColors
import com.equipo4.nearhome.ui.theme.noticeColors
import com.equipo4.nearhome.ui.theme.onAdminAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminListingDetailScreen(
    listingId: String,
    message: String? = null,
    onMessageShown: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onActionClick: (ListingAction) -> Unit = {}
) {
    val viewModel: AdminListingDetailViewModel = viewModel(
        key = "listing_detail_$listingId",
        factory = viewModelFactory { initializer { AdminListingDetailViewModel(listingId) } }
    )
    val listing by viewModel.listing.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        if (message != null) {
            snackbar.showSnackbar(message)
            onMessageShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(R.string.listing_detail_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        listing?.let {
                            Text(
                                stringResource(R.string.listing_detail_header_format, it.id, it.status.headerLabel()),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
            listing?.let { l ->
                FixedActionBar { ActionButtons(l, onActionClick) }
            }
        },
        snackbarHost = { ListingSnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background
    ) { inner ->
        val l = listing
        if (l != null) {
            DetailBody(l, Modifier.padding(inner))
        }
    }
}

@Composable
private fun RowScope.ActionButtons(listing: AdminListing, onActionClick: (ListingAction) -> Unit) {
    val danger = noticeColors(NoticeTone.DANGER).content
    val accent = adminAccent()
    ListingAction.availableFor(listing.status).forEach { action ->
        val modifier = Modifier.weight(1f).height(48.dp)
        val label = stringResource(action.shortLabelRes())
        when (action) {
            ListingAction.APROBAR, ListingAction.REACTIVAR -> Button(
                onClick = { onActionClick(action) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = onAdminAccent()),
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = modifier
            ) { Text(label, maxLines = 1) }

            ListingAction.ELIMINAR -> OutlinedButton(
                onClick = { onActionClick(action) },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, danger),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = danger),
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = modifier
            ) { Text(label, maxLines = 1) }

            else -> OutlinedButton(
                onClick = { onActionClick(action) },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, accent),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = accent),
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = modifier
            ) { Text(label, maxLines = 1) }
        }
    }
}

private fun ListingAction.shortLabelRes(): Int = when (this) {
    ListingAction.APROBAR -> R.string.listing_action_approve
    ListingAction.RECHAZAR -> R.string.listing_action_reject
    ListingAction.DESACTIVAR -> R.string.listing_action_deactivate
    ListingAction.REACTIVAR -> R.string.listing_action_reactivate
    ListingAction.ELIMINAR -> R.string.listing_action_delete
}

@Composable
private fun DetailBody(listing: AdminListing, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        // Foto con esquinas redondeadas y margen, como en el Figma.
        Box(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            PhotoGallery(
                images = listing.images,
                counterSeparator = " / ",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
        }

        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Precio, estado, tipo, anunciante y ubicación
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${listing.listingType.label()} ${priceText(listing.price, listing.listingType)}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    ListingStatusTag(listing.status)
                }
                Text(
                    listing.type.label(),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${stringResource(R.string.listing_advertiser)}: ${listing.advertiser.name}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(listing.location, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            FeaturesRow(listing)

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    listing.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(listing.description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            AdminDataCard(listing)
            HistoryCard(listing.history)
        }
    }
}

/** Características en una tarjeta: ícono azul arriba y el dato abajo. */
@Composable
private fun FeaturesRow(listing: AdminListing) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top
        ) {
            Feature(Icons.Filled.SquareFoot, stringResource(R.string.listing_feature_area, listing.areaSqM))
            listing.bedrooms?.let {
                Feature(Icons.Filled.Bed, pluralStringResource(R.plurals.listing_feature_bedrooms, it, it))
            }
            listing.bathrooms?.let {
                Feature(Icons.Filled.Bathtub, pluralStringResource(R.plurals.listing_feature_bathrooms, it, it))
            }
            listing.garages?.let {
                Feature(Icons.Filled.DirectionsCar, pluralStringResource(R.plurals.listing_feature_garages, it, it))
            }
        }
    }
}

@Composable
private fun Feature(icon: ImageVector, text: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = adminAccent(), modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun SectionCard(
    title: String,
    trailing: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (trailing != null) {
                    Text(trailing, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            content()
        }
    }
}

@Composable
private fun AdminDataCard(listing: AdminListing) {
    SectionCard(
        title = stringResource(R.string.listing_admin_data),
        trailing = formatDate(listing.registeredAt)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AvatarInitial(listing.advertiser.name)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    listing.advertiser.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(listing.advertiser.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

    }
}

@Composable
private fun HistoryCard(history: List<StatusChange>) {
    SectionCard(stringResource(R.string.listing_history_title)) {
        // Del más nuevo al más viejo, con un punto de color por estado.
        history.sortedByDescending { it.timestamp }.forEachIndexed { index, change ->
            if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(10.dp)
                        .background(listingStatusColors(change.to).content, CircleShape)
                )
                Spacer(Modifier.width(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (change.from == null) {
                            Text(
                                stringResource(R.string.listing_history_created),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.width(8.dp))
                        } else {
                            ListingStatusTag(change.from)
                            Text(
                                " → ",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        ListingStatusTag(change.to)
                    }
                    Text(
                        "${formatDateTime(change.timestamp)} · ${stringResource(R.string.listing_history_by_format, change.actor)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    change.reason?.let {
                        Text(
                            stringResource(R.string.listing_history_reason_format, it),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}