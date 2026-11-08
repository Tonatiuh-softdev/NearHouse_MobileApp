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
    val googleSuccessMessage: String? = null,
    val showReactivateDialog: Boolean = false,
    /** true = la cuenta está por eliminarse (se pregunta si la conserva); false = solo desactivada. */
    val reactivateIsDeletion: Boolean = false,
    /** Mensaje de la API para el diálogo (trae la fecha de eliminación). */
    val reactivateMessage: String? = null,
    val isReactivating: Boolean = false,
    val reactivateSuccessMessage: String? = null
) {
    val isFormValid: Boolean
        get() = isEmailValid && isPasswordValid && !isLoading
}
