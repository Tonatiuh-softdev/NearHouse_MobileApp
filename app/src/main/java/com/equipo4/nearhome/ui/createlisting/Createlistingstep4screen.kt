package com.equipo4.nearhome.ui.createlisting

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.equipo4.nearhome.R
import com.equipo4.nearhome.domain.model.PropertyType
import com.equipo4.nearhome.ui.common.NearHomeBottomBar
import com.equipo4.nearhome.ui.theme.NoticeTone
import com.equipo4.nearhome.ui.theme.noticeColors
import java.io.File
import java.text.NumberFormat
import java.util.Locale

@StringRes
internal fun propertyTypeRes(type: PropertyType?): Int = when (type) {
    PropertyType.DEPARTAMENTO -> R.string.listing_type_apartment
    PropertyType.CASA -> R.string.listing_type_house
    else -> R.string.listing_type_land
}

internal fun formatPrice(digits: String): String {
    val n = digits.toLongOrNull() ?: return "\$0"
    return "\$" + NumberFormat.getIntegerInstance(Locale.US).format(n)
}

@Composable
fun CreateListingStep4Screen(
    viewModel: CreateListingViewModel,
    onBack: () -> Unit,
    onEditData: () -> Unit,
    onEditContent: () -> Unit,
    onContinue: () -> Unit,
    onBottomTabSelected: (Int) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val cs = MaterialTheme.colorScheme
    var pendingTab by remember { mutableStateOf<Int?>(null) }
    val onTab: (Int) -> Unit = { index ->
        if (state.hasData()) pendingTab = index else onBottomTabSelected(index)
    }

    val opLabel = stringResource(if (state.isRent) R.string.create_listing_op_rent else R.string.create_listing_op_buy)
    val typeLabel = stringResource(propertyTypeRes(state.propertyType))
    val perMonth = stringResource(R.string.create_listing_per_month)

    Scaffold(
        containerColor = cs.background,
        topBar = { StepHeader(stringResource(R.string.create_listing_step4_title), step = 4, onBack = onBack) },
        bottomBar = {
            Column(Modifier.background(cs.surface)) {
                Button(
                    onClick = onContinue,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(48.dp)
                ) { Text(stringResource(R.string.create_listing_send_review), fontWeight = FontWeight.SemiBold) }
                NearHomeBottomBar(selectedTab = 3, onTabSelected = onTab)
            }
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(R.string.create_listing_step4_heading),
                    style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                    color = cs.onSurface
                )
                Text(
                    stringResource(R.string.create_listing_step4_subtitle),
                    style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant
                )
            }

            // Tarjeta principal: portada, tipo, precio y título
            Column(
                Modifier.fillMaxWidth().background(cs.surface, RoundedCornerShape(12.dp)).border(1.dp, cs.outlineVariant, RoundedCornerShape(12.dp)).padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.photos.firstOrNull()?.let { cover ->
                    AsyncImage(
                        model = File(cover), contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(8.dp))
                    )
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${opLabel.uppercase()} • ${typeLabel.uppercase()}",
                        style = MaterialTheme.typography.labelSmall, color = cs.primary, fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        formatPrice(state.price) + if (state.isRent) " $perMonth" else "",
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = cs.onSurface
                    )
                }
                Text(
                    state.title,
                    style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
                    maxLines = 2, overflow = TextOverflow.Ellipsis, color = cs.onSurface
                )
            }

            // Datos del inmueble
            SummaryCard(
                title = stringResource(R.string.create_listing_property_data),
                onEdit = onEditData
            ) {
                propertyRows(state).forEach { (label, value) -> SummaryRow(label, value) }
            }

            // Contenido
            SummaryCard(
                title = stringResource(R.string.create_listing_content_section),
                onEdit = onEditContent
            ) {
                Text(
                    state.description,
                    style = MaterialTheme.typography.bodySmall, color = cs.onSurface,
                    maxLines = 5, overflow = TextOverflow.Ellipsis
                )
                if (state.rules.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.create_listing_ad_rules),
                        style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant
                    )
                    Text(state.rules, style = MaterialTheme.typography.bodySmall, color = cs.onSurface)
                }
            }

            ReviewNotice()
        }
    }

    if (pendingTab != null) {
        DiscardDialog(
            onConfirm = {
                val tab = pendingTab
                pendingTab = null
                viewModel.reset()
                if (tab != null) onBottomTabSelected(tab)
            },
            onDismiss = { pendingTab = null }
        )
    }
}

