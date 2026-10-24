package com.equipo4.nearhome.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo4.nearhome.R
import com.equipo4.nearhome.ui.common.NearHomeBottomBar
import kotlinx.coroutines.launch

private enum class SettingSheetType { LANGUAGE, DARK_MODE }

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onMyListingsClick: () -> Unit = {},
    onAccountClick: () -> Unit = {},
    onAdminPanelClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onSessionExpired: () -> Unit = {},
    onBottomTabSelected: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.sessionExpired.collect { onSessionExpired() }
    }

    ProfileContent(
        uiState = uiState,
        onLanguageSelected = viewModel::onLanguageSelected,
        onDarkModeSelected = viewModel::onDarkModeSelected,
        onMyListingsClick = onMyListingsClick,
        onAccountClick = onAccountClick,
        onAdminPanelClick = onAdminPanelClick,
        onTermsClick = onTermsClick,
        onPrivacyClick = onPrivacyClick,
        onLogoutClick = onLogoutClick,
        onBottomTabSelected = onBottomTabSelected
    )
}

@Composable
fun ProfileContent(
    uiState: ProfileUiState,
    onLanguageSelected: (String) -> Unit = {},
    onDarkModeSelected: (Boolean) -> Unit = {},
    onMyListingsClick: () -> Unit = {},
    onAccountClick: () -> Unit = {},
    onAdminPanelClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onBottomTabSelected: (Int) -> Unit = {}
) {
    var openSheet by remember { mutableStateOf<SettingSheetType?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            NearHomeBottomBar(
                selectedTab = 3,
                onTabSelected = { index ->
                    if (index == 3) {
                        scope.launch {
                            scrollState.animateScrollTo(0)
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
                .verticalScroll(scrollState)
        ) {
            ProfileHeader(uiState = uiState)

            SectionHeader(stringResource(R.string.profile_section_profile))
            if (uiState.showMyListings) {
                ProfileRow(
                    Icons.Outlined.Verified, stringResource(R.string.profile_my_listings),
                    showDivider = uiState.showAccountAdmin || uiState.showAdminPanel,
                    onClick = onMyListingsClick
                )
            }
            if (uiState.showAccountAdmin) {
                ProfileRow(
                    Icons.Outlined.Settings, stringResource(R.string.profile_account_admin),
                    showDivider = uiState.showAdminPanel, onClick = onAccountClick
                )
            }
            if (uiState.showAdminPanel) {
                ProfileRow(
                    icon = Icons.Outlined.AdminPanelSettings,
                    label = stringResource(R.string.profile_admin_panel),
                    onClick = onAdminPanelClick
                )
            }

            SectionHeader(stringResource(R.string.profile_section_settings))
            SettingRow(
                icon = Icons.Outlined.Language,
                label = stringResource(R.string.profile_language),
                value = uiState.language,
                showDivider = true,
                onClick = { openSheet = SettingSheetType.LANGUAGE }
            )
            SettingRow(
                icon = Icons.Outlined.DarkMode,
                label = stringResource(R.string.profile_dark_mode),
                value = uiState.darkModeLabel,
                onClick = { openSheet = SettingSheetType.DARK_MODE }
            )

            SectionHeader(stringResource(R.string.profile_section_info))
            ProfileRow(Icons.Outlined.Description, stringResource(R.string.profile_terms), showDivider = true, onClick = onTermsClick)
            ProfileRow(Icons.Outlined.MenuBook, stringResource(R.string.profile_privacy), onClick = onPrivacyClick)

            SectionHeader(stringResource(R.string.profile_section_session))
            ProfileRow(Icons.AutoMirrored.Outlined.Logout, stringResource(R.string.profile_logout), showDivider = true, onClick = { showLogoutDialog = true })

            Spacer(Modifier.height(16.dp))
        }
    }

    val darkOnLabel = stringResource(R.string.dark_mode_on)
    val darkOffLabel = stringResource(R.string.dark_mode_off)
    when (openSheet) {
        SettingSheetType.LANGUAGE -> SettingSheet(
            title = stringResource(R.string.profile_language),
            subtitle = stringResource(R.string.profile_language_subtitle),
            options = LanguageOptions,
            selected = uiState.language,
            onSelect = onLanguageSelected,
            onDismiss = { openSheet = null }
        )
        SettingSheetType.DARK_MODE -> SettingSheet(
            title = stringResource(R.string.profile_dark_mode),
            subtitle = stringResource(R.string.profile_dark_mode_subtitle),
            options = listOf(darkOnLabel, darkOffLabel),
            selected = if (uiState.darkModeEnabled) darkOnLabel else darkOffLabel,
            onSelect = { onDarkModeSelected(it == darkOnLabel) },
            onDismiss = { openSheet = null }
        )
        null -> Unit
    }

    if (showLogoutDialog) {
        LogoutDialog(
            onCancel = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                onLogoutClick()
            }
        )
    }
}

@Composable
private fun ProfileHeader(uiState: ProfileUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(34.dp)
            )
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                uiState.userName.ifBlank { stringResource(R.string.profile_default_name) },
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            // El rol solo se muestra. No es clickable.
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    stringResource(uiState.role.labelRes),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    )
}

@Composable
private fun ProfileRow(
    icon: ImageVector,
    label: String,
    showDivider: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .height(52.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(14.dp))
            Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            trailing?.invoke()
        }
        if (showDivider) {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    label: String,
    value: String,
    showDivider: Boolean = false,
    onClick: () -> Unit
) {
    ProfileRow(
        icon = icon,
        label = label,
        showDivider = showDivider,
        onClick = onClick,
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingSheet(
    title: String,
    subtitle: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    fun close(afterClose: () -> Unit = {}) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            afterClose()
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 8.dp)) {
            Box(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 20.dp)) {
                Text(
                    title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Center)
                )
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.common_close),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.CenterEnd).size(22.dp).noRippleClickable { close() }
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(16.dp))
            options.forEach { option ->
                val isSelected = option == selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface)
                        .noRippleClickable { close { onSelect(option) } }
                        .height(52.dp)
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(option, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                    if (isSelected) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LogoutDialog(onCancel: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onCancel) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(R.string.logout_confirm_message),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) { Text(stringResource(R.string.logout_cancel), fontSize = 13.sp) }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) { Text(stringResource(R.string.profile_logout), fontSize = 13.sp, maxLines = 1) }
                }
            }
        }
    }
}

@Composable
private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier =
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )