package com.equipo4.nearhome.ui.auth.login

data class LoginUiState(
    val email: String = "",
    val isEmailValid: Boolean = false,
    val password: String = "",
    val isPasswordValid: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val googleSuccessMessage: String? = null
) {
    // El botón Entrar solo se activa si el correo tiene formato válido,
    // la contraseña no está vacía y no se está ejecutando la petición (isLoading)
    val isFormValid: Boolean
        get() = isEmailValid && isPasswordValid && !isLoading
}