@Composable
private fun propertyRows(state: CreateListingState): List<Pair<String, String>> {
    val yes = stringResource(R.string.create_listing_yes)
    val no = stringResource(R.string.create_listing_no)
    val ground = stringResource(R.string.create_listing_ground_floor)
    val bedrooms = stringResource(R.string.create_listing_bedrooms)
    val bathrooms = stringResource(R.string.create_listing_bathrooms)
    val parking = stringResource(R.string.create_listing_parking)
    val floor = stringResource(R.string.create_listing_floor)
    val levels = stringResource(R.string.create_listing_levels)
    val furnished = stringResource(R.string.create_listing_furnished)
    val pets = stringResource(R.string.create_listing_pets)
    val elevator = stringResource(R.string.create_listing_elevator)
    val garden = stringResource(R.string.create_listing_garden)
    val area = stringResource(R.string.create_listing_area)
    val landUse = stringResource(R.string.create_listing_land_use)
    val residential = stringResource(R.string.create_listing_land_residential)
    val agricultural = stringResource(R.string.create_listing_land_agricultural)
    val locationLabel = stringResource(R.string.create_listing_location_confirmed)
    fun Boolean.yn() = if (this) yes else no

    return buildList {
        when (state.propertyType) {
            PropertyType.DEPARTAMENTO -> {
                add(bedrooms to "${state.bedrooms}")
                add(bathrooms to "${state.bathrooms}")
                add(parking to "${state.parking}")
                add(floor to if (state.floor == 0) ground else "${state.floor}")
                add(furnished to state.furnished.yn())
                if (state.isRent) add(pets to state.petsAllowed.yn())
                add(elevator to state.elevator.yn())
            }
            PropertyType.CASA -> {
                add(bedrooms to "${state.bedrooms}")
                add(bathrooms to "${state.bathrooms}")
                add(parking to "${state.parking}")
                add(levels to "${state.levels}")
                add(garden to state.garden.yn())
                if (state.isRent) add(pets to state.petsAllowed.yn())
            }
            PropertyType.TERRENO -> {
                add(area to "${state.area} m²")
                add(landUse to if (state.landUse == LandUse.AGRICOLA) agricultural else residential)
            }
            null -> Unit
        }
        add(locationLabel to state.fullAddress)
    }
}

@Composable
private fun SummaryCard(title: String, onEdit: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxWidth().background(cs.surface, RoundedCornerShape(12.dp)).border(1.dp, cs.outlineVariant, RoundedCornerShape(12.dp)).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = cs.onSurface)
            Text(
                stringResource(R.string.create_listing_edit),
                style = MaterialTheme.typography.labelMedium, color = cs.primary, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onEdit).padding(4.dp)
            )
        }
        content()
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = cs.onSurface,
            textAlign = TextAlign.End, maxLines = 2, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1.4f)
        )
    }
}

@Composable
internal fun ReviewNotice() {
    val warningColors = noticeColors(NoticeTone.WARNING)
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(warningColors.container)
            .border(1.dp, warningColors.content.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Default.Warning, null, tint = warningColors.content, modifier = Modifier.size(18.dp))
        Text(
            stringResource(R.string.create_listing_review_notice),
            style = MaterialTheme.typography.bodySmall, color = warningColors.content
        )
    }
}
