package com.equipo4.nearhome.ui.home.filter

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo4.nearhome.domain.model.ListingType
import com.equipo4.nearhome.domain.model.PropertyType
import com.equipo4.nearhome.ui.theme.adminAccent
import com.equipo4.nearhome.ui.theme.onAdminAccent

private val LightGray = Color(0xFFF3F4F6)
private val BorderColor = Color(0xFFE5E7EB)
private val SelectedBg = Color(0xFFF0F4F8)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchFiltersScreen(
    viewModel: SearchFiltersViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onApplySuccess: () -> Unit = {}
) {
    val state by viewModel.filterState.collectAsState()
    val primaryColor = adminAccent()

    var minPriceStr by remember(state.minPrice) { mutableStateOf(state.minPrice?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }
    var maxPriceStr by remember(state.maxPrice) { mutableStateOf(state.maxPrice?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }

    var minSupStr by remember(state.minSuperficie) { mutableStateOf(state.minSuperficie?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }
    var maxSupStr by remember(state.maxSuperficie) { mutableStateOf(state.maxSuperficie?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }

    var coloniasExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Filtros", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                        Text(
                            text = state.buildSummaryText(),
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280),
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.clearFilters()
                            minPriceStr = ""
                            maxPriceStr = ""
                            minSupStr = ""
                            maxSupStr = ""
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, primaryColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Limpiar", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = {
                            viewModel.updateFilter {
                                it.copy(
                                    minPrice = minPriceStr.toDoubleOrNull(),
                                    maxPrice = maxPriceStr.toDoubleOrNull(),
                                    minSuperficie = minSupStr.toDoubleOrNull(),
                                    maxSuperficie = maxSupStr.toDoubleOrNull()
                                )
                            }
                            viewModel.applyFilters()
                            onApplySuccess()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = onAdminAccent()),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Aplicar", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Segmented Buttons: Comprar / Rentar
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Operación", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(LightGray)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isComprar = state.listingType == ListingType.VENTA
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isComprar) primaryColor else Color.Transparent)
                            .clickable { viewModel.updateFilter { it.copy(listingType = ListingType.VENTA) } },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Comprar",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isComprar) Color.White else Color(0xFF4B5563)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isComprar) primaryColor else Color.Transparent)
                            .clickable { viewModel.updateFilter { it.copy(listingType = ListingType.RENTA) } },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Rentar",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (!isComprar) Color.White else Color(0xFF4B5563)
                        )
                    }
                }
            }

            // 2. Property Type Buttons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tipo de Inmueble", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val types = listOf(
                        PropertyType.DEPARTAMENTO to "Departamento",
                        PropertyType.CASA to "Casa",
                        PropertyType.TERRENO to "Terreno"
                    )
                    types.forEach { (type, label) ->
                        val selected = state.propertyType == type
                        OutlinedButton(
                            onClick = {
                                viewModel.updateFilter { it.copy(propertyType = if (selected) null else type) }
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) primaryColor else BorderColor
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selected) SelectedBg else Color.White,
                                contentColor = if (selected) primaryColor else Color(0xFF4B5563)
                            ),
                            modifier = Modifier.weight(1f).height(44.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text(label, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, maxLines = 1)
                        }
                    }
                }
            }

            // 3. Price Inputs
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val priceTitle = if (state.listingType == ListingType.RENTA) "Renta mensual (MXN)" else "Precio de venta (MXN)"
                Text(priceTitle, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = minPriceStr,
                        onValueChange = { minPriceStr = it },
                        label = { Text("Mínimo") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    Text("—", color = Color(0xFF9CA3AF))
                    OutlinedTextField(
                        value = maxPriceStr,
                        onValueChange = { maxPriceStr = it },
                        label = { Text("Máximo") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // 4. Location multi-select dropdown for colonias
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ubicación", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                    Text("Selección múltiple", fontSize = 12.sp, color = Color(0xFF6B7280))
                }

                Spacer(Modifier.height(4.dp))

                val isColoniaSelected = state.selectedColonias.isNotEmpty()
                OutlinedCard(
                    onClick = { coloniasExpanded = !coloniasExpanded },
                    shape = if (coloniasExpanded) RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 0.dp, bottomEnd = 0.dp) else RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (isColoniaSelected) SelectedBg else Color.White
                    ),
                    border = BorderStroke(
                        width = if (isColoniaSelected) 1.5.dp else 1.dp,
                        color = if (isColoniaSelected) primaryColor else BorderColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val colText = if (!isColoniaSelected) {
                            "Seleccionar ubicación"
                        } else {
                            "${state.selectedColonias.size} seleccionadas: ${state.selectedColonias.joinToString(", ")}"
                        }
                        Text(
                            colText,
                            fontSize = 14.sp,
                            fontWeight = if (isColoniaSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isColoniaSelected) primaryColor else Color(0xFF1F2937),
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (coloniasExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = if (isColoniaSelected) primaryColor else Color(0xFF6B7280)
                        )
                    }
                }

                if (coloniasExpanded) {
                    Card(
                        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 12.dp, bottomEnd = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // "Todas"
                            val isAllSelected = state.selectedColonias.isEmpty()
                            FlatCheckItem(
                                text = "Todas las ubicaciones",
                                checked = isAllSelected,
                                onCheckedChange = { viewModel.updateFilter { it.copy(selectedColonias = emptySet()) } },
                                primaryColor = primaryColor,
                                iconOnRight = false
                            )

                            // Lista de colonias en 2 columnas
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                ALL_COLONIAS.forEach { colonia ->
                                    val isSelected = colonia in state.selectedColonias
                                    Box(modifier = Modifier.fillMaxWidth(0.48f)) {
                                        FlatCheckItem(
                                            text = colonia,
                                            checked = isSelected,
                                            onCheckedChange = { checked ->
                                                viewModel.updateFilter { s ->
                                                    val next = if (checked) s.selectedColonias + colonia else s.selectedColonias - colonia
                                                    s.copy(selectedColonias = next)
                                                }
                                            },
                                            primaryColor = primaryColor,
                                            iconOnRight = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Characteristics sections dynamically rendered
            when (state.propertyType) {
                PropertyType.CASA -> {
                    CasaCharacteristicsSection(state, viewModel, primaryColor)
                }
                PropertyType.DEPARTAMENTO -> {
                    DeptoCharacteristicsSection(state, viewModel, primaryColor)
                }
                PropertyType.TERRENO -> {
                    TerrenoCharacteristicsSection(state, viewModel, minSupStr, { minSupStr = it }, maxSupStr, { maxSupStr = it }, primaryColor)
                }
                null -> {
                    CasaCharacteristicsSection(state, viewModel, primaryColor)
                }
            }
        }
    }
}

@Composable
private fun NumericDropdownSelector(
    title: String,
    selectedValues: Set<Int>,
    onSelectValue: (Int?) -> Unit,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val singleSelected = selectedValues.firstOrNull()
    val hasSelection = singleSelected != null
    val summary = when {
        singleSelected == null -> "Todos"
        singleSelected == 5 -> "5+"
        else -> singleSelected.toString()
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
        OutlinedCard(
            onClick = { expanded = !expanded },
            shape = if (expanded) RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 0.dp, bottomEnd = 0.dp) else RoundedCornerShape(12.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = if (hasSelection) SelectedBg else Color.White
            ),
            border = BorderStroke(
                width = if (hasSelection) 1.5.dp else 1.dp,
                color = if (hasSelection) primaryColor else BorderColor
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    summary,
                    fontSize = 13.sp,
                    fontWeight = if (hasSelection) FontWeight.Bold else FontWeight.Normal,
                    color = if (hasSelection) primaryColor else Color(0xFF1F2937)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (hasSelection) primaryColor else Color(0xFF6B7280)
                )
            }
        }

        if (expanded) {
            Card(
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 12.dp, bottomEnd = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(1, 2, 3, 4, 5).forEach { num ->
                        val isSelected = singleSelected == num
                        OutlinedButton(
                            onClick = {
                                onSelectValue(if (isSelected) null else num)
                                expanded = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (isSelected) primaryColor else BorderColor),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) SelectedBg else Color.White,
                                contentColor = if (isSelected) primaryColor else Color(0xFF4B5563)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(if (num == 5) "5+" else "$num", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/** Elemento de lista plano (sin tarjeta con borde propio), con check circular al seleccionarse (soporta icono a la izquierda o derecha). */
@Composable
private fun FlatCheckItem(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    primaryColor: Color,
    iconOnRight: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (checked) SelectedBg else Color.Transparent)
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (!iconOnRight) {
            CheckIndicator(checked, primaryColor)
            Spacer(Modifier.width(10.dp))
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Medium,
                color = if (checked) primaryColor else Color(0xFF1F2937),
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
        } else {
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Medium,
                color = if (checked) primaryColor else Color(0xFF1F2937),
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            Spacer(Modifier.width(10.dp))
            CheckIndicator(checked, primaryColor)
        }
    }
}

@Composable
private fun CheckIndicator(checked: Boolean, primaryColor: Color) {
    if (checked) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(primaryColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = onAdminAccent(),
                modifier = Modifier.size(12.dp)
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(BorderColor, RoundedCornerShape(4.dp))
                .padding(1.dp)
                .background(Color.White, RoundedCornerShape(3.dp))
        )
    }
}

@Composable
private fun CasaCharacteristicsSection(state: PropertyFilterState, viewModel: SearchFiltersViewModel, primaryColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        HorizontalDivider(color = BorderColor)
        Text("Características de la Casa", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))

        // Fila 1: Dormitorios | Baños
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NumericDropdownSelector(
                title = "Dormitorios",
                selectedValues = state.bedrooms,
                onSelectValue = { num ->
                    viewModel.updateFilter { it.copy(bedrooms = if (num == null) emptySet() else setOf(num)) }
                },
                primaryColor = primaryColor,
                modifier = Modifier.weight(1f)
            )
            NumericDropdownSelector(
                title = "Baños",
                selectedValues = state.bathrooms,
                onSelectValue = { num ->
                    viewModel.updateFilter { it.copy(bathrooms = if (num == null) emptySet() else setOf(num)) }
                },
                primaryColor = primaryColor,
                modifier = Modifier.weight(1f)
            )
        }

        // Fila 2: Estacionamiento | Niveles de la casa
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NumericDropdownSelector(
                title = "Estacionamiento",
                selectedValues = state.garages,
                onSelectValue = { num ->
                    viewModel.updateFilter { it.copy(garages = if (num == null) emptySet() else setOf(num)) }
                },
                primaryColor = primaryColor,
                modifier = Modifier.weight(1f)
            )
            NumericDropdownSelector(
                title = "Niveles de la casa",
                selectedValues = state.niveles,
                onSelectValue = { num ->
                    viewModel.updateFilter { it.copy(niveles = if (num == null) emptySet() else setOf(num)) }
                },
                primaryColor = primaryColor,
                modifier = Modifier.weight(1f)
            )
        }

        FlatCheckItem(
            text = "Jardín / Patio",
            checked = state.jardinPatio,
            onCheckedChange = { checked -> viewModel.updateFilter { it.copy(jardinPatio = checked) } },
            primaryColor = primaryColor,
            iconOnRight = true
        )

        if (state.listingType == ListingType.RENTA) {
            FlatCheckItem(
                text = "Mascotas permitidas",
                checked = state.mascotasPermitidas,
                onCheckedChange = { checked -> viewModel.updateFilter { it.copy(mascotasPermitidas = checked) } },
                primaryColor = primaryColor,
                iconOnRight = true
            )
        }
    }
}

@Composable
private fun DeptoCharacteristicsSection(state: PropertyFilterState, viewModel: SearchFiltersViewModel, primaryColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        HorizontalDivider(color = BorderColor)
        Text("Características del Departamento", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))

        // Fila 1: Dormitorios | Baños
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NumericDropdownSelector(
                title = "Dormitorios",
                selectedValues = state.bedrooms,
                onSelectValue = { num ->
                    viewModel.updateFilter { it.copy(bedrooms = if (num == null) emptySet() else setOf(num)) }
                },
                primaryColor = primaryColor,
                modifier = Modifier.weight(1f)
            )
            NumericDropdownSelector(
                title = "Baños",
                selectedValues = state.bathrooms,
                onSelectValue = { num ->
                    viewModel.updateFilter { it.copy(bathrooms = if (num == null) emptySet() else setOf(num)) }
                },
                primaryColor = primaryColor,
                modifier = Modifier.weight(1f)
            )
        }

        // Fila 2: Estacionamiento | Piso
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NumericDropdownSelector(
                title = "Estacionamiento",
                selectedValues = state.garages,
                onSelectValue = { num ->
                    viewModel.updateFilter { it.copy(garages = if (num == null) emptySet() else setOf(num)) }
                },
                primaryColor = primaryColor,
                modifier = Modifier.weight(1f)
            )
            NumericDropdownSelector(
                title = "Piso",
                selectedValues = state.piso,
                onSelectValue = { num ->
                    viewModel.updateFilter { it.copy(piso = if (num == null) emptySet() else setOf(num)) }
                },
                primaryColor = primaryColor,
                modifier = Modifier.weight(1f)
            )
        }

        FlatCheckItem(
            text = "Amueblado",
            checked = state.amueblado,
            onCheckedChange = { checked -> viewModel.updateFilter { it.copy(amueblado = checked) } },
            primaryColor = primaryColor,
            iconOnRight = true
        )

        if (state.listingType == ListingType.RENTA) {
            FlatCheckItem(
                text = "Mascotas permitidas",
                checked = state.mascotasPermitidas,
                onCheckedChange = { checked -> viewModel.updateFilter { it.copy(mascotasPermitidas = checked) } },
                primaryColor = primaryColor,
                iconOnRight = true
            )

            FlatCheckItem(
                text = "Elevador",
                checked = state.elevador,
                onCheckedChange = { checked -> viewModel.updateFilter { it.copy(elevador = checked) } },
                primaryColor = primaryColor,
                iconOnRight = true
            )
        }
    }
}

