package com.equipo4.nearhome.ui.profile.account

import android.util.Patterns
import androidx.annotation.StringRes
import com.equipo4.nearhome.ui.profile.UserRole

data class UserAccountData(
    val email: String,
    val phone: String,
    val role: UserRole,
    val planName: String? = null,
    val planStatus: String? = null,
    val planExpired: Boolean = false,
    val renewalDate: String? = null,
    val isGoogleUser: Boolean = false,
    val tieneContrasena: Boolean = true
)

enum class PasswordFlowStep {
    NONE,
    CONFIRM_SEND_CODE,
    VERIFY_CODE,
    NEW_PASSWORD
}

enum class EmailFlowStep {
    NONE,
    NEW_EMAIL,
    VERIFY_CODE
}

data class AccountAdminUiState(
    val isLoading: Boolean = false,
    val userData: UserAccountData? = null,

    // Flujo de Contraseña
    val passwordStep: PasswordFlowStep = PasswordFlowStep.NONE,
    val maskedEmail: String = "",
    val otpCode: List<String> = List(6) { "" },
    val isOtpLoading: Boolean = false,
    val otpErrorMessage: String? = null,
    val resendCountdown: Int = 0,
    val canResendCode: Boolean = false,
    val currentPasswordInput: String = "",
    val currentPasswordError: String? = null,
    val newPasswordInput: String = "",
    val confirmPasswordInput: String = "",
    val isPasswordSaving: Boolean = false,
    val passwordSaveError: String? = null,
    val proofToken: String? = null,

    // Flujo de Cambio de Correo
    val emailStep: EmailFlowStep = EmailFlowStep.NONE,
    val newEmailInput: String = "",
    val emailCurrentPasswordInput: String = "",
    val emailCurrentPasswordError: String? = null,
    val newEmailError: String? = null,
    val isEmailCodeSending: Boolean = false,
    val emailGeneralError: String? = null,
    val emailOtpCode: List<String> = List(6) { "" },
    val isEmailConfirming: Boolean = false,
    val emailOtpErrorMessage: String? = null,
    val emailResendCountdown: Int = 0,
    val canResendEmailCode: Boolean = false,
    val googleTokenForEmailChange: String? = null,

    // Diálogos de Desactivación y Eliminación
    val showDeactivateDialog: Boolean = false,
    val deactivatePasswordInput: String = "",
    val deactivateError: String? = null,
    val isDeactivating: Boolean = false,
    val isDeactivatedSuccess: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val deleteInputText: String = "",
    val deletePasswordInput: String = "",
    val deleteError: String? = null,
    val isDeleting: Boolean = false,
    /** Fecha en que se borrará la cuenta, cuando la eliminación ya quedó programada. */
    val deletionScheduledDate: String? = null,
    /** Se está abriendo la ventana de Google para confirmar desactivar o eliminar. */
    val isGoogleConfirming: Boolean = false,
    @param:StringRes val userMessage: Int? = null,
    val snackbarSuccessMessage: String? = null
) {
    val isDeleteButtonEnabled: Boolean
        get() {
            val input = deleteInputText.trim().uppercase()
            val textoCorrecto = input == "ELIMINAR" || input == "DELETE"
            // Con contraseña hay que escribirla; las cuentas de Google confirman con Google al tocar el botón
            val identidadLista = userData?.tieneContrasena != true || deletePasswordInput.isNotBlank()
            return textoCorrecto && identidadLista && !isDeleting && !isGoogleConfirming
        }

    val isNewPasswordFormValid: Boolean
        get() {
            val hasMinLength = newPasswordInput.length >= 6
            val isConfirmed = newPasswordInput == confirmPasswordInput
            val hasCurrentPassword = userData?.tieneContrasena == true
            val currentPasswordValid = if (hasCurrentPassword) currentPasswordInput.isNotBlank() else true
            val isDifferentFromCurrent = if (hasCurrentPassword) newPasswordInput != currentPasswordInput else true
            return hasMinLength && isConfirmed && currentPasswordValid && isDifferentFromCurrent && !isPasswordSaving
        }

    val isNewEmailFormValid: Boolean
        get() {
            val emailClean = newEmailInput.trim()
            val isFormatValid = Patterns.EMAIL_ADDRESS.matcher(emailClean).matches()
            val isDifferent = emailClean.lowercase() != userData?.email?.lowercase()
            val hasPassword = userData?.tieneContrasena == true
            val passwordValid = if (hasPassword) emailCurrentPasswordInput.isNotBlank() else true
            return isFormatValid && isDifferent && passwordValid && !isEmailCodeSending
        }
}

sealed interface AccountAdminUiEvent {
    data object OnOptionClick : AccountAdminUiEvent
    data object OnEmailOptionClick : AccountAdminUiEvent
    data class OnNewEmailChanged(val text: String) : AccountAdminUiEvent
    data class OnEmailCurrentPasswordChanged(val text: String) : AccountAdminUiEvent
    data class OnRequestEmailCodeSubmitted(val googleToken: String? = null) : AccountAdminUiEvent
    data object OnEmailFlowDismissed : AccountAdminUiEvent
    data class OnEmailOtpDigitChanged(val index: Int, val digit: String) : AccountAdminUiEvent
    data object OnConfirmNewEmailSubmitted : AccountAdminUiEvent
    data object OnResendEmailCodeRequested : AccountAdminUiEvent
    data object OnReturnToNewEmailStepRequested : AccountAdminUiEvent

    data object OnPasswordOptionClick : AccountAdminUiEvent
    data object OnRequestCodeConfirmed : AccountAdminUiEvent
    data object OnPasswordFlowDismissed : AccountAdminUiEvent
    data class OnOtpDigitChanged(val index: Int, val digit: String) : AccountAdminUiEvent
    data object OnVerifyCodeSubmitted : AccountAdminUiEvent
    data object OnResendCodeRequested : AccountAdminUiEvent
    data class OnCurrentPasswordChanged(val text: String) : AccountAdminUiEvent
    data class OnNewPasswordChanged(val text: String) : AccountAdminUiEvent
    data class OnConfirmPasswordChanged(val text: String) : AccountAdminUiEvent
    data object OnSaveNewPasswordSubmitted : AccountAdminUiEvent

    data object OnDeactivateRequested : AccountAdminUiEvent
    data class OnDeactivatePasswordChanged(val text: String) : AccountAdminUiEvent
    data class OnDeactivateConfirmed(val googleToken: String? = null) : AccountAdminUiEvent
    data object OnDeactivateDismissed : AccountAdminUiEvent

    data object OnDeleteRequested : AccountAdminUiEvent
    data class OnDeleteConfirmed(val googleToken: String? = null) : AccountAdminUiEvent
    data class OnDeletePasswordChanged(val text: String) : AccountAdminUiEvent
    data object OnDeleteDismissed : AccountAdminUiEvent
    data class OnDeleteInputChanged(val text: String) : AccountAdminUiEvent

    data object OnUserMessageConsumed : AccountAdminUiEvent
    data object OnSnackbarMessageConsumed : AccountAdminUiEvent
}
