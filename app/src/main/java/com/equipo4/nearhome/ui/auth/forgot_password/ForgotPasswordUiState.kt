package com.equipo4.nearhome.ui.auth.forgot_password

/**
 * Estado que representa la UI de la pantalla de recuperación de contraseña.
 */
data class ForgotPasswordUiState(
    val email: String = "",
    val isEmailValid: Boolean = false,
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)