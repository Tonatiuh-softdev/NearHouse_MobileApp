package com.equipo4.nearhome.ui.home.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo4.nearhome.R
import com.equipo4.nearhome.ui.common.NearHomeBottomBar
import com.equipo4.nearhome.ui.common.PropertyCard
import com.equipo4.nearhome.ui.home.filter.PropertyFilterManager
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

private val LightGrayBg = Color(0xFFF3F4F6)
private val SectionGrayBg = Color(0xFFF8F9FA)
private val BorderColor = Color(0xFFE5E7EB)
private val TextGray = Color(0xFF6B7280)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onPropertyClick: (String) -> Unit = {},
    onToggleToMap: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    onBottomTabSelected: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val filterState by PropertyFilterManager.filterState.collectAsState()
    val activeCount = filterState.getSelectedCount()
    val hasFilters = activeCount > 0

    Scaffold(
        bottomBar = {
            NearHomeBottomBar(
                selectedTab = 0, // Inicio
                onTabSelected = onBottomTabSelected
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Encabezado superior
            HomeHeader(
                searchQuery = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                onToggleToMap = onToggleToMap,
                onHomeClick = {
                    viewModel.onSearchQueryChange("")
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                }
            )

            // Botones de filtro y ordenamiento
            FilterAndSortRow(
                onFilterClick = onFilterClick,
                hasFilters = hasFilters,
                activeCount = activeCount
            )

            Spacer(modifier = Modifier.height(4.dp))

            // SECCIÓN DE PROPIEDADES
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SectionGrayBg)
            ) {
                Text(
                    text = "${formatNumber(uiState.properties.size)} inmuebles",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGray,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = uiState.properties,
                        key = { it.id }
                    ) { property ->
                        PropertyCard(
                            property = property,
                            onToggleSave = { viewModel.toggleSaveProperty(property.id) },
                            onClick = { onPropertyClick(property.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onToggleToMap: () -> Unit,
    onHomeClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_nearhouse_logo),
            contentDescription = "Logo NearHome",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onHomeClick() }
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .clip(RoundedCornerShape(23.dp))
                .background(LightGrayBg)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (searchQuery.isEmpty()) {
                    Text(
                        text = "Busca por ubicación",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 13.sp,
                        color = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                painter = painterResource(id = R.drawable.ic_localizacion),
                contentDescription = "Ver mapa",
                tint = Color.Black,
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onToggleToMap() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterAndSortRow(
    onFilterClick: () -> Unit,
    hasFilters: Boolean,
    activeCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FilterChip(
            selected = hasFilters,
            onClick = onFilterClick,
            label = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Outlined.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Filtrar")
                    if (hasFilters) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(Color(0xFF0B2C4D), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = activeCount.toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            shape = RoundedCornerShape(10.dp),
            colors = FilterChipDefaults.filterChipColors(
                containerColor = Color.White,
                selectedContainerColor = Color(0xFFE0F2FE),
                labelColor = Color(0xFF1F2937),
                selectedLabelColor = Color(0xFF0369A1)
            )
        )

        OutlinedButton(
            onClick = { },
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, BorderColor),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_ordenar),
                contentDescription = "Ordenar",
                tint = Color.Black,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Ordenar", fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

private fun formatNumber(number: Int): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    return formatter.format(number)
}