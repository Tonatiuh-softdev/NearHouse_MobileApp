package com.equipo4.nearhome.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.equipo4.nearhome.R
import com.equipo4.nearhome.ui.common.NearHomeBottomBar
import com.equipo4.nearhome.ui.common.NearHomeDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    reselectTrigger: Long = 0L, // <-- AGREGADO
    viewModel: NotificationsViewModel,
    onNavigate: (String) -> Unit,
    onBottomTabSelected: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var notifAEliminar by remember { mutableStateOf<String?>(null) }

    // Recargar si tocan la campana estando ya en la pantalla
    LaunchedEffect(reselectTrigger) {
        if (reselectTrigger > 0) {
            viewModel.refresh()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notifications_title)) },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.notif_menu_mark_all)) },
                                onClick = {
                                    showMenu = false
                                    viewModel.marcarTodasLeidas()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.notif_menu_delete_all), color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    showDeleteAllDialog = true
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NearHomeBottomBar(
                selectedTab = 2,
                onTabSelected = onBottomTabSelected,
                unreadCount = unreadCount
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is NotificationsUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is NotificationsUiState.Empty -> {
                    NotificationsEmptyState()
                }
                is NotificationsUiState.Error -> {
                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(R.string.notifications_error))
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadNotifications() }) {
                            Text(stringResource(R.string.notifications_retry))
                        }
                    }
                }
                is NotificationsUiState.Success -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.list, key = { it.id }) { notif ->
                            NotificationItem(
                                notificacion = notif,
                                onClick = { viewModel.onNotificationClicked(notif, onNavigate) },
                                onLongClick = { notifAEliminar = notif.id }
                            )
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo confirmación: Borrar todas
    if (showDeleteAllDialog) {
        NearHomeDialog(
            title = stringResource(R.string.notif_menu_delete_confirm_title),
            message = stringResource(R.string.notif_menu_delete_confirm_body),
            confirmText = stringResource(R.string.confirm_delete),
            onConfirm = {
                viewModel.borrarTodas()
                showDeleteAllDialog = false
            },
            dismissText = stringResource(R.string.cancel),
            onDismiss = { showDeleteAllDialog = false },
            destructive = true
        )
    }

    // Diálogo confirmación: Borrar una
    notifAEliminar?.let { id ->
        NearHomeDialog(
            title = stringResource(R.string.notif_menu_delete_confirm_title),
            message = stringResource(R.string.notif_menu_delete_confirm_body),
            confirmText = stringResource(R.string.confirm_delete),
            onConfirm = {
                viewModel.borrarUna(id)
                notifAEliminar = null
            },
            dismissText = stringResource(R.string.cancel),
            onDismiss = { notifAEliminar = null },
            destructive = true
        )
    }
}