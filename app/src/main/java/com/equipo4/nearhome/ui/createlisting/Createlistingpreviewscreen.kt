package com.equipo4.nearhome.ui.createlisting

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.equipo4.nearhome.R
import com.equipo4.nearhome.domain.model.PropertyType
import com.equipo4.nearhome.ui.common.NearHomeDialog
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateListingPreviewScreen(
    viewModel: CreateListingViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    /** Sin plan vigente al enviar: se manda a la pantalla de planes conservando lo capturado. */
    onPlanRequired: () -> Unit = {},
    onFinished: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme

    // El ViewModel avisa cuando el API responde PLAN_REQUERIDO
    LaunchedEffect(Unit) {
        viewModel.planRequired.collect { onPlanRequired() }
    }

    val opLabel = stringResource(
        if (state.isRent) R.string.create_listing_op_rent else R.string.create_listing_op_buy
    )
    val typeLabel = stringResource(propertyTypeRes(state.propertyType))
    val perMonth = stringResource(R.string.create_listing_per_month)
    val bedroomsLabel = stringResource(R.string.create_listing_bedrooms)
    val bathroomsLabel = stringResource(R.string.create_listing_bathrooms)
    val parkingLabel = stringResource(R.string.create_listing_parking)

    val chips = buildList {
        add(typeLabel)
        when (state.propertyType) {
            PropertyType.DEPARTAMENTO, PropertyType.CASA -> {
                state.area.toIntOrNull()?.let { add("$it m²") }
                state.bedrooms?.let { add("$it $bedroomsLabel") }
                state.bathrooms?.let { add("$it $bathroomsLabel") }
                state.parking?.let { add("$it $parkingLabel") }
            }
            PropertyType.TERRENO -> add("${state.area} m²")
            null -> Unit
        }
    }

    // Portada: URL del servidor (editar) o archivo local (crear / fotos nuevas)
    val coverModel: Any? = state.existingPhotos.firstOrNull()?.url
        ?: state.photos.firstOrNull()?.let { File(it) }

    Scaffold(
        containerColor = cs.surface,
        topBar = {
            Column(Modifier.background(cs.surface)) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.create_listing_preview_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                stringResource(R.string.common_back)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = cs.surface
                    )
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(cs.primaryContainer)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Visibility,
                        null,
                        tint = cs.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        stringResource(R.string.create_listing_preview_banner),
                        style = MaterialTheme.typography.labelLarge,
                        color = cs.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        bottomBar = {
            Column(
                Modifier
                    .background(cs.surface)
                    .navigationBarsPadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.submit(context) },
                    enabled = !state.submitting && !state.submitted,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(
                        stringResource(
                            when {
                                state.submitting -> R.string.create_listing_sending
                                state.isEditing -> R.string.edit_listing_save
                                else -> R.string.create_listing_confirm_publish
                            }
                        ),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                OutlinedButton(
                    onClick = onEdit,
                    enabled = !state.submitting && !state.submitted,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(stringResource(R.string.create_listing_back_to_edit))
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.create_listing_preview_label).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.onSurfaceVariant
                )
                Text(
                    stringResource(
                        if (state.isEditing) R.string.create_listing_preview_label
                        else R.string.create_listing_preview_new
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.onPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(cs.primary)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, cs.outlineVariant, RoundedCornerShape(16.dp))
            ) {
                Box {
                    coverModel?.let { model ->
                        AsyncImage(
                            model = model,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(180.dp)
                        )
                    }
                    Text(
                        "1 / ${state.totalPhotoCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.surface,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(cs.scrim.copy(alpha = 0.6f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Column(
                    Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "MX " + formatPrice(state.price) + if (state.isRent) " $perMonth" else "",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            opLabel.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = cs.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(cs.primaryContainer)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        state.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            null,
                            tint = cs.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            state.fullAddress,
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        chips.forEach { chip ->
                            Text(
                                chip,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(cs.surfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            state.submitError?.let { message ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(cs.errorContainer)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        null,
                        tint = cs.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        message,
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onErrorContainer
                    )
                }
            }

            Text(
                stringResource(R.string.create_listing_preview_note),
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(cs.surfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp)
            )
        }
    }

    if (state.submitted) {
        NearHomeDialog(
            title = stringResource(
                if (state.isEditing) R.string.edit_listing_sent_title
                else R.string.create_listing_sent_title
            ),
            message = stringResource(
                if (state.isEditing) R.string.edit_listing_sent_body
                else R.string.create_listing_sent_body
            ),
            confirmText = stringResource(R.string.create_listing_sent_ok),
            onConfirm = onFinished,
            onDismiss = {}
        )
    }
}