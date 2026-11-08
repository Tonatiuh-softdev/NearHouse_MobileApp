package com.equipo4.nearhome.ui.auth.recover

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoverPasswordScreen(
    viewModel: RecoverPasswordViewModel,
    prefilledEmail: String = "",
    onNavigateBack: () -> Unit,
    onSuccessFinished: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(prefilledEmail) {
        if (prefilledEmail.isNotBlank()) {
            viewModel.setPrefilledEmail(prefilledEmail)
        }
    }

    var isNewPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (uiState.step) {
                            RecoverStep.REQUEST_CODE -> "Recuperar contraseña"
                            RecoverStep.VERIFY_CODE -> "Verifica tu correo"
                            RecoverStep.NEW_PASSWORD -> "Nueva contraseña"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.step == RecoverStep.VERIFY_CODE) {
                            viewModel.onEvent(RecoverPasswordUiEvent.OnReturnToEmailStepRequested)
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            when (uiState.step) {
                // PASO 1: RECUPERAR CONTRASEÑA (SOLICITAR CÓDIGO)
                RecoverStep.REQUEST_CODE -> {
                    Text(
                        text = "Ingresa el correo electrónico asociado a tu cuenta para recibir un código de recuperación.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = uiState.emailInput,
                        onValueChange = { viewModel.onEvent(RecoverPasswordUiEvent.OnEmailChanged(it)) },
                        label = { Text("Correo electrónico") },
                        singleLine = true,
                        enabled = !uiState.isSendingCode,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                        isError = uiState.emailError != null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (uiState.emailError != null) {
                        Text(
                            text = uiState.emailError!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(uiState.emailInput.trim()).matches()

                    Button(
                        onClick = { viewModel.onEvent(RecoverPasswordUiEvent.OnRequestCodeSubmitted) },
                        enabled = isEmailValid && !uiState.isSendingCode,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (uiState.isSendingCode) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Enviar código", fontSize = 16.sp)
                        }
                    }
                }

                // PASO 2: VERIFICA TU CORREO
                RecoverStep.VERIFY_CODE -> {
                    Text(
                        text = "Si el correo está registrado, te enviamos un código a ${uiState.emailInput.trim()}. Revisa también la carpeta de spam.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val fullCode = uiState.otpCode.joinToString("")
                    val isVerifyEnabled = fullCode.length == 6 && !uiState.isVerifyingCode
                    val focusRequesters = remember { List(6) { FocusRequester() } }

                    // 6 casillas OTP
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (i in 0 until 6) {
                            OutlinedTextField(
                                value = uiState.otpCode.getOrElse(i) { "" },
                                onValueChange = { digit ->
                                    val cleanDigit = digit.filter { it.isDigit() }.takeLast(1)
                                    viewModel.onEvent(RecoverPasswordUiEvent.OnOtpDigitChanged(i, cleanDigit))
                                    if (cleanDigit.isNotEmpty() && i < 5) {
                                        focusRequesters[i + 1].requestFocus()
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .width(46.dp)
                                    .height(56.dp)
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

                    Button(
                        onClick = { viewModel.onEvent(RecoverPasswordUiEvent.OnVerifyCodeSubmitted) },
                        enabled = isVerifyEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (uiState.isVerifyingCode) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Verificar", fontSize = 16.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { viewModel.onEvent(RecoverPasswordUiEvent.OnReturnToEmailStepRequested) },
                            enabled = !uiState.isVerifyingCode
                        ) {
                            Text("Cambiar correo")
                        }

                        TextButton(
                            onClick = { viewModel.onEvent(RecoverPasswordUiEvent.OnResendCodeRequested) },
                            enabled = uiState.canResendCode && !uiState.isVerifyingCode
                        ) {
                            if (uiState.canResendCode) {
                                Text("Reenviar código")
                            } else {
                                Text("Reenviar en ${uiState.resendCountdown}s")
                            }
                        }
                    }
                }

                // PASO 3: NUEVA CONTRASEÑA
                RecoverStep.NEW_PASSWORD -> {
                    Text(
                        text = "Crea una nueva contraseña para tu cuenta.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = uiState.newPasswordInput,
                        onValueChange = { viewModel.onEvent(RecoverPasswordUiEvent.OnNewPasswordChanged(it)) },
                        label = { Text("Nueva contraseña") },
                        singleLine = true,
                        enabled = !uiState.isSavingPassword,
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.confirmPasswordInput,
                        onValueChange = { viewModel.onEvent(RecoverPasswordUiEvent.OnConfirmPasswordChanged(it)) },
                        label = { Text("Confirmar nueva contraseña") },
                        singleLine = true,
                        enabled = !uiState.isSavingPassword,
                        visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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

                    if (uiState.passwordError != null) {
                        Text(
                            text = uiState.passwordError!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.onEvent(RecoverPasswordUiEvent.OnSaveNewPasswordSubmitted, onSuccessFinished) },
                        enabled = uiState.isNewPasswordFormValid && !uiState.isSavingPassword,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (uiState.isSavingPassword) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Guardar", fontSize = 16.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
