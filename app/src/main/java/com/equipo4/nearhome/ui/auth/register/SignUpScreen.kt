package com.equipo4.nearhome.ui.auth.register

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo4.nearhome.R
import kotlinx.coroutines.delay

private val NavyButtonColor = Color(0xFF0B2C4D)
private val BlueLinkColor = Color(0xFF3897F0)
private val TextGrayDisclaimer = Color(0xFF8E8E93)
private val GoogleButtonBg = Color(0xFFEEEEEE)
private val PrimaryNavy = Color(0xFF0B2C4D)
private val GrayInputBg = Color(0xFFF5F5F5)
private val GrayInputBorder = Color(0xFFE0E0E0)
private val ErrorRed = Color(0xFFE53935)

@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel = viewModel(),
    onNavigateToLogin: () -> Unit = {},
    onSignUpSuccess: () -> Unit = {},
    onNavigateToTerms: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    BackHandler(enabled = uiState.step == SignUpStep.OTP_VERIFICATION) {
        viewModel.onBackToForm()
    }

    when (uiState.step) {
        SignUpStep.FORM -> SignUpFormContent(
            uiState = uiState,
            onEmailChange = viewModel::onEmailChanged,
            onPasswordChange = viewModel::onPasswordChanged,
            onConfirmPasswordChange = viewModel::onConfirmPasswordChanged,
            onTermsAcceptedChange = viewModel::onTermsAcceptedChanged,
            onSubmit = viewModel::onSubmitForm,
            onNavigateToLogin = onNavigateToLogin,
            onNavigateToTerms = onNavigateToTerms,
            onNavigateToPrivacy = onNavigateToPrivacy
        )
        SignUpStep.OTP_VERIFICATION -> OtpVerificationContent(
            uiState = uiState,
            onDigitChange = viewModel::onOtpDigitChanged,
            onVerify = viewModel::onVerifyOtp,
            onResend = viewModel::onResendCode
        )
        SignUpStep.SUCCESS -> {
            LaunchedEffect(Unit) {
                delay(1500)
                onSignUpSuccess()
            }
            VerifiedSuccessContent()
        }
    }
}

