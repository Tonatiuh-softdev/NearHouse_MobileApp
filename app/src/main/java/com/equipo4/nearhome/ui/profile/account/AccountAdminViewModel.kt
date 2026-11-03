package com.equipo4.nearhome.ui.profile.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.session.CurrentUserStore
import com.equipo4.nearhome.di.NetworkModule
import com.equipo4.nearhome.network.AuthApi
import com.equipo4.nearhome.network.CambiarContrasenaRequest
import com.equipo4.nearhome.network.DesactivarCuentaRequest
import com.equipo4.nearhome.network.UsuarioMeDto
import com.equipo4.nearhome.network.VerificarCodigoContrasenaRequest
import com.equipo4.nearhome.ui.profile.UserRole
import com.equipo4.nearhome.ui.subscription.PlanDates
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException

class AccountAdminViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val authApi: AuthApi = NetworkModule.provideRetrofit(
        NetworkModule.provideOkHttpClient(sessionManager)
    ).create(AuthApi::class.java)

    private val _uiState = MutableStateFlow(AccountAdminUiState(isLoading = true))
    val uiState: StateFlow<AccountAdminUiState> = _uiState.asStateFlow()

    private var resendTimerJob: Job? = null

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
            renewalDate = p?.let { PlanDates.formatear(it.fechaVencimiento, locale) },
            isGoogleUser = !tieneContrasena,
            tieneContrasena = tieneContrasena
        )
    }

    fun onEvent(event: AccountAdminUiEvent) {
        when (event) {
            AccountAdminUiEvent.OnOptionClick -> {
                _uiState.update { it.copy(userMessage = R.string.coming_soon) }
            }
            AccountAdminUiEvent.OnPasswordOptionClick -> {
                _uiState.update {
                    it.copy(
                        passwordStep = PasswordFlowStep.CONFIRM_SEND_CODE,
                        otpErrorMessage = null
                    )
                }
            }
            AccountAdminUiEvent.OnRequestCodeConfirmed -> {
                solicitarCodigoContrasena()
            }
            AccountAdminUiEvent.OnPasswordFlowDismissed -> {
                limpiarEstadoMemoriaContrasena()
            }
            is AccountAdminUiEvent.OnOtpDigitChanged -> {
                val currentOtp = _uiState.value.otpCode.toMutableList()
                currentOtp[event.index] = event.digit
                _uiState.update { it.copy(otpCode = currentOtp, otpErrorMessage = null) }
            }
            AccountAdminUiEvent.OnVerifyCodeSubmitted -> {
                verificarCodigoContrasena()
            }
            AccountAdminUiEvent.OnResendCodeRequested -> {
                if (_uiState.value.canResendCode) {
                    solicitarCodigoContrasena()
                }
            }
            is AccountAdminUiEvent.OnCurrentPasswordChanged -> {
                _uiState.update { it.copy(currentPasswordInput = event.text, currentPasswordError = null) }
            }
            is AccountAdminUiEvent.OnNewPasswordChanged -> {
                _uiState.update { it.copy(newPasswordInput = event.text, passwordSaveError = null) }
            }
            is AccountAdminUiEvent.OnConfirmPasswordChanged -> {
                _uiState.update { it.copy(confirmPasswordInput = event.text, passwordSaveError = null) }
            }
            AccountAdminUiEvent.OnSaveNewPasswordSubmitted -> {
                guardarNuevaContrasena()
            }
            AccountAdminUiEvent.OnDeactivateRequested -> {
                _uiState.update {
                    it.copy(
                        showDeactivateDialog = true,
                        deactivatePasswordInput = "",
                        deactivateError = null
                    )
                }
            }
            is AccountAdminUiEvent.OnDeactivatePasswordChanged -> {
                _uiState.update {
                    it.copy(deactivatePasswordInput = event.text, deactivateError = null)
                }
            }
            AccountAdminUiEvent.OnDeactivateDismissed -> {
                _uiState.update {
                    it.copy(
                        showDeactivateDialog = false,
                        deactivatePasswordInput = "",
                        deactivateError = null
                    )
                }
            }
            is AccountAdminUiEvent.OnDeactivateConfirmed -> {
                desactivarCuenta(event.googleToken)
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
            AccountAdminUiEvent.OnSnackbarMessageConsumed -> {
                _uiState.update { it.copy(snackbarSuccessMessage = null) }
            }
        }
    }

    private fun solicitarCodigoContrasena() {
        viewModelScope.launch {
            _uiState.update { it.copy(isOtpLoading = true, otpErrorMessage = null) }
            try {
                val response = authApi.solicitarCodigoContrasena()
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    _uiState.update {
                        it.copy(
                            isOtpLoading = false,
                            maskedEmail = body.correoOculto,
                            passwordStep = PasswordFlowStep.VERIFY_CODE,
                            otpCode = List(6) { "" },
                            otpErrorMessage = null
                        )
                    }
                    startResendTimer()
                } else {
                    val rawError = response.errorBody()?.string()
                    val errorMsg = parseErrorMessage(rawError, "No se pudo enviar el correo")
                    _uiState.update { it.copy(isOtpLoading = false, otpErrorMessage = errorMsg) }
                }
            } catch (_: IOException) {
                _uiState.update {
                    it.copy(isOtpLoading = false, otpErrorMessage = "No se pudo conectar con el servidor. Revisa tu conexión a internet e inténtalo de nuevo")
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(isOtpLoading = false, otpErrorMessage = "Ocurrió un error inesperado. Inténtalo de nuevo")
                }
            }
        }
    }

    private fun verificarCodigoContrasena() {
        val currentState = _uiState.value
        val code = currentState.otpCode.joinToString("")
        if (code.length != 6) return

        viewModelScope.launch {
            _uiState.update { it.copy(isOtpLoading = true, otpErrorMessage = null) }
            try {
                val response = authApi.verificarCodigoContrasena(VerificarCodigoContrasenaRequest(code))
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    _uiState.update {
                        it.copy(
                            isOtpLoading = false,
                            proofToken = body.comprobante,
                            passwordStep = PasswordFlowStep.NEW_PASSWORD,
                            otpErrorMessage = null
                        )
                    }
                } else {
                    val rawError = response.errorBody()?.string()
                    val errorMsg = parseErrorMessage(rawError, "El código es incorrecto o ya venció")
                    _uiState.update { it.copy(isOtpLoading = false, otpErrorMessage = errorMsg) }
                }
            } catch (_: IOException) {
                _uiState.update {
                    it.copy(isOtpLoading = false, otpErrorMessage = "No se pudo conectar con el servidor. Revisa tu conexión a internet e inténtalo de nuevo")
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(isOtpLoading = false, otpErrorMessage = "Ocurrió un error inesperado. Inténtalo de nuevo")
                }
            }
        }
    }

    private fun guardarNuevaContrasena() {
        val currentState = _uiState.value
        val proof = currentState.proofToken ?: return
        if (!currentState.isNewPasswordFormValid || currentState.isPasswordSaving) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isPasswordSaving = true,
                    currentPasswordError = null,
                    passwordSaveError = null
                )
            }
            try {
                val hasCurrentPassword = currentState.userData?.tieneContrasena == true
                val request = CambiarContrasenaRequest(
                    comprobante = proof,
                    contrasenaActual = if (hasCurrentPassword) currentState.currentPasswordInput else null,
                    contrasenaNueva = currentState.newPasswordInput
                )
                val response = authApi.cambiarOcrearContrasena(request)
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    // Guardar nuevo token de sesión emitido con nueva versión
                    sessionManager.saveToken(body.token)
                    CurrentUserStore.refresh(authApi)

                    val successMsg = if (hasCurrentPassword) "Contraseña actualizada" else "Contraseña creada"

                    limpiarEstadoMemoriaContrasena()
                    _uiState.update {
                        it.copy(snackbarSuccessMessage = successMsg)
                    }
                } else {
                    val rawError = response.errorBody()?.string()
                    val errorMsg = parseErrorMessage(rawError, "Error al guardar la contraseña")
                    if (response.code() == 403) {
                        _uiState.update {
                            it.copy(isPasswordSaving = false, currentPasswordError = "La contraseña actual no es correcta")
                        }
                    } else if (response.code() == 409) {
                        limpiarEstadoMemoriaContrasena()
                        _uiState.update {
                            it.copy(snackbarSuccessMessage = "El tiempo para cambiar tu contraseña terminó. Vuelve a empezar.")
                        }
                    } else {
                        _uiState.update {
                            it.copy(isPasswordSaving = false, passwordSaveError = errorMsg)
                        }
                    }
                }
            } catch (_: IOException) {
                _uiState.update {
                    it.copy(
                        isPasswordSaving = false,
                        passwordSaveError = "No se pudo conectar con el servidor. Revisa tu conexión a internet e inténtalo de nuevo"
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isPasswordSaving = false,
                        passwordSaveError = "Ocurrió un error inesperado. Inténtalo de nuevo"
                    )
                }
            }
        }
    }

    private fun startResendTimer() {
        resendTimerJob?.cancel()
        _uiState.update { it.copy(canResendCode = false, resendCountdown = 60) }
        resendTimerJob = viewModelScope.launch {
            for (i in 60 downTo 1) {
                _uiState.update { it.copy(resendCountdown = i) }
                delay(1000)
            }
            _uiState.update { it.copy(canResendCode = true, resendCountdown = 0) }
        }
    }

    private fun limpiarEstadoMemoriaContrasena() {
        resendTimerJob?.cancel()
        _uiState.update {
            it.copy(
                passwordStep = PasswordFlowStep.NONE,
                maskedEmail = "",
                otpCode = List(6) { "" },
                isOtpLoading = false,
                otpErrorMessage = null,
                resendCountdown = 0,
                canResendCode = false,
                currentPasswordInput = "",
                currentPasswordError = null,
                newPasswordInput = "",
                confirmPasswordInput = "",
                isPasswordSaving = false,
                passwordSaveError = null,
                proofToken = null
            )
        }
    }

    private fun desactivarCuenta(googleToken: String?) {
        val currentState = _uiState.value
        if (currentState.isDeactivating) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeactivating = true, deactivateError = null) }
            try {
                val request = DesactivarCuentaRequest(
                    contrasena = currentState.deactivatePasswordInput.takeIf { it.isNotBlank() },
                    googleToken = googleToken
                )
                val response = authApi.desactivarCuenta(request)
                if (response.isSuccessful || response.code() == 204) {
                    sessionManager.clearSession()
                    _uiState.update {
                        it.copy(
                            isDeactivating = false,
                            showDeactivateDialog = false,
                            isDeactivatedSuccess = true
                        )
                    }
                } else {
                    val rawError = response.errorBody()?.string()
                    val errorMsg = parseErrorMessage(rawError, "La contraseña actual es incorrecta")
                    _uiState.update {
                        it.copy(isDeactivating = false, deactivateError = errorMsg)
                    }
                }
            } catch (_: IOException) {
                _uiState.update {
                    it.copy(
                        isDeactivating = false,
                        deactivateError = "No se pudo conectar con el servidor. Revisa tu conexión a internet e inténtalo de nuevo"
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isDeactivating = false,
                        deactivateError = "Ocurrió un error inesperado. Inténtalo de nuevo"
                    )
                }
            }
        }
    }

    private fun parseErrorMessage(rawError: String?, defaultMsg: String): String {
        if (rawError.isNullOrBlank()) return defaultMsg
        return try {
            val json = JSONObject(rawError)
            json.optString("error", defaultMsg)
        } catch (_: Exception) {
            defaultMsg
        }
    }

    override fun onCleared() {
        super.onCleared()
        resendTimerJob?.cancel()
        limpiarEstadoMemoriaContrasena()
    }
}
