package com.equipo4.nearhome.ui.auth.forgot_password

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equipo4.nearhome.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val cs = MaterialTheme.colorScheme

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissMessages()
        }
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = cs.background
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val screenHeight = maxHeight

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = screenHeight)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Parte Superior (Flecha de retorno)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.forgot_back),
                            tint = cs.onSurface
                        )
                    }
                }

                // 2. Parte Central (Formulario centrado)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.forgot_title),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = cs.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(36.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.forgot_email_label),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = cs.onSurface
                        )
                        Text(
                            text = stringResource(R.string.forgot_back),
                            fontSize = 13.sp,
                            color = cs.primary,
                            modifier = Modifier.clickable { onNavigateToLogin() }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.onEmailChanged(it) },
                        placeholder = {
                            Text(
                                text = "email@gmail.com",
                                color = cs.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = cs.surfaceVariant,
                            unfocusedContainerColor = cs.surfaceVariant,
                            focusedBorderColor = cs.primary,
                            unfocusedBorderColor = cs.outlineVariant,
                            focusedTextColor = cs.onSurface,
                            unfocusedTextColor = cs.onSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val helperPrefix = stringResource(R.string.forgot_helper_prefix)
                    val helperAction = stringResource(R.string.forgot_reset_button)
                    val helperSuffix = stringResource(R.string.forgot_helper_suffix)
                    val helperText = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = cs.onSurfaceVariant)) {
                            append(helperPrefix)
                        }
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = cs.onSurface)) {
                            append(helperAction)
                        }
                        withStyle(style = SpanStyle(color = cs.onSurfaceVariant)) {
                            append(helperSuffix)
                        }
                    }

                    Text(
                        text = helperText,
                        fontSize = 12.sp,
                        color = cs.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.resetPassword() },
                        enabled = uiState.isEmailValid && !uiState.isLoading,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = cs.primary,
                            disabledContainerColor = cs.primary.copy(alpha = 0.5f),
                            contentColor = cs.onPrimary,
                            disabledContentColor = cs.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = cs.onPrimary,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.forgot_reset_button),
                                color = cs.onPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // 3. Parte Inferior (Divisor y link de registro)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HorizontalDivider(
                        color = cs.outlineVariant,
                        thickness = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    val noAccountText = stringResource(R.string.forgot_no_account)
                    val registerText = stringResource(R.string.forgot_register)
                    val signUpAnnotatedText = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = cs.onSurfaceVariant)) {
                            append(noAccountText)
                        }
                        withStyle(style = SpanStyle(color = cs.primary, fontWeight = FontWeight.Medium)) {
                            append(registerText)
                        }
                    }

                    Text(
                        text = signUpAnnotatedText,
                        fontSize = 13.sp,
                        modifier = Modifier.clickable { onNavigateToSignUp() }
                    )
                }
            }
        }
    }
}