@Composable
private fun SignUpFormContent(
    uiState: SignUpUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTermsAcceptedChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToPrivacy: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            Text(
                text = stringResource(R.string.signup_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            Spacer(modifier = Modifier.height(24.dp))

            // Campo Correo
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.signup_email_label) + ":", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                Spacer(modifier = Modifier.height(6.dp))
                CustomOutlinedInput(
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    placeholder = stringResource(R.string.login_email_hint),
                    keyboardType = KeyboardType.Email
                )

                if (uiState.showEmailError && uiState.displayEmailError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.displayEmailError!!,
                        color = ErrorRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Campo Contraseña
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.signup_password_label) + ":", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                Spacer(modifier = Modifier.height(6.dp))
                CustomOutlinedInput(
                    value = uiState.password,
                    onValueChange = onPasswordChange,
                    placeholder = "*****************",
                    isPassword = true
                )

                if (uiState.showPasswordError && uiState.passwordErrorMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.passwordErrorMessage!!,
                        color = ErrorRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Campo Confirmar contraseña
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.signup_confirm_password_label) + ":", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                Spacer(modifier = Modifier.height(6.dp))
                CustomOutlinedInput(
                    value = uiState.confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    placeholder = "*****************",
                    isPassword = true
                )

                if (uiState.showPasswordMismatchError) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.signup_password_mismatch),
                        color = ErrorRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Checkbox y Términos y condiciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = uiState.isTermsAccepted,
                    onCheckedChange = onTermsAcceptedChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = NavyButtonColor
                    )
                )

                val termsPrefix = stringResource(R.string.signup_terms_prefix)
                val termsLink = stringResource(R.string.signup_terms_link)
                val termsMiddle = stringResource(R.string.signup_terms_middle)
                val termsPrivacy = stringResource(R.string.signup_terms_privacy)

                val termsAnnotatedString = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = TextGrayDisclaimer, fontSize = 12.sp)) {
                        append(termsPrefix)
                    }

                    pushStringAnnotation(tag = "TERMS", annotation = "terms")
                    withStyle(style = SpanStyle(color = BlueLinkColor, fontSize = 12.sp, fontWeight = FontWeight.Normal)) {
                        append(termsLink)
                    }
                    pop()

                    withStyle(style = SpanStyle(color = TextGrayDisclaimer, fontSize = 12.sp)) {
                        append(termsMiddle)
                    }

                    pushStringAnnotation(tag = "PRIVACY", annotation = "privacy")
                    withStyle(style = SpanStyle(color = BlueLinkColor, fontSize = 12.sp, fontWeight = FontWeight.Normal)) {
                        append(termsPrivacy)
                    }
                    pop()
                }

                ClickableText(
                    text = termsAnnotatedString,
                    style = TextStyle(lineHeight = 16.sp),
                    modifier = Modifier.weight(1f),
                    onClick = { offset ->
                        termsAnnotatedString.getStringAnnotations(tag = "TERMS", start = offset, end = offset)
                            .firstOrNull()?.let { onNavigateToTerms() }

                        termsAnnotatedString.getStringAnnotations(tag = "PRIVACY", start = offset, end = offset)
                            .firstOrNull()?.let { onNavigateToPrivacy() }
                    }
                )
            }

            if (uiState.showTermsError) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.signup_terms_error),
                    color = ErrorRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onSubmit,
                enabled = uiState.areFieldsValid,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavyButtonColor,
                    disabledContainerColor = Color(0xFF888888),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.signup_send_code), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = GrayInputBorder)
                Text(stringResource(R.string.common_or), fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(horizontal = 12.dp))
                HorizontalDivider(modifier = Modifier.weight(1f), color = GrayInputBorder)
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = { },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoogleButtonBg,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Google",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.signup_continue_google), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

            Spacer(modifier = Modifier.height(16.dp))

            val haveAccountText = stringResource(R.string.login_have_account)
            val signinActionText = stringResource(R.string.login_signin_action)

            Text(
                text = buildAnnotatedString {
                    append(haveAccountText)
                    withStyle(style = SpanStyle(color = BlueLinkColor, fontWeight = FontWeight.Bold)) {
                        append(signinActionText)
                    }
                },
                fontSize = 12.sp,
                modifier = Modifier.clickable { onNavigateToLogin() }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OtpVerificationContent(
    uiState: SignUpUiState,
    onDigitChange: (Int, String) -> Unit,
    onVerify: () -> Unit,
    onResend: () -> Unit
) {
    val fullOtpCode = uiState.otpCode.joinToString("")
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = fullOtpCode,
                selection = TextRange(fullOtpCode.length)
            )
        )
    }

    LaunchedEffect(fullOtpCode) {
        if (textFieldValue.text != fullOtpCode) {
            textFieldValue = TextFieldValue(
                text = fullOtpCode,
                selection = TextRange(fullOtpCode.length)
            )
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = stringResource(R.string.signup_otp_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        Image(
            painter = painterResource(id = R.drawable.ic_nearhouse_logo),
            contentDescription = stringResource(R.string.signup_email_icon_cd),
            modifier = Modifier.size(90.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        val otpSubtitlePrefix = stringResource(R.string.signup_otp_subtitle)
        val emailValue = uiState.email.ifEmpty { stringResource(R.string.login_email_hint) }

        Text(
            text = buildAnnotatedString {
                append(otpSubtitlePrefix)
                withStyle(style = SpanStyle(color = Color(0xFF888888))) {
                    append(emailValue)
                }
            },
            fontSize = 14.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = stringResource(R.string.signup_otp_enter_code),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }
        ) {
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    if (newValue.text.length <= 6 && newValue.text.all { it.isDigit() }) {
                        textFieldValue = newValue
                        for (i in 0 until 6) {
                            val char = newValue.text.getOrNull(i)?.toString() ?: ""
                            onDigitChange(i, char)
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .size(1.dp)
                    .alpha(0f)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val activeIndex = fullOtpCode.length.coerceAtMost(5)

                for (index in 0 until 6) {
                    val digit = uiState.otpCode.getOrNull(index) ?: ""
                    val isFocused = index == activeIndex && fullOtpCode.length < 6

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(width = 46.dp, height = 56.dp)
                            .background(
                                color = if (uiState.isOtpError) Color(0xFFFFF0F0) else Color(0xFFEEEEEE),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .border(
                                width = if (isFocused) 1.5.dp else 0.dp,
                                color = when {
                                    uiState.isOtpError -> ErrorRed
                                    isFocused -> PrimaryNavy
                                    else -> Color.Transparent
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                    ) {
                        Text(
                            text = digit,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        if (uiState.isOtpError) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uiState.otpErrorMessage ?: stringResource(R.string.signup_otp_error),
                color = ErrorRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onVerify,
            enabled = fullOtpCode.length == 6 && !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryNavy,
                disabledContainerColor = PrimaryNavy.copy(alpha = 0.6f)
            )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(stringResource(R.string.signup_verify_email), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.canResendCode) {
            Text(
                text = stringResource(R.string.signup_resend_code),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryNavy,
                modifier = Modifier.clickable { onResend() }
            )
        } else {
            Text(
                text = stringResource(R.string.signup_resend_waiting, uiState.resendCountdown),
                fontSize = 12.sp,
                color = Color(0xFF888888),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun VerifiedSuccessContent() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF3F9EE)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.signup_verified_success),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .size(180.dp)
                    .background(Color(0xFF52B768), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.listing_verified),
                    tint = Color.White,
                    modifier = Modifier.size(100.dp)
                )
            }
        }
    }
}

@Composable
private fun CustomOutlinedInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
        visualTransformation = if (isPassword && !isPasswordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
            imeAction = ImeAction.Next
        ),
        decorationBox = { innerTextField ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(8.dp),
                color = GrayInputBg,
                border = BorderStroke(1.dp, GrayInputBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (value.isEmpty()) {
                            Text(placeholder, color = Color.Gray, fontSize = 14.sp)
                        }
                        innerTextField()
                    }

                    if (isPassword) {
                        IconButton(
                            onClick = { isPasswordVisible = !isPasswordVisible },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    }
                }
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}