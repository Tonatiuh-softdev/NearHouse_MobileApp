package com.equipo4.nearhome.ui.home.list

import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo4.nearhome.R
import com.equipo4.nearhome.ui.common.NearHomeBottomBar
import com.equipo4.nearhome.ui.common.PropertyCard
import com.equipo4.nearhome.ui.home.filter.PropertyFilterManager
import com.equipo4.nearhome.ui.theme.nearHouseColors
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

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

    LaunchedEffect(uiState.sortOption) {
        listState.scrollToItem(0)
    }

    Scaffold(
        bottomBar = {
            NearHomeBottomBar(
                selectedTab = 0, // Inicio
                onTabSelected = { index ->
                    if (index == 0) {
                        viewModel.onSearchQueryChange("")
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                    } else {
                        onBottomTabSelected(index)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
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
                activeCount = activeCount,
                sortOption = uiState.sortOption,
                onSortOptionSelected = viewModel::setSortOption
            )

            Spacer(modifier = Modifier.height(4.dp))

            // SECCIÓN DE PROPIEDADES
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Text(
                    text = stringResource(R.string.home_properties_count, formatNumber(uiState.properties.size)),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                if (uiState.properties.isEmpty()) {
                    EmptyHomeState(onClearFilters = { viewModel.clearFilters() })
                } else {
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
                            Box(
                                modifier = Modifier.animateItem(
                                    placementSpec = spring(dampingRatio = 1f, stiffness = 400f)
                                )
                            ) {
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
    }
}

@Composable
private fun HomeHeader(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onToggleToMap: () -> Unit,
    onHomeClick: () -> Unit = {}
) {
    var lastToggleTime by remember { mutableStateOf(0L) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_nearhouse_logo),
            contentDescription = stringResource(R.string.logo_cd),
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
                .background(MaterialTheme.nearHouseColors.searchBarBg)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (searchQuery.isEmpty()) {
                    Text(
                        text = stringResource(R.string.home_search_location),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                painter = painterResource(id = R.drawable.ic_localizacion),
                contentDescription = stringResource(R.string.home_view_map),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(18.dp)
                    .clickable {
                        val now = System.currentTimeMillis()
                        if (now - lastToggleTime > 400) {
                            lastToggleTime = now
                            onToggleToMap()
                        }
                    }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterAndSortRow(
    onFilterClick: () -> Unit,
    hasFilters: Boolean,
    activeCount: Int,
    sortOption: SortOption,
    onSortOptionSelected: (SortOption) -> Unit
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
            modifier = Modifier.height(36.dp),
            label = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Outlined.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(stringResource(R.string.home_filter_btn))
                    if (hasFilters) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = activeCount.toString(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            shape = RoundedCornerShape(8.dp),
            colors = FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.nearHouseColors.filterChipBg,
                selectedContainerColor = MaterialTheme.nearHouseColors.filterChipSelectedBg,
                labelColor = MaterialTheme.colorScheme.onSurface,
                selectedLabelColor = MaterialTheme.colorScheme.primary
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = hasFilters,
                borderColor = MaterialTheme.colorScheme.outline,
                selectedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        SortDropdownButton(
            sortOption = sortOption,
            onSortOptionSelected = onSortOptionSelected
        )
    }
}

@Composable
private fun SortDropdownButton(
    sortOption: SortOption,
    onSortOptionSelected: (SortOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var chipHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    val labelText = when (sortOption) {
        SortOption.DEFAULT -> stringResource(R.string.home_sort)
        SortOption.PRICE_LOW_TO_HIGH -> stringResource(R.string.sort_price_low_high)
        SortOption.PRICE_HIGH_TO_LOW -> stringResource(R.string.sort_price_high_low)
    }

    Box(modifier = Modifier.onSizeChanged { chipHeight = with(density) { it.height.toDp() } }) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.height(36.dp),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.nearHouseColors.filterChipBg,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_ordenar),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = labelText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(0.dp, -chipHeight),
            shape = RoundedCornerShape(12.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.width(230.dp)
        ) {
            SortOptionRow(
                label = stringResource(R.string.home_sort),
                checked = sortOption == SortOption.DEFAULT,
                trailing = {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.common_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp).clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { expanded = false }
                    )
                }
            ) {
                onSortOptionSelected(SortOption.DEFAULT)
                expanded = false
            }

            SortOptionRow(
                label = stringResource(R.string.sort_price_low_high),
                checked = sortOption == SortOption.PRICE_LOW_TO_HIGH
            ) {
                onSortOptionSelected(SortOption.PRICE_LOW_TO_HIGH)
                expanded = false
            }

            SortOptionRow(
                label = stringResource(R.string.sort_price_high_low),
                checked = sortOption == SortOption.PRICE_HIGH_TO_LOW
            ) {
                onSortOptionSelected(SortOption.PRICE_HIGH_TO_LOW)
                expanded = false
            }
        }
    }
}

@Composable
private fun SortOptionRow(
    label: String,
    checked: Boolean,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (checked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        trailing?.invoke()
    }
}

private fun formatNumber(number: Int): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    return formatter.format(number)
}

@Composable
private fun EmptyHomeState(onClearFilters: () -> Unit) {
    val emptyCircleColor = MaterialTheme.colorScheme.surfaceVariant
    val emptyIconColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(160.dp).background(emptyCircleColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_nearhouse_logo),
                contentDescription = null,
                tint = emptyIconColor.copy(alpha = 0.5f),
                modifier = Modifier.size(72.dp)
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.home_empty_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.home_empty_subtitle),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        OutlinedButton(
            onClick = onClearFilters,
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(stringResource(R.string.home_empty_clear_filters))
        }
    }
}
