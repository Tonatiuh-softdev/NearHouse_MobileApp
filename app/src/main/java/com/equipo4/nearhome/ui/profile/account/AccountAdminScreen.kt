package com.equipo4.nearhome.ui.profile.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo4.nearhome.R
import com.equipo4.nearhome.ui.common.NearHomeBottomBar
import com.equipo4.nearhome.ui.profile.UserRole

@Composable
fun AccountAdminRoute(
    viewModel: AccountAdminViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPlans: () -> Unit,
    onBottomTabSelected: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AccountAdminScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        onNavigateToPlans = onNavigateToPlans,
        onBottomTabSelected = onBottomTabSelected
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountAdminScreen(
    uiState: AccountAdminUiState,
    onEvent: (AccountAdminUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToPlans: () -> Unit,
    onBottomTabSelected: (Int) -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }

    uiState.userMessage?.let { messageRes ->
        val message = stringResource(id = messageRes)
        LaunchedEffect(messageRes) {
            snackbarHostState.showSnackbar(message)
            onEvent(AccountAdminUiEvent.OnUserMessageConsumed)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.account_admin_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        bottomBar = {
            NearHomeBottomBar(
                selectedTab = 3,
                onTabSelected = onBottomTabSelected
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading || uiState.userData == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val userData = uiState.userData

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // ==================== DATOS DE ACCESO ====================
                SectionHeader(title = stringResource(id = R.string.section_access_data))

                OptionRowItem(
                    icon = Icons.Outlined.Email,
                    title = stringResource(id = R.string.label_email),
                    subtitle = userData.email,
                    onClick = { onEvent(AccountAdminUiEvent.OnOptionClick) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))

                OptionRowItem(
                    icon = Icons.Outlined.Smartphone,
                    title = stringResource(id = R.string.label_phone),
                    subtitle = userData.phone,
                    onClick = { onEvent(AccountAdminUiEvent.OnOptionClick) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))

                OptionRowItem(
                    icon = Icons.Outlined.Lock,
                    title = stringResource(id = R.string.label_password),
                    subtitle = stringResource(id = R.string.masked_password),
                    onClick = { onEvent(AccountAdminUiEvent.OnOptionClick) }
                )

                // ==================== MEMBRESÍA Y PAGOS ====================
                if (userData.role == UserRole.VENDEDOR || userData.role == UserRole.COMPRADOR) {
                    SectionHeader(title = stringResource(id = R.string.section_membership))

                    if (userData.role == UserRole.VENDEDOR) {
                        val statusText = stringResource(
                            id = R.string.seller_plan_status_format,
                            userData.planName ?: "",
                            userData.planStatus ?: ""
                        )
                        val renewalText = stringResource(
                            id = R.string.seller_renewal_format,
                            userData.renewalDate ?: ""
                        )

                        OptionRowItem(
                            icon = Icons.Outlined.PublishedWithChanges,
                            title = stringResource(id = R.string.label_current_plan),
                            subtitle = "$statusText\n$renewalText",
                            iconBackgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            iconTintColor = MaterialTheme.colorScheme.onSurface,
                            onClick = onNavigateToPlans
                        )
                    } else if (userData.role == UserRole.COMPRADOR) {
                        OptionRowItem(
                            icon = Icons.Outlined.CardMembership,
                            title = stringResource(id = R.string.label_current_plan),
                            subtitle = "${stringResource(id = R.string.buyer_no_plan)}\n${stringResource(id = R.string.buyer_plan_subtitle)}",
                            iconBackgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            iconTintColor = MaterialTheme.colorScheme.onSurface,
                            onClick = onNavigateToPlans
                        )
                    }
                }

                // ==================== GESTIÓN DE LA CUENTA ====================
                if (userData.role != UserRole.SUPER_ADMIN) {
                    SectionHeader(title = stringResource(id = R.string.section_account_management))

                    OptionRowItem(
                        icon = Icons.Outlined.PowerSettingsNew,
                        title = stringResource(id = R.string.deactivate_account_title),
                        subtitle = stringResource(id = R.string.deactivate_account_subtitle),
                        iconBackgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        iconTintColor = MaterialTheme.colorScheme.onSurface,
                        onClick = { onEvent(AccountAdminUiEvent.OnDeactivateRequested) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))

                    OptionRowItem(
                        icon = Icons.Outlined.Delete,
                        title = stringResource(id = R.string.delete_account_title),
                        subtitle = stringResource(id = R.string.delete_account_subtitle),
                        titleColor = MaterialTheme.colorScheme.error,
                        iconBackgroundColor = MaterialTheme.colorScheme.errorContainer,
                        iconTintColor = MaterialTheme.colorScheme.error,
                        onClick = { onEvent(AccountAdminUiEvent.OnDeleteRequested) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // ==================== DIÁLOGOS DE CONFIRMACIÓN ====================
    if (uiState.showDeactivateDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(AccountAdminUiEvent.OnDeactivateDismissed) },
            title = { Text(text = stringResource(id = R.string.dialog_deactivate_title)) },
            text = { Text(text = stringResource(id = R.string.dialog_deactivate_text)) },
            confirmButton = {
                TextButton(onClick = { onEvent(AccountAdminUiEvent.OnDeactivateConfirmed) }) {
                    Text(text = stringResource(id = R.string.confirm_deactivate))
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(AccountAdminUiEvent.OnDeactivateDismissed) }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            }
        )
    }

    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(AccountAdminUiEvent.OnDeleteDismissed) },
            title = {
                Text(
                    text = stringResource(id = R.string.dialog_delete_title),
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = stringResource(id = R.string.dialog_delete_text))
                    OutlinedTextField(
                        value = uiState.deleteInputText,
                        onValueChange = { onEvent(AccountAdminUiEvent.OnDeleteInputChanged(it)) },
                        placeholder = { Text(text = stringResource(id = R.string.dialog_delete_placeholder)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = uiState.isDeleteButtonEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    onClick = { onEvent(AccountAdminUiEvent.OnDeleteConfirmed) }
                ) {
                    Text(text = stringResource(id = R.string.confirm_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(AccountAdminUiEvent.OnDeleteDismissed) }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun OptionRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    iconBackgroundColor: Color = Color.Transparent,
    iconTintColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(color = iconBackgroundColor, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTintColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(20.dp)
        )
    }
}