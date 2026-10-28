package com.equipo4.nearhome.ui.auth.verification

data class PhoneVerificationUiState(
    val phoneNumber: String = "",
    val otpCode: String = "",
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val isVerified: Boolean = false,
    val isLoading: Boolean = false,
    val isSending: Boolean = false, // mandando el SMS con Firebase
    val timerSeconds: Int = 60,
    val canResend: Boolean = false
)
