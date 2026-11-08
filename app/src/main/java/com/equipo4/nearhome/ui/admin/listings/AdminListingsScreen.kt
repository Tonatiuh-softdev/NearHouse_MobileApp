package com.equipo4.nearhome.ui.admin.listings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo4.nearhome.R
import com.equipo4.nearhome.ui.common.NearHomeBottomBar
import com.equipo4.nearhome.ui.theme.NoticeTone
import com.equipo4.nearhome.ui.theme.adminAccent
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminListingsScreen(
    viewModel: AdminListingsViewModel = viewModel(),
    message: String? = null,
    onMessageShown: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onFiltersClick: () -> Unit = {},
    onListingClick: (String) -> Unit = {},
    onBottomTabSelected: (Int) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.reload()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
                            stringResource(R.string.listings_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (state.isQueue) {
                                pluralStringResource(R.plurals.listings_pending_count, state.pendingCount, state.pendingCount)
                            } else {
                                pluralStringResource(R.plurals.listings_results_count, state.results.size, state.results.size)
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                if (state.isQueue) {
                    NoticeBanner(
                        tone = NoticeTone.WARNING,
                        text = stringResource(R.string.listings_review_notice),
                        icon = Icons.Filled.Schedule,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                NearHomeBottomBar(selectedTab = 3, onTabSelected = onBottomTabSelected)
            }
        },
        snackbarHost = { ListingSnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                SearchField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange
                )
                Spacer(Modifier.height(12.dp))
                FiltersRow(state, onFiltersClick)
                Spacer(Modifier.height(10.dp))
                Text(
                    text = showingText(state),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            when {
                state.results.isEmpty() && state.isLoading -> {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = adminAccent())
                    }
                }

                state.results.isEmpty() && state.loadError -> {
                    LoadErrorState(onRetry = viewModel::reload, modifier = Modifier.weight(1f))
                }

                state.results.isEmpty() -> {
                    EmptyListings(
                        state = state,
                        onClear = viewModel::clearAll,
                        modifier = Modifier.weight(1f)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.results, key = { it.id }) { listing ->
                            ListingCard(listing = listing, onClick = { onListingClick(listing.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        placeholder = { Text(stringResource(R.string.listings_search_hint), fontSize = 14.sp) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.listings_search_clear))
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = adminAccent(),
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

/** Etiquetas de los filtros activos y, al final, el botón "Filtros". */
@Composable
private fun FiltersRow(state: AdminListingsUiState, onFiltersClick: () -> Unit) {
    // En la cola de pendientes no se muestran etiquetas.
    val labels = if (state.isQueue) emptyList() else activeFilterLabels(state.filters)
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(labels) { label ->
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, adminAccent())
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                )
            }
        }
        item {
            Surface(
                onClick = onFiltersClick,
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.listings_filters_button),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun showingText(state: AdminListingsUiState): String {
    val what = when {
        state.isQueue -> stringResource(R.string.listings_showing_queue)
        state.filters.selectedCount == 0 -> stringResource(R.string.listings_showing_all)
        else -> activeFilterLabels(state.filters).joinToString(", ")
    }
    return stringResource(R.string.listings_showing_format, what, state.filters.order.label())
}

@Composable
private fun EmptyListings(state: AdminListingsUiState, onClear: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Apartment,
                contentDescription = null,
                tint = adminAccent(),
                modifier = Modifier.size(38.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.listings_empty_title),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            emptyListingsMessage(state.filters, state.query),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (!state.isQueue) {
            Spacer(Modifier.height(20.dp))
            OutlinedButton(
                onClick = onClear,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, adminAccent()),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = adminAccent()),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(stringResource(R.string.listings_clear_button))
            }
        }
    }
}

@Composable
private fun LoadErrorState(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.listings_load_error),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, adminAccent()),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = adminAccent())
        ) {
            Text(stringResource(R.string.listings_retry))
        }
    }
}