package com.equipo4.nearhome.ui.profile.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.R
import com.equipo4.nearhome.ui.profile.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccountAdminViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AccountAdminUiState())
    val uiState: StateFlow<AccountAdminUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Carga de datos de prueba (en producción provienen del SessionManager o repositorio)
            val user = UserAccountData(
                email = "cdjsz@email.com",
                phone = "+52 314 123 4567",
                role = UserRole.VENDEDOR,
                planName = "Plan Vendedor Profesional",
                planStatus = "Activo",
                renewalDate = "15 de noviembre de 2026"
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    userData = user
                )
            }
        }
    }

    fun onEvent(event: AccountAdminUiEvent) {
        when (event) {
            AccountAdminUiEvent.OnOptionClick -> {
                _uiState.update { it.copy(userMessage = R.string.coming_soon) }
            }
            AccountAdminUiEvent.OnDeactivateRequested -> {
                _uiState.update { it.copy(showDeactivateDialog = true) }
            }
            AccountAdminUiEvent.OnDeactivateDismissed -> {
                _uiState.update { it.copy(showDeactivateDialog = false) }
            }
            AccountAdminUiEvent.OnDeactivateConfirmed -> {
                _uiState.update {
                    it.copy(
                        showDeactivateDialog = false,
                        userMessage = R.string.coming_soon
                    )
                }
            }
            AccountAdminUiEvent.OnDeleteRequested -> {
                _uiState.update { it.copy(showDeleteDialog = true, deleteInputText = "") }
            }
            AccountAdminUiEvent.OnDeleteDismissed -> {
                _uiState.update { it.copy(showDeleteDialog = false, deleteInputText = "") }
            }
            is AccountAdminUiEvent.OnDeleteInputChanged -> {
                val newText = event.text
                _uiState.update { it.copy(deleteInputText = newText) }
            }
            AccountAdminUiEvent.OnDeleteConfirmed -> {
                _uiState.update {
                    it.copy(
                        showDeleteDialog = false,
                        deleteInputText = "",
                        userMessage = R.string.coming_soon
                    )
                }
            }
            AccountAdminUiEvent.OnUserMessageConsumed -> {
                _uiState.update { it.copy(userMessage = null) }
            }
        }
    }
}