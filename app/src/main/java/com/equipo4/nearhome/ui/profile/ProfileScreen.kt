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
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo4.nearhome.ui.common.NearHomeBottomBar
import kotlinx.coroutines.launch

private val SectionBg = Color(0xFFF2F2F2)
private val SectionText = Color(0xFF4B4B4B)
private val DividerColor = Color(0xFFE5E7EB)
private val TextDark = Color(0xFF1F2937)
private val ArrowGray = Color(0xFF9CA3AF)
private val ChipBg = Color(0xFFE6F2EA)
private val ChipText = Color(0xFF2E7D4F)
private val AvatarBg = Color(0xFFE5E7EB)
private val NavyColor = Color(0xFF0B2C4D)
private val SelectedRowBg = Color(0xFFEDEDED)
private val SubtitleGray = Color(0xFF6B7280)

private enum class SettingSheetType { LANGUAGE, DARK_MODE, ROLE }

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onMyListingsClick: () -> Unit = {},
    onAccountClick: () -> Unit = {},
    onAdminPanelClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onBottomTabSelected: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var openSheet by remember { mutableStateOf<SettingSheetType?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = { NearHomeBottomBar(selectedTab = 3, onTabSelected = onBottomTabSelected) },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            ProfileHeader(
                uiState = uiState,
                onRoleClick = { openSheet = SettingSheetType.ROLE }
            )

            if (uiState.role == UserRole.ADMIN) {
                SectionHeader("Administración")
                ProfileRow(
                    icon = Icons.Outlined.Security,
                    label = "Panel de administración",
                    showDivider = true,
                    onClick = onAdminPanelClick
                )
            }

            SectionHeader("Perfil")
            if (uiState.canManageListings) {
                ProfileRow(Icons.Outlined.Verified, "Mis anuncios", showDivider = true, onClick = onMyListingsClick)
            }
            ProfileRow(Icons.Outlined.Settings, "Administración de la cuenta", onClick = onAccountClick)

            SectionHeader("Ajustes")
            SettingRow(
                icon = Icons.Outlined.Language,
                label = "Idioma",
                value = uiState.language,
                showDivider = true,
                onClick = { openSheet = SettingSheetType.LANGUAGE }
            )
            SettingRow(
                icon = Icons.Outlined.DarkMode,
                label = "Modo oscuro",
                value = uiState.darkModeLabel,
                onClick = { openSheet = SettingSheetType.DARK_MODE }
            )

            SectionHeader("Información")
            ProfileRow(Icons.Outlined.Description, "Terminos y condiciones", showDivider = true, onClick = onTermsClick)
            ProfileRow(Icons.Outlined.MenuBook, "Politicas y privacidad", onClick = onPrivacyClick)

            SectionHeader("Sesión")
            ProfileRow(Icons.AutoMirrored.Outlined.Logout, "Cerrar sesión", showDivider = true, onClick = { showLogoutDialog = true })

            Spacer(Modifier.height(16.dp))
        }
    }

    when (openSheet) {
        SettingSheetType.LANGUAGE -> SettingSheet(
            title = "Idioma",
            subtitle = "Selecciona el idioma de la aplicación.",
            options = LanguageOptions,
            selected = uiState.language,
            onSelect = viewModel::onLanguageSelected,
            onDismiss = { openSheet = null }
        )
        SettingSheetType.DARK_MODE -> SettingSheet(
            title = "Modo oscuro",
            subtitle = "Elige cómo quieres ver la aplicación.",
            options = listOf("Activado", "Desactivado"),
            selected = uiState.darkModeLabel,
            onSelect = { viewModel.onDarkModeSelected(it == "Activado") },
            onDismiss = { openSheet = null }
        )
        SettingSheetType.ROLE -> SettingSheet(
            title = "Rol de usuario",
            subtitle = "Cambia el rol para probar vistas de Comprador, Vendedor o Administrador.",
            options = UserRole.values().map { it.label },
            selected = uiState.role.label,
            onSelect = { label ->
                val selectedRole = UserRole.values().firstOrNull { it.label == label } ?: UserRole.ADMIN
                viewModel.onRoleSelected(selectedRole)
            },
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
private fun ProfileHeader(uiState: ProfileUiState, onRoleClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(60.dp).background(AvatarBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Person, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(34.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(uiState.userName, fontSize = 16.sp, color = TextDark)
            Spacer(Modifier.height(4.dp))
            Surface(
                color = ChipBg,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.clickable(onClick = onRoleClick)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        uiState.role.label,
                        color = ChipText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Cambiar rol",
                        tint = ChipText,
                        modifier = Modifier.size(14.dp)
                    )
                }
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
        color = SectionText,
        modifier = Modifier
            .fillMaxWidth()
            .background(SectionBg)
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
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .height(52.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = TextDark, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(14.dp))
            Text(label, fontSize = 14.sp, color = TextDark, modifier = Modifier.weight(1f))
            trailing?.invoke()
        }
        if (showDivider) {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = DividerColor)
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
                Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextDark)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = ArrowGray)
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
        containerColor = Color.White,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 8.dp)) {
            Box(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 20.dp)) {
                Text(
                    title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.align(Alignment.Center)
                )
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = TextDark,
                    modifier = Modifier.align(Alignment.CenterEnd).size(22.dp).noRippleClickable { close() }
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                subtitle,
                fontSize = 12.sp,
                color = SubtitleGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(16.dp))
            options.forEach { option ->
                val isSelected = option == selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isSelected) SelectedRowBg else Color.White)
                        .noRippleClickable { close { onSelect(option) } }
                        .height(52.dp)
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(option, fontSize = 14.sp, color = TextDark, modifier = Modifier.weight(1f))
                    if (isSelected) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = TextDark, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LogoutDialog(onCancel: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onCancel) {
        Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "¿Estás seguro de que quieres cerrar sesión?",
                    fontSize = 14.sp,
                    color = TextDark,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB0B5BC), contentColor = Color.White)
                    ) { Text("No", fontSize = 13.sp) }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyColor, contentColor = Color.White)
                    ) { Text("Cerrar sesión", fontSize = 13.sp, maxLines = 1) }
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
