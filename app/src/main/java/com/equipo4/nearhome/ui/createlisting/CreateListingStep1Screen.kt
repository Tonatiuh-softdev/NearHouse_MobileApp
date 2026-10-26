package com.equipo4.nearhome.ui.createlisting

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.equipo4.nearhome.R
import com.equipo4.nearhome.domain.model.ListingType
import com.equipo4.nearhome.domain.model.PropertyType
import com.equipo4.nearhome.ui.common.NearHomeBottomBar

@Composable
fun CreateListingStep1Screen(
    viewModel: CreateListingViewModel,
    onExit: () -> Unit,
    onContinue: () -> Unit,
    onBottomTabSelected: (Int) -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showDiscard by remember { mutableStateOf(false) }
    var pendingTab by remember { mutableStateOf<Int?>(null) }

    val requestExit = {
        pendingTab = null
        if (state.hasData()) showDiscard = true else onExit()
    }
    val onTab: (Int) -> Unit = { index ->
        if (state.hasData()) { pendingTab = index; showDiscard = true }
        else onBottomTabSelected(index)
    }

    BackHandler(onBack = requestExit)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            StepHeader(
                title = stringResource(R.string.create_listing_title),
                step = 1,
                onBack = requestExit
            )
        },
        bottomBar = {
            Column(Modifier.background(MaterialTheme.colorScheme.surface)) {
                Button(
                    onClick = onContinue,
                    enabled = state.isStep1Valid,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(48.dp)
                ) { Text(stringResource(R.string.create_listing_continue), fontWeight = FontWeight.SemiBold) }
                NearHomeBottomBar(selectedTab = 3, onTabSelected = onTab)
            }
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(R.string.create_listing_step1_heading),
                    style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold
                )
                Text(
                    subtitleFor(state.listingType, state.propertyType),
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Operación
            Text(stringResource(R.string.create_listing_operation), style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SelectableBox(
                    state.listingType == ListingType.VENTA,
                    { viewModel.setListingType(ListingType.VENTA) },
                    Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.create_listing_op_buy), fontWeight = FontWeight.SemiBold)
                    Text(
                        stringResource(R.string.create_listing_op_buy_sub),
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                SelectableBox(
                    state.listingType == ListingType.RENTA,
                    { viewModel.setListingType(ListingType.RENTA) },
                    Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.create_listing_op_rent), fontWeight = FontWeight.SemiBold)
                    Text(
                        stringResource(R.string.create_listing_op_rent_sub),
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Tipo de inmueble
            Text(stringResource(R.string.create_listing_property_type), style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    PropertyType.DEPARTAMENTO to R.string.listing_type_apartment,
                    PropertyType.CASA to R.string.listing_type_house,
                    PropertyType.TERRENO to R.string.listing_type_land
                ).forEach { (type, label) ->
                    SelectableBox(state.propertyType == type, { viewModel.setPropertyType(type) }, Modifier.weight(1f)) {
                        Text(stringResource(label), style = MaterialTheme.typography.labelLarge, maxLines = 1)
                    }
                }
            }

            // Campos vigentes: solo cuando ya hay operación y tipo
            if (state.listingType != null && state.propertyType != null) {
                FieldsCard(state, viewModel)
            }
        }
    }

    if (showDiscard) {
        DiscardDialog(
            onConfirm = {
                showDiscard = false
                viewModel.reset()
                val tab = pendingTab
                pendingTab = null
                if (tab != null) onBottomTabSelected(tab) else onExit()
            },
            onDismiss = { showDiscard = false; pendingTab = null }
        )
    }
}

@Composable
private fun subtitleFor(op: ListingType?, type: PropertyType?): String {
    val res = when {
        op == null || type == null -> R.string.create_listing_sub_default
        op == ListingType.VENTA && type == PropertyType.DEPARTAMENTO -> R.string.create_listing_sub_buy_apartment
        op == ListingType.VENTA && type == PropertyType.CASA -> R.string.create_listing_sub_buy_house
        op == ListingType.VENTA -> R.string.create_listing_sub_buy_land
        type == PropertyType.DEPARTAMENTO -> R.string.create_listing_sub_rent_apartment
        type == PropertyType.CASA -> R.string.create_listing_sub_rent_house
        else -> R.string.create_listing_sub_rent_land
    }
    return stringResource(res)
}

private data class NumField(val key: String, val labelRes: Int, val options: List<NumOption>)

@Composable
private fun FieldsCard(state: CreateListingState, vm: CreateListingViewModel) {
    val cs = MaterialTheme.colorScheme
    val isRent = state.listingType == ListingType.RENTA
    val required = stringResource(R.string.create_listing_error_required)

    val opLabel = stringResource(if (isRent) R.string.create_listing_op_rent else R.string.create_listing_op_buy)
    val typeLabel = stringResource(
        when (state.propertyType) {
            PropertyType.DEPARTAMENTO -> R.string.listing_type_apartment
            PropertyType.CASA -> R.string.listing_type_house
            else -> R.string.listing_type_land
        }
    )

    // Un solo panel abierto a la vez ("bedrooms", "bathrooms", ...)
    var openKey by remember(state.propertyType) { mutableStateOf<String?>(null) }
    val toggle: (String) -> Unit = { key ->
        if (openKey == key) { vm.touch(key); openKey = null } else openKey = key
    }

    val groundFloor = stringResource(R.string.create_listing_ground_floor)
    val groundShort = stringResource(R.string.create_listing_ground_floor_short)
    val bedrooms = NumField("bedrooms", R.string.create_listing_bedrooms, (1..6).map { NumOption(it, "$it") })
    val bathrooms = NumField("bathrooms", R.string.create_listing_bathrooms, (1..5).map { NumOption(it, "$it") })
    val parking = NumField("parking", R.string.create_listing_parking, (0..4).map { NumOption(it, "$it") })
    val floor = NumField(
        "floor", R.string.create_listing_floor,
        (0..20).map { if (it == 0) NumOption(0, groundFloor, groundShort) else NumOption(it, "$it") }
    )
    val levels = NumField("levels", R.string.create_listing_levels, (1..4).map { NumOption(it, "$it") })

    Column(
        Modifier.fillMaxWidth().border(1.dp, cs.primary, RoundedCornerShape(12.dp)).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Encabezado: "Comprar · Casa" + etiqueta con el número de campos
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$opLabel · $typeLabel", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Surface(shape = RoundedCornerShape(50), color = cs.primaryContainer) {
                Text(
                    pluralStringResource(R.plurals.create_listing_fields_count, state.fieldCount, state.fieldCount),
                    style = MaterialTheme.typography.labelSmall, color = cs.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        // Precio (y superficie solo en Terreno)
        if (state.propertyType == PropertyType.TERRENO) {
            TwoColumns(
                left = { PriceField(state, vm, isRent, required) },
                right = { AreaField(state, vm, required) }
            )
        } else {
            PriceField(state, vm, isRent, required)
        }

        // La colonia ya no se elige aquí: se detecta con la ubicación de la foto (Paso 2)

        when (state.propertyType) {
            PropertyType.DEPARTAMENTO -> {
                NumberPair(bedrooms, bathrooms, state, vm, openKey, toggle) { openKey = null }
                NumberPair(parking, floor, state, vm, openKey, toggle) { openKey = null }
                CheckRow(stringResource(R.string.create_listing_furnished), state.furnished) { c ->
                    vm.update { it.copy(furnished = c) }
                }
                // Mascotas permitidas solo aplica al rentar
                if (isRent) {
                    CheckRow(stringResource(R.string.create_listing_pets), state.petsAllowed) { c ->
                        vm.update { it.copy(petsAllowed = c) }
                    }
                }
                CheckRow(stringResource(R.string.create_listing_elevator), state.elevator) { c ->
                    vm.update { it.copy(elevator = c) }
                }
            }

            PropertyType.CASA -> {
                NumberPair(bedrooms, bathrooms, state, vm, openKey, toggle) { openKey = null }
                NumberPair(parking, levels, state, vm, openKey, toggle) { openKey = null }
                CheckRow(stringResource(R.string.create_listing_garden), state.garden) { c ->
                    vm.update { it.copy(garden = c) }
                }
                // Mascotas permitidas solo aplica al rentar
                if (isRent) {
                    CheckRow(stringResource(R.string.create_listing_pets), state.petsAllowed) { c ->
                        vm.update { it.copy(petsAllowed = c) }
                    }
                }
            }

            PropertyType.TERRENO -> {
                LabeledField(
                    stringResource(R.string.create_listing_land_use),
                    if (state.isInvalid("landUse")) required else null
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            LandUse.HABITACIONAL to R.string.create_listing_land_residential,
                            LandUse.AGRICOLA to R.string.create_listing_land_agricultural
                        ).forEach { (use, label) ->
                            SelectableBox(
                                state.landUse == use,
                                { vm.update("landUse") { it.copy(landUse = use) } },
                                Modifier.weight(1f)
                            ) {
                                Text(stringResource(label), style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }

            null -> Unit
        }
    }
}

@Composable
private fun PriceField(state: CreateListingState, vm: CreateListingViewModel, isRent: Boolean, required: String) {
    val err = when {
        !state.isInvalid("price") -> null
        state.price.isEmpty() -> required
        else -> stringResource(R.string.create_listing_error_price)
    }
    LabeledField(
        stringResource(if (isRent) R.string.create_listing_price_rent else R.string.create_listing_price_total),
        err
    ) {
        OutlinedTextField(
            value = state.price,
            onValueChange = { v ->
                vm.update("price") { it.copy(price = v.filter(Char::isDigit).take(9).trimStart('0')) }
            },
            visualTransformation = MoneyVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true, isError = err != null,
            placeholder = { Text("\$0") },
            shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AreaField(state: CreateListingState, vm: CreateListingViewModel, required: String) {
    val err = when {
        !state.isInvalid("area") -> null
        state.area.isEmpty() -> required
        else -> stringResource(R.string.create_listing_error_area)
    }
    LabeledField(stringResource(R.string.create_listing_area), err) {
        OutlinedTextField(
            value = state.area,
            onValueChange = { v ->
                vm.update("area") { it.copy(area = v.filter(Char::isDigit).take(7).trimStart('0')) }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true, isError = err != null,
            suffix = { Text("m²") },
            shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()
        )
    }
}

/** Dos características lado a lado; el panel de opciones se abre debajo, a todo el ancho. */
@Composable
private fun NumberPair(
    left: NumField,
    right: NumField,
    state: CreateListingState,
    vm: CreateListingViewModel,
    openKey: String?,
    onToggle: (String) -> Unit,
    onPicked: () -> Unit
) {
    val placeholder = stringResource(R.string.create_listing_select)
    val required = stringResource(R.string.create_listing_error_required)

    @Composable
    fun header(f: NumField) {
        LabeledField(stringResource(f.labelRes), if (state.isInvalid(f.key)) required else null) {
            DropdownHeader(
                text = f.options.firstOrNull { it.value == state.numberOf(f.key) }?.label,
                placeholder = placeholder,
                expanded = openKey == f.key,
                isError = state.isInvalid(f.key),
                onClick = { onToggle(f.key) }
            )
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TwoColumns(left = { header(left) }, right = { header(right) })
        listOf(left, right).firstOrNull { it.key == openKey }?.let { f ->
            NumberOptionsPanel(f.options, state.numberOf(f.key)) { v ->
                vm.update(f.key) { s -> s.withNumber(f.key, v) }
                onPicked()
            }
        }
    }
}

@Composable
private fun TwoColumns(left: @Composable () -> Unit, right: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.weight(1f)) { left() }
        Box(Modifier.weight(1f)) { right() }
    }
}