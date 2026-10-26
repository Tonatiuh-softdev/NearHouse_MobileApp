package com.equipo4.nearhome.ui.auth.forgot_password

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.equipo4.nearhome.util.AppStrings
import com.equipo4.nearhome.R

class ForgotPasswordViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    // Dominios comerciales comunes
    private val publicDomains = setOf(
        "gmail.com",
        "hotmail.com",
        "hotmail.es",
        "outlook.com",
        "outlook.es",
        "yahoo.com",
        "icloud.com"
    )

    // Seteo de extensiones institucionales / educativas
    private val institutionalSuffixes = listOf(
        ".edu",
        ".edu.mx",
        ".gob.mx",
        ".org.mx"
    )

    // Dominios institucionales específicos
    private val specificInstitutionalDomains = setOf(
        "ucol.mx" // Universidad de Colima
    )

    fun onEmailChanged(email: String) {
        val isValid = isStrictEmailValid(email)
        _uiState.update { currentState ->
            currentState.copy(
                email = email,
                isEmailValid = isValid
            )
        }
    }

    private fun isStrictEmailValid(email: String): Boolean {
        val trimmedEmail = email.trim()

        // 1. Sintaxis general
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            return false
        }

        // 2. Mínimo 3 caracteres antes del @ (Nombre de usuario)
        val username = trimmedEmail.substringBefore("@", "")
        if (username.length < 3) {
            return false
        }

        val domain = trimmedEmail.substringAfter("@", "").lowercase()

        // 3. Comprobación contra reglas de dominios permitidos
        val isPublic = domain in publicDomains
        val isSpecificInst = domain in specificInstitutionalDomains
        val isInstSuffix = institutionalSuffixes.any { domain.endsWith(it) }

        return isPublic || isSpecificInst || isInstSuffix
    }

    fun resetPassword() {
        if (!_uiState.value.isEmailValid || _uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            try {
                // TODO: Reemplazar con la llamada real al backend cuando esté listo
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = AppStrings.get(R.string.forgot_success)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = AppStrings.get(R.string.forgot_error_connection)
                    )
                }
            }
        }
    }

    fun dismissMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}