package com.equipo4.nearhome.ui.auth.verification

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
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.delay

import androidx.compose.ui.res.stringResource

private val PrimaryNavy = Color(0xFF0B2C4D)
private val ErrorRed = Color(0xFFE53935)
private val GrayInputBg = Color(0xFFF2F3F5)
private val GrayBorderDefault = Color(0xFFCCCCCC)
private val GrayDivider = Color(0xFFE0E0E0)
private val GraySubtext = Color(0xFF757575)
private val ResendLinkColor = Color(0xFF1E88E5)
private val SuccessBg = Color(0xFFEFF7EB)
private val SuccessGreenCircle = Color(0xFF52C41A)

@Composable
fun PhoneVerificationScreen(
    phoneNumber: String = "",
    viewModel: PhoneVerificationViewModel = viewModel(),
    onNavigateToLogin: () -> Unit = {}
) {
    LaunchedEffect(phoneNumber) {
        if (phoneNumber.isNotEmpty()) {
            viewModel.setPhoneNumber(phoneNumber)
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isVerified) {
        LaunchedEffect(Unit) {
            delay(2000L)
            onNavigateToLogin()
        }

        VerifiedSuccessScreen()
    } else {
        VerificationInputContent(
            uiState = uiState,
            onOtpCodeChanged = viewModel::onOtpCodeChanged,
            onVerifyClicked = viewModel::onVerifyClicked,
            onResendSmsClicked = viewModel::onResendSmsClicked
        )
    }
}

@Composable
private fun VerificationInputContent(
    uiState: PhoneVerificationUiState,
    onOtpCodeChanged: (String) -> Unit,
    onVerifyClicked: () -> Unit,
    onResendSmsClicked: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
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
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                VerificationHeaderIcon()

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.phone_verify_sent, uiState.phoneNumber),
                    fontSize = 13.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.phone_verify_enter_code),
                    fontSize = 13.sp,
                    color = Color.Black,
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
                        color = ErrorRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onVerifyClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryNavy,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = stringResource(R.string.phone_verify_button),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Pie de página anclado al fondo (sube con el teclado)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalDivider(color = GrayDivider, thickness = 0.8.dp)

                Spacer(modifier = Modifier.height(12.dp))

                val footerNoCode = stringResource(R.string.phone_verify_no_code)
                val footerResend = stringResource(R.string.phone_verify_resend)
                val footerWait = stringResource(R.string.phone_verify_wait, uiState.timerSeconds)
                val annotatedFooter = buildAnnotatedString {
                    append(footerNoCode)
                    if (uiState.canResend) {
                        withStyle(style = SpanStyle(color = ResendLinkColor, fontWeight = FontWeight.SemiBold)) {
                            append(footerResend)
                        }
                    } else {
                        withStyle(style = SpanStyle(color = GraySubtext)) {
                            append(footerWait)
                        }
                    }
                }

                Text(
                    text = annotatedFooter,
                    fontSize = 12.sp,
                    color = GraySubtext,
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
            modifier = Modifier.requiredSize(210.dp)
        )
    }
}

@Composable
private fun OtpInputField(
    otpCode: String,
    onOtpCodeChange: (String) -> Unit,
    isError: Boolean
) {
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
                        isError -> ErrorRed
                        isActive -> PrimaryNavy
                        else -> GrayBorderDefault
                    }

                    val borderWidth = if (isActive || isError) 2.dp else 1.dp

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .background(
                                color = GrayInputBg,
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
                            color = Color.Black
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun VerifiedSuccessScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SuccessBg
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
                color = Color.Black,
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