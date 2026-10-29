package com.equipo4.nearhome.ui.profile.account

import androidx.annotation.StringRes
import com.equipo4.nearhome.ui.profile.UserRole

data class UserAccountData(
    val email: String,
    val phone: String,
    val role: UserRole,
    val planName: String? = null,
    val planStatus: String? = null,
    val planExpired: Boolean = false,
    val renewalDate: String? = null
)

data class AccountAdminUiState(
    val isLoading: Boolean = false,
    val userData: UserAccountData? = null,
    val showDeactivateDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val deleteInputText: String = "",
    @param:StringRes val userMessage: Int? = null
) {
    val isDeleteButtonEnabled: Boolean
        get() {
            val input = deleteInputText.trim().uppercase()
            return input == "ELIMINAR" || input == "DELETE"
        }
}

sealed interface AccountAdminUiEvent {
    data object OnOptionClick : AccountAdminUiEvent
    data object OnDeactivateRequested : AccountAdminUiEvent
    data object OnDeactivateConfirmed : AccountAdminUiEvent
    data object OnDeactivateDismissed : AccountAdminUiEvent
    data object OnDeleteRequested : AccountAdminUiEvent
    data object OnDeleteConfirmed : AccountAdminUiEvent
    data object OnDeleteDismissed : AccountAdminUiEvent
    data class OnDeleteInputChanged(val text: String) : AccountAdminUiEvent
    data object OnUserMessageConsumed : AccountAdminUiEvent
}