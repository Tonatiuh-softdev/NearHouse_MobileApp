package com.equipo4.nearhome.ui.auth.verification

data class PhoneVerificationUiState(
    val phoneNumber: String = "",
    val otpCode: String = "",
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val isVerified: Boolean = false,
    val isLoading: Boolean = false,
    val timerSeconds: Int = 30,
    val canResend: Boolean = false
)