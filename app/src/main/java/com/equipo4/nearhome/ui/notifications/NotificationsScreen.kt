package com.equipo4.nearhome.ui.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    reselectTrigger: Long = 0L,
    viewModel: NotificationsViewModel,
    onNavigate: (String) -> Unit,
    onBottomTabSelected: (Int) -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var notifAEliminar by remember { mutableStateOf<String?>(null) }

    BackHandler {
        onBottomTabSelected(0)
    }

    val errorMessage = stringResource(R.string.refresh_error_connection)

    val triggerReload = {
        viewModel.recargar { success ->
            if (!success) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(reselectTrigger) {
        if (reselectTrigger > 0) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
            triggerReload()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notifications_title)) },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.notif_menu_mark_all)) },
                            onClick = {
                                showMenu = false
                                viewModel.marcarTodasLeidas()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.notif_menu_delete_all)) },
                            onClick = {
                                showMenu = false
                                showDeleteAllDialog = true
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {
            NearHomeBottomBar(
                selectedTab = 2,
                onTabSelected = onBottomTabSelected,
                unreadCount = viewModel.unreadCount.collectAsState().value
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = triggerReload,
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
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
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
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text(stringResource(R.string.notif_menu_delete_confirm_title)) },
            text = { Text(stringResource(R.string.notif_menu_delete_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.borrarTodas()
                    showDeleteAllDialog = false
                }) { Text(stringResource(R.string.confirm_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Diálogo confirmación: Borrar una (por mantener presionado)
    notifAEliminar?.let { id ->
        AlertDialog(
            onDismissRequest = { notifAEliminar = null },
            title = { Text(stringResource(R.string.notif_menu_delete_confirm_title)) },
            text = { Text(stringResource(R.string.notif_menu_delete_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.borrarUna(id)
                    notifAEliminar = null
                }) { Text(stringResource(R.string.confirm_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { notifAEliminar = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}