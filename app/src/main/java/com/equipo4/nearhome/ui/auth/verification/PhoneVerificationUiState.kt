package com.equipo4.nearhome.ui.auth.verification

import com.equipo4.nearhome.network.LoginResponse

data class PhoneVerificationUiState(
    val phoneNumber: String = "",
    val otpCode: String = "",
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val isVerified: Boolean = false,
    val isLoading: Boolean = false,
    val isSending: Boolean = false, // mandando el SMS con Firebase
    val timerSeconds: Int = 60,
    val canResend: Boolean = false,
    /** Registro con Google: la sesión que regresó la API (entra directo, sin pasar por el login). */
    val sesionNueva: LoginResponse? = null
)