@Composable
private fun TerrenoCharacteristicsSection(
    state: PropertyFilterState,
    viewModel: SearchFiltersViewModel,
    minSup: String,
    onMinSupChange: (String) -> Unit,
    maxSup: String,
    onMaxSupChange: (String) -> Unit,
    primaryColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        HorizontalDivider(color = BorderColor)
        Text("Características del terreno", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Superficie (m²)", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6B7280))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = minSup,
                    onValueChange = minSupChange@{ onMinSupChange(it) },
                    label = { Text("Mínimo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
                Text("—", color = Color(0xFF9CA3AF))
                OutlinedTextField(
                    value = maxSup,
                    onValueChange = maxSupChange@{ onMaxSupChange(it) },
                    label = { Text("Máximo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // Uso del suelo dropdown unificado
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Uso del suelo", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
            Spacer(Modifier.height(4.dp))

            var usoExpanded by remember { mutableStateOf(false) }
            val isUsoSelected = state.usoSuelo != null
            OutlinedCard(
                onClick = { usoExpanded = !usoExpanded },
                shape = if (usoExpanded) RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 0.dp, bottomEnd = 0.dp) else RoundedCornerShape(12.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (isUsoSelected) SelectedBg else Color.White
                ),
                border = BorderStroke(
                    width = if (isUsoSelected) 1.5.dp else 1.dp,
                    color = if (isUsoSelected) primaryColor else BorderColor
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        state.usoSuelo ?: "Seleccionar",
                        fontSize = 14.sp,
                        fontWeight = if (isUsoSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isUsoSelected) primaryColor else Color(0xFF1F2937)
                    )
                    Icon(
                        imageVector = if (usoExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = if (isUsoSelected) primaryColor else Color(0xFF6B7280)
                    )
                }
            }

            if (usoExpanded) {
                Card(
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 12.dp, bottomEnd = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(null, "Habitacional", "Agrícola").forEach { uso ->
                            val isSelected = state.usoSuelo == uso
                            FlatCheckItem(
                                text = uso ?: "Todos",
                                checked = isSelected,
                                onCheckedChange = { _ ->
                                    viewModel.updateFilter { it.copy(usoSuelo = uso) }
                                    usoExpanded = false
                                },
                                primaryColor = primaryColor,
                                iconOnRight = false
                            )
                        }
                    }
                }
            }
        }
    }
}
