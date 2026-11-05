package com.equipo4.nearhome.ui.profile.account

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo4.nearhome.R
import com.equipo4.nearhome.ui.common.AccountAdminSkeleton
import com.equipo4.nearhome.ui.common.NearHomeBottomBar
import com.equipo4.nearhome.ui.profile.UserRole

@Composable
fun AccountAdminRoute(
    viewModel: AccountAdminViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPlans: () -> Unit,
    onNavigateToLogin: (String) -> Unit = {},
    onBottomTabSelected: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.isDeactivatedSuccess) {
        if (uiState.isDeactivatedSuccess) {
            val msg = "Tu cuenta está desactivada. Para volver, solo inicia sesión"
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            onNavigateToLogin(msg)
        }
    }

    LaunchedEffect(uiState.snackbarSuccessMessage) {
        uiState.snackbarSuccessMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.onEvent(AccountAdminUiEvent.OnSnackbarMessageConsumed)
        }
    }

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
    var isCurrentPasswordVisible by remember { mutableStateOf(false) }
    var isNewPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

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
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
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
        // Carga inicial -> Muestra Skeleton de Administración de Cuenta
        if (uiState.isLoading || uiState.userData == null) {
            AccountAdminSkeleton(modifier = Modifier.padding(paddingValues))
        } else {
            val userData = uiState.userData

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // ==================== DATOS DE ACCESO ====================
                SectionHeader(title = stringResource(id = R.string.section_access_data))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column {
                        OptionRowItem(
                            icon = Icons.Outlined.Email,
                            title = stringResource(id = R.string.label_email),
                            subtitle = userData.email,
                            useIconContainer = true,
                            iconBackgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                            iconTintColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = { onEvent(AccountAdminUiEvent.OnOptionClick) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 72.dp, end = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            thickness = 0.8.dp
                        )

                        OptionRowItem(
                            icon = Icons.Outlined.Smartphone,
                            title = stringResource(id = R.string.label_phone),
                            subtitle = userData.phone,
                            useIconContainer = true,
                            iconBackgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                            iconTintColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = { onEvent(AccountAdminUiEvent.OnOptionClick) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 72.dp, end = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            thickness = 0.8.dp
                        )

                        OptionRowItem(
                            icon = Icons.Outlined.Lock,
                            title = stringResource(id = R.string.label_password),
                            subtitle = if (userData.tieneContrasena) {
                                stringResource(id = R.string.masked_password)
                            } else {
                                "Entras con Google. Crea una contraseña para entrar también con tu correo."
                            },
                            useIconContainer = true,
                            iconBackgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                            iconTintColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = { onEvent(AccountAdminUiEvent.OnPasswordOptionClick) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ==================== MEMBRESÍA Y PAGOS ====================
                SectionHeader(title = stringResource(id = R.string.section_membership))

                val planName = userData.planName

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    OptionRowItem(
                        icon = Icons.Outlined.PublishedWithChanges,
                        title = stringResource(id = R.string.label_current_plan),
                        subtitle = if (planName != null) {
                            val statusText = if (!userData.planStatus.isNullOrEmpty()) " (${userData.planStatus})" else ""
                            "$planName$statusText"
                        } else {
                            stringResource(id = R.string.buyer_no_plan)
                        },
                        secondLine = when {
                            planName == null -> stringResource(id = R.string.buyer_plan_subtitle)
                            userData.planExpired -> stringResource(id = R.string.plan_expired_format, userData.renewalDate.orEmpty())
                            else -> stringResource(id = R.string.plan_expires_format, userData.renewalDate.orEmpty())
                        },
                        secondLineColor = if (userData.planExpired) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        useIconContainer = true,
                        iconBackgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                        iconTintColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = onNavigateToPlans
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ==================== GESTIÓN DE LA CUENTA ====================
                if (userData.role != UserRole.SUPER_ADMIN) {
                    SectionHeader(title = stringResource(id = R.string.section_account_management))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column {
                            OptionRowItem(
                                icon = Icons.Outlined.PowerSettingsNew,
                                title = stringResource(id = R.string.deactivate_account_title),
                                subtitle = stringResource(id = R.string.deactivate_account_subtitle),
                                useIconContainer = true,
                                iconBackgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                                iconTintColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                onClick = { onEvent(AccountAdminUiEvent.OnDeactivateRequested) }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 72.dp, end = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                thickness = 0.8.dp
                            )

                            OptionRowItem(
                                icon = Icons.Outlined.Delete,
                                title = stringResource(id = R.string.delete_account_title),
                                subtitle = stringResource(id = R.string.delete_account_subtitle),
                                titleColor = MaterialTheme.colorScheme.error,
                                useIconContainer = true,
                                iconBackgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                                iconTintColor = MaterialTheme.colorScheme.error,
                                onClick = { onEvent(AccountAdminUiEvent.OnDeleteRequested) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // ==================== DIÁLOGOS Y PASOS DEL FLUJO DE CONTRASEÑA ====================

    // PASO 1: AVISO AL TOCAR "CONTRASEÑA"
    if (uiState.passwordStep == PasswordFlowStep.CONFIRM_SEND_CODE) {
        val hasPass = uiState.userData?.tieneContrasena == true
        AlertDialog(
            onDismissRequest = {
                if (!uiState.isOtpLoading) onEvent(AccountAdminUiEvent.OnPasswordFlowDismissed)
            },
            title = {
                Text(
                    text = if (hasPass) "¿Quieres cambiar tu contraseña?" else "¿Quieres crear una contraseña?"
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (hasPass) {
                            "Te enviaremos un código a tu correo para confirmar que eres tú."
                        } else {
                            "Tu cuenta entra con Google. Si creas una contraseña, también podrás entrar con tu correo y contraseña; Google seguirá funcionando. Te enviaremos un código a tu correo."
                        }
                    )
                    if (uiState.otpErrorMessage != null) {
                        Text(
                            text = uiState.otpErrorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { onEvent(AccountAdminUiEvent.OnRequestCodeConfirmed) },
                    enabled = !uiState.isOtpLoading
                ) {
                    if (uiState.isOtpLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Enviar código")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onEvent(AccountAdminUiEvent.OnPasswordFlowDismissed) },
                    enabled = !uiState.isOtpLoading
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // PASO 2: VERIFICA TU CORREO
    if (uiState.passwordStep == PasswordFlowStep.VERIFY_CODE) {
        val fullCode = uiState.otpCode.joinToString("")
        val isVerifyEnabled = fullCode.length == 6 && !uiState.isOtpLoading
        val focusRequesters = remember { List(6) { FocusRequester() } }

        AlertDialog(
            onDismissRequest = {
                if (!uiState.isOtpLoading) onEvent(AccountAdminUiEvent.OnPasswordFlowDismissed)
            },
            title = { Text("Verifica tu correo") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Escribe el código que enviamos a ${uiState.maskedEmail.ifBlank { uiState.userData?.email.orEmpty() }}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 6 casillas OTP
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (i in 0 until 6) {
                            OutlinedTextField(
                                value = uiState.otpCode.getOrElse(i) { "" },
                                onValueChange = { digit ->
                                    val cleanDigit = digit.filter { it.isDigit() }.takeLast(1)
                                    onEvent(AccountAdminUiEvent.OnOtpDigitChanged(i, cleanDigit))
                                    if (cleanDigit.isNotEmpty() && i < 5) {
                                        focusRequesters[i + 1].requestFocus()
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .width(44.dp)
                                    .height(54.dp)
                                    .focusRequester(focusRequesters[i])
                            )
                        }
                    }

                    if (uiState.otpErrorMessage != null) {
                        Text(
                            text = uiState.otpErrorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    // Enlace Reenviar código
                    TextButton(
                        onClick = { onEvent(AccountAdminUiEvent.OnResendCodeRequested) },
                        enabled = uiState.canResendCode && !uiState.isOtpLoading
                    ) {
                        if (uiState.canResendCode) {
                            Text("Reenviar código")
                        } else {
                            Text("Reenviar código en ${uiState.resendCountdown}s")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { onEvent(AccountAdminUiEvent.OnVerifyCodeSubmitted) },
                    enabled = isVerifyEnabled
                ) {
                    if (uiState.isOtpLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Verificar")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onEvent(AccountAdminUiEvent.OnPasswordFlowDismissed) },
                    enabled = !uiState.isOtpLoading
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // PASO 3: NUEVA CONTRASEÑA
    if (uiState.passwordStep == PasswordFlowStep.NEW_PASSWORD) {
        val hasPass = uiState.userData?.tieneContrasena == true

        AlertDialog(
            onDismissRequest = {
                if (!uiState.isPasswordSaving) onEvent(AccountAdminUiEvent.OnPasswordFlowDismissed)
            },
            title = {
                Text(
                    text = if (hasPass) "Nueva contraseña" else "Crear contraseña"
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (hasPass) {
                        OutlinedTextField(
                            value = uiState.currentPasswordInput,
                            onValueChange = { onEvent(AccountAdminUiEvent.OnCurrentPasswordChanged(it)) },
                            label = { Text("Contraseña actual") },
                            singleLine = true,
                            enabled = !uiState.isPasswordSaving,
                            visualTransformation = if (isCurrentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isCurrentPasswordVisible = !isCurrentPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isCurrentPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Mostrar u ocultar contraseña"
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                            isError = uiState.currentPasswordError != null,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (uiState.currentPasswordError != null) {
                            Text(
                                text = uiState.currentPasswordError!!,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }
                    }

                    OutlinedTextField(
                        value = uiState.newPasswordInput,
                        onValueChange = { onEvent(AccountAdminUiEvent.OnNewPasswordChanged(it)) },
                        label = { Text("Nueva contraseña") },
                        singleLine = true,
                        enabled = !uiState.isPasswordSaving,
                        visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isNewPasswordVisible = !isNewPasswordVisible }) {
                                Icon(
                                    imageVector = if (isNewPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Mostrar u ocultar contraseña"
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "mínimo 6 caracteres",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = uiState.confirmPasswordInput,
                        onValueChange = { onEvent(AccountAdminUiEvent.OnConfirmPasswordChanged(it)) },
                        label = { Text("Confirmar nueva contraseña") },
                        singleLine = true,
                        enabled = !uiState.isPasswordSaving,                        visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Mostrar u ocultar contraseña"
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (uiState.passwordSaveError != null) {
                        Text(
                            text = uiState.passwordSaveError!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { onEvent(AccountAdminUiEvent.OnSaveNewPasswordSubmitted) },
                    enabled = uiState.isNewPasswordFormValid && !uiState.isPasswordSaving
                ) {
                    if (uiState.isPasswordSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Guardar")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onEvent(AccountAdminUiEvent.OnPasswordFlowDismissed) },
                    enabled = !uiState.isPasswordSaving
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ==================== DIÁLOGOS DE DESACTIVACIÓN Y ELIMINACIÓN ====================
    if (uiState.showDeactivateDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!uiState.isDeactivating) {
                    onEvent(AccountAdminUiEvent.OnDeactivateDismissed)
                }
            },
            title = { Text(text = stringResource(id = R.string.dialog_deactivate_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = stringResource(id = R.string.dialog_deactivate_text))

                    OutlinedTextField(
                        value = uiState.deactivatePasswordInput,
                        onValueChange = { onEvent(AccountAdminUiEvent.OnDeactivatePasswordChanged(it)) },
                        label = { Text("Contraseña actual") },
                        singleLine = true,
                        enabled = !uiState.isDeactivating,
                        visualTransformation = if (isCurrentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isCurrentPasswordVisible = !isCurrentPasswordVisible }) {
                                Icon(
                                    imageVector = if (isCurrentPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Mostrar u ocultar contraseña"
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (uiState.deactivateError != null) {
                        Text(
                            text = uiState.deactivateError,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = uiState.deactivatePasswordInput.isNotBlank() && !uiState.isDeactivating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    onClick = { onEvent(AccountAdminUiEvent.OnDeactivateConfirmed()) }
                ) {
                    if (uiState.isDeactivating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onError,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(text = stringResource(id = R.string.confirm_deactivate))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !uiState.isDeactivating,
                    onClick = { onEvent(AccountAdminUiEvent.OnDeactivateDismissed) }
                ) {
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
                        onValueChange = { newText ->
                            onEvent(AccountAdminUiEvent.OnDeleteInputChanged(newText))
                        },
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
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        ),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
    )
}

@Composable
private fun OptionRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    secondLine: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    secondLineColor: Color = subtitleColor,
    useIconContainer: Boolean = false,
    iconBackgroundColor: Color = Color.Transparent,
    iconTintColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (useIconContainer) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color = iconBackgroundColor, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTintColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
        } else {
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTintColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                color = subtitleColor
            )
            if (secondLine != null) {
                Text(
                    text = secondLine,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    color = secondLineColor
                )
            }
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