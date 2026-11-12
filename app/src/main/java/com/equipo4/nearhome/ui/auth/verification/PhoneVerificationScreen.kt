package com.equipo4.nearhome.ui.auth.verification

import com.equipo4.nearhome.util.LogroSonidoEffect
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo4.nearhome.R
import com.equipo4.nearhome.network.LoginResponse
import kotlinx.coroutines.delay

import androidx.compose.ui.res.stringResource
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.platform.LocalContext

// Los demás colores salen del tema (MaterialTheme.colorScheme) para que funcione el modo oscuro.
// Verde de la palomita: se ve bien en claro y en oscuro
private val SuccessGreenCircle = Color(0xFF52C41A)
// Fondo de la pantalla de éxito: verde suave en claro, verde muy oscuro en oscuro
private val SuccessBgLight = Color(0xFFEFF7EB)
private val SuccessBgDark = Color(0xFF14261A)

@Composable
fun PhoneVerificationScreen(
    phoneNumber: String = "",
    viewModel: PhoneVerificationViewModel = viewModel(),
    onNavigateToLogin: () -> Unit = {},
    // Registro con Google: ya hay sesión, se entra directo a la app
    onSessionStarted: (LoginResponse) -> Unit = {}
) {
    // Firebase necesita la Activity para mandar el SMS
    val activity = LocalContext.current.findActivity()

    LaunchedEffect(phoneNumber) {
        if (phoneNumber.isNotEmpty()) {
            viewModel.setPhoneNumber(phoneNumber)
        }
    }

    // Al abrir la pantalla se manda el primer SMS
    LaunchedEffect(activity) {
        activity?.let { viewModel.enviarPrimerCodigo(it) }
    }

    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isVerified) {
        val sesionNueva = uiState.sesionNueva
        LaunchedEffect(Unit) {
            delay(2000L)
            if (sesionNueva != null) onSessionStarted(sesionNueva) else onNavigateToLogin()
        }

        VerifiedSuccessScreen()
    } else {
        VerificationInputContent(
            uiState = uiState,
            onOtpCodeChanged = viewModel::onOtpCodeChanged,
            onVerifyClicked = viewModel::onVerifyClicked,
            onResendSmsClicked = { activity?.let { viewModel.onResendSmsClicked(it) } }
        )
    }
}

/** Busca la Activity detrás del Context de Compose. */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
private fun VerificationInputContent(
    uiState: PhoneVerificationUiState,
    onOtpCodeChanged: (String) -> Unit,
    onVerifyClicked: () -> Unit,
    onResendSmsClicked: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = cs.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding() // Respeta la barra del sistema
                .imePadding()            // Empuja el contenido inferior arriba del teclado
        ) {
            // Contenido principal
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 70.dp), // Deja espacio para que el footer no tape el botón
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = stringResource(R.string.phone_verify_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = cs.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                VerificationHeaderIcon()

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.phone_verify_sent, uiState.phoneNumber),
                    fontSize = 13.sp,
                    color = cs.onSurface,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.phone_verify_enter_code),
                    fontSize = 13.sp,
                    color = cs.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                OtpInputField(
                    otpCode = uiState.otpCode,
                    onOtpCodeChange = onOtpCodeChanged,
                    isError = uiState.isError
                )

                if (uiState.isError && uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.errorMessage,
                        color = cs.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onVerifyClicked,
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cs.primary,
                        contentColor = cs.onPrimary,
                        disabledContainerColor = cs.primary.copy(alpha = 0.6f),
                        disabledContentColor = cs.onPrimary
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = cs.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.phone_verify_button),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Pie de página anclado al fondo (sube con el teclado)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(cs.background)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalDivider(color = cs.outlineVariant, thickness = 0.8.dp)

                Spacer(modifier = Modifier.height(12.dp))

                val footerNoCode = stringResource(R.string.phone_verify_no_code)
                val footerResend = stringResource(R.string.phone_verify_resend)
                val footerWait = stringResource(R.string.phone_verify_wait, uiState.timerSeconds)
                val annotatedFooter = buildAnnotatedString {
                    append(footerNoCode)
                    if (uiState.canResend) {
                        withStyle(style = SpanStyle(color = cs.primary, fontWeight = FontWeight.SemiBold)) {
                            append(footerResend)
                        }
                    } else {
                        withStyle(style = SpanStyle(color = cs.onSurfaceVariant)) {
                            append(footerWait)
                        }
                    }
                }

                Text(
                    text = annotatedFooter,
                    fontSize = 12.sp,
                    color = cs.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .clickable(enabled = uiState.canResend) {
                            onResendSmsClicked()
                        }
                )
            }
        }
    }
}

@Composable
private fun VerificationHeaderIcon() {
    Box(
        modifier = Modifier
            .height(130.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_celular_globo),
            contentDescription = null,
            modifier = Modifier.requiredSize(210.dp),
            // El dibujo es negro con fondo transparente: se pinta del color del texto
            // para que no desaparezca en modo oscuro
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
        )
    }
}

@Composable
private fun OtpInputField(
    otpCode: String,
    onOtpCodeChange: (String) -> Unit,
    isError: Boolean
) {
    val cs = MaterialTheme.colorScheme
    BasicTextField(
        value = otpCode,
        onValueChange = onOtpCodeChange,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val activeIndex = otpCode.length.coerceAtMost(5)

                repeat(6) { index ->
                    val char = otpCode.getOrNull(index)?.toString() ?: ""
                    val isActive = index == activeIndex

                    val borderColor = when {
                        isError -> cs.error
                        isActive -> cs.primary
                        else -> cs.outline
                    }

                    val borderWidth = if (isActive || isError) 2.dp else 1.dp

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .background(
                                color = if (isError) cs.errorContainer else cs.surfaceVariant,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .border(
                                width = borderWidth,
                                color = borderColor,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = cs.onSurface
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun VerifiedSuccessScreen() {
    LogroSonidoEffect()
    val cs = MaterialTheme.colorScheme
    // El tema se puede cambiar dentro de la app, por eso se revisa el fondo y no el sistema
    val esOscuro = cs.background.luminance() < 0.5f
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = if (esOscuro) SuccessBgDark else SuccessBgLight
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.phone_verified_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = cs.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(color = SuccessGreenCircle, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.phone_verified_cd),
                    tint = Color.White,
                    modifier = Modifier.size(90.dp)
                )
            }
        }
    }
}