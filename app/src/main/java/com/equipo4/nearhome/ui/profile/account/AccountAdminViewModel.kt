package com.equipo4.nearhome.ui.profile.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.session.CurrentUserStore
import com.equipo4.nearhome.network.UsuarioMeDto
import com.equipo4.nearhome.ui.profile.UserRole
import com.equipo4.nearhome.ui.subscription.PlanDates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccountAdminViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AccountAdminUiState(isLoading = true))
    val uiState: StateFlow<AccountAdminUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            CurrentUserStore.user.collect { user ->
                _uiState.update {
                    it.copy(isLoading = false, userData = user?.toAccountData())
                }
            }
        }
    }

    private fun UsuarioMeDto.toAccountData(): UserAccountData {
        val res = getApplication<Application>().resources
        val locale = res.configuration.locales[0]
        val p = plan
        val vigente = p?.vigente == true
        return UserAccountData(
            email = correo,
            phone = telefono,
            role = UserRole.fromDb(rol),
            planName = p?.let { res.getString(R.string.plan_full_name_format, it.nombre) },
            planStatus = p?.let { res.getString(if (vigente) R.string.plan_status_active else R.string.plan_status_expired) },
            planExpired = p != null && !vigente,
            renewalDate = p?.let { PlanDates.formatear(it.fechaVencimiento, locale) }
        )
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
                _uiState.update { it.copy(showDeactivateDialog = false, userMessage = R.string.coming_soon) }
            }
            AccountAdminUiEvent.OnDeleteRequested -> {
                _uiState.update { it.copy(showDeleteDialog = true, deleteInputText = "") }
            }
            AccountAdminUiEvent.OnDeleteDismissed -> {
                _uiState.update { it.copy(showDeleteDialog = false, deleteInputText = "") }
            }
            is AccountAdminUiEvent.OnDeleteInputChanged -> {
                _uiState.update { it.copy(deleteInputText = event.text) }
            }
            AccountAdminUiEvent.OnDeleteConfirmed -> {
                _uiState.update {
                    it.copy(showDeleteDialog = false, deleteInputText = "", userMessage = R.string.coming_soon)
                }
            }
            AccountAdminUiEvent.OnUserMessageConsumed -> {
                _uiState.update { it.copy(userMessage = null) }
            }
        }
    }
}