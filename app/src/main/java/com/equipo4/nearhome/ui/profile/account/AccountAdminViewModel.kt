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
import com.equipo4.nearhome.network.ConfirmarCambioCorreoRequest
import com.equipo4.nearhome.network.DesactivarCuentaRequest
import com.equipo4.nearhome.network.SolicitarCodigoCambioCorreoRequest
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
import android.content.Context
import com.equipo4.nearhome.data.remote.GoogleAuthManager
import java.io.IOException

class AccountAdminViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val authApi: AuthApi = NetworkModule.provideRetrofit(
        NetworkModule.provideOkHttpClient(sessionManager)
    ).create(AuthApi::class.java)

    private val _uiState = MutableStateFlow(AccountAdminUiState(isLoading = true))
    val uiState: StateFlow<AccountAdminUiState> = _uiState.asStateFlow()

    private var resendTimerJob: Job? = null
    private var emailResendTimerJob: Job? = null

    init {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, loadError = false) }
                val response = CurrentUserStore.refresh(authApi)
                _uiState.update { it.copy(loadError = !response.isSuccessful) }
            } catch (e: Exception) {
                _uiState.update { it.copy(loadError = true) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }

            CurrentUserStore.user.collect { user ->
                _uiState.update {
                    it.copy(isLoading = false, userData = user?.toAccountData())
                }
            }
        }
    }

    fun reload() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, loadError = false) }
                val response = CurrentUserStore.refresh(authApi)
                _uiState.update { it.copy(loadError = !response.isSuccessful) }
            } catch (e: Exception) {
                _uiState.update { it.copy(loadError = true) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
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

            // --- Cambio de Correo ---
            AccountAdminUiEvent.OnEmailOptionClick -> {
                _uiState.update {
                    it.copy(
                        emailStep = EmailFlowStep.NEW_EMAIL,
                        newEmailInput = "",
                        emailCurrentPasswordInput = "",
                        emailCurrentPasswordError = null,
                        newEmailError = null,
                        emailGeneralError = null,
                        emailOtpCode = List(6) { "" },
                        emailOtpErrorMessage = null,
                        googleTokenForEmailChange = null
                    )
                }
            }
            is AccountAdminUiEvent.OnNewEmailChanged -> {
                _uiState.update { it.copy(newEmailInput = event.text, newEmailError = null, emailGeneralError = null) }
            }
            is AccountAdminUiEvent.OnEmailCurrentPasswordChanged -> {
                _uiState.update { it.copy(emailCurrentPasswordInput = event.text, emailCurrentPasswordError = null, emailGeneralError = null) }
            }
            is AccountAdminUiEvent.OnRequestEmailCodeSubmitted -> {
                solicitarCodigoCambioCorreo(event.googleToken)
            }
            AccountAdminUiEvent.OnEmailFlowDismissed -> {
                limpiarEstadoMemoriaCorreo()
            }
            is AccountAdminUiEvent.OnEmailOtpDigitChanged -> {
                val currentOtp = _uiState.value.emailOtpCode.toMutableList()
                currentOtp[event.index] = event.digit
                _uiState.update { it.copy(emailOtpCode = currentOtp, emailOtpErrorMessage = null) }
            }
            AccountAdminUiEvent.OnConfirmNewEmailSubmitted -> {
                confirmarCambioCorreo()
            }
            AccountAdminUiEvent.OnResendEmailCodeRequested -> {
                if (_uiState.value.canResendEmailCode) {
                    solicitarCodigoCambioCorreo(_uiState.value.googleTokenForEmailChange)
                }
            }
            AccountAdminUiEvent.OnReturnToNewEmailStepRequested -> {
                _uiState.update { it.copy(emailStep = EmailFlowStep.NEW_EMAIL, emailOtpErrorMessage = null) }
            }

            // --- Contraseña ---
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

            // --- Desactivar / Eliminar ---
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
                _uiState.update {
                    it.copy(showDeleteDialog = true, deleteInputText = "", deletePasswordInput = "", deleteError = null)
                }
            }
            AccountAdminUiEvent.OnDeleteDismissed -> {
                if (!_uiState.value.isDeleting && !_uiState.value.isGoogleConfirming) {
                    _uiState.update {
                        it.copy(showDeleteDialog = false, deleteInputText = "", deletePasswordInput = "", deleteError = null)
                    }
                }
            }
            is AccountAdminUiEvent.OnDeleteInputChanged -> {
                _uiState.update { it.copy(deleteInputText = event.text, deleteError = null) }
            }
            is AccountAdminUiEvent.OnDeletePasswordChanged -> {
                _uiState.update { it.copy(deletePasswordInput = event.text, deleteError = null) }
            }
            is AccountAdminUiEvent.OnDeleteConfirmed -> {
                eliminarCuenta(event.googleToken)
            }
            AccountAdminUiEvent.OnUserMessageConsumed -> {
                _uiState.update { it.copy(userMessage = null) }
            }
            AccountAdminUiEvent.OnSnackbarMessageConsumed -> {
                _uiState.update { it.copy(snackbarSuccessMessage = null) }
            }
        }
    }

    private fun solicitarCodigoCambioCorreo(googleToken: String?) {
        val currentState = _uiState.value
        if (!currentState.isNewEmailFormValid && googleToken == null) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isEmailCodeSending = true,
                    emailCurrentPasswordError = null,
                    newEmailError = null,
                    emailGeneralError = null
                )
            }
            try {
                val hasPass = currentState.userData?.tieneContrasena == true
                val request = SolicitarCodigoCambioCorreoRequest(
                    correoNuevo = currentState.newEmailInput.trim(),
                    contrasena = if (hasPass) currentState.emailCurrentPasswordInput else null,
                    googleToken = googleToken
                )
                val response = authApi.solicitarCodigoCambioCorreo(request)
                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            isEmailCodeSending = false,
                            googleTokenForEmailChange = googleToken,
                            emailStep = EmailFlowStep.VERIFY_CODE,
                            emailOtpCode = List(6) { "" },
                            emailOtpErrorMessage = null
                        )
                    }
                    startEmailResendTimer()
                } else {
                    val rawError = response.errorBody()?.string()
                    val errorMsg = parseErrorMessage(rawError, "Error al solicitar código")
                    when (response.code()) {
                        403 -> {
                            if (hasPass) {
                                _uiState.update { it.copy(isEmailCodeSending = false, emailCurrentPasswordError = "La contraseña actual no es correcta") }
                            } else {
                                _uiState.update { it.copy(isEmailCodeSending = false, emailGeneralError = "Esa cuenta de Google no es la de tu cuenta de NearHouse. Elige la correcta.") }
                            }
                        }
                        409 -> {
                            _uiState.update { it.copy(isEmailCodeSending = false, newEmailError = "Ese correo ya está registrado") }
                        }
                        400 -> {
                            if (errorMsg.contains("Confirma con Google", ignoreCase = true)) {
                                _uiState.update { it.copy(isEmailCodeSending = false, emailStep = EmailFlowStep.NEW_EMAIL, emailGeneralError = "Confirma con Google otra vez") }
                            } else {
                                _uiState.update { it.copy(isEmailCodeSending = false, emailGeneralError = errorMsg) }
                            }
                        }
                        else -> {
                            _uiState.update { it.copy(isEmailCodeSending = false, emailGeneralError = errorMsg) }
                        }
                    }
                }
            } catch (_: IOException) {
                _uiState.update {
                    it.copy(isEmailCodeSending = false, emailGeneralError = "No se pudo conectar con el servidor. Revisa tu conexión a internet e inténtalo de nuevo")
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(isEmailCodeSending = false, emailGeneralError = "Ocurrió un error inesperado. Inténtalo de nuevo")
                }
            }
        }
    }

    private fun confirmarCambioCorreo() {
        val currentState = _uiState.value
        val code = currentState.emailOtpCode.joinToString("")
        if (code.length != 6) return

        viewModelScope.launch {
            _uiState.update { it.copy(isEmailConfirming = true, emailOtpErrorMessage = null) }
            try {
                val request = ConfirmarCambioCorreoRequest(
                    correoNuevo = currentState.newEmailInput.trim(),
                    codigo = code
                )
                val response = authApi.confirmarCambioCorreo(request)
                if (response.isSuccessful) {
                    // Refrescar usuario inmediatamente en CurrentUserStore
                    CurrentUserStore.refresh(authApi)
                    limpiarEstadoMemoriaCorreo()
                    _uiState.update { it.copy(snackbarSuccessMessage = "Correo actualizado") }
                } else {
                    val rawError = response.errorBody()?.string()
                    val errorMsg = parseErrorMessage(rawError, "El código es incorrecto o ya venció")
                    if (response.code() == 409) {
                        _uiState.update {
                            it.copy(
                                isEmailConfirming = false,
                                emailStep = EmailFlowStep.NEW_EMAIL,
                                newEmailError = "Ese correo ya está registrado"
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(isEmailConfirming = false, emailOtpErrorMessage = errorMsg)
                        }
                    }
                }
            } catch (_: IOException) {
                _uiState.update {
                    it.copy(isEmailConfirming = false, emailOtpErrorMessage = "No se pudo conectar con el servidor. Revisa tu conexión a internet e inténtalo de nuevo")
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(isEmailConfirming = false, emailOtpErrorMessage = "Ocurrió un error inesperado. Inténtalo de nuevo")
                }
            }
        }
    }

    private fun startEmailResendTimer() {
        emailResendTimerJob?.cancel()
        _uiState.update { it.copy(canResendEmailCode = false, emailResendCountdown = 60) }
        emailResendTimerJob = viewModelScope.launch {
            for (i in 60 downTo 1) {
                _uiState.update { it.copy(emailResendCountdown = i) }
                delay(1000)
            }
            _uiState.update { it.copy(canResendEmailCode = true, emailResendCountdown = 0) }
        }
    }

    private fun limpiarEstadoMemoriaCorreo() {
        emailResendTimerJob?.cancel()
        _uiState.update {
            it.copy(
                emailStep = EmailFlowStep.NONE,
                newEmailInput = "",
                emailCurrentPasswordInput = "",
                emailCurrentPasswordError = null,
                newEmailError = null,
                isEmailCodeSending = false,
                emailGeneralError = null,
                emailOtpCode = List(6) { "" },
                isEmailConfirming = false,
                emailOtpErrorMessage = null,
                emailResendCountdown = 0,
                canResendEmailCode = false,
                googleTokenForEmailChange = null
            )
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

    /**
     * Abre la ventana de Google para que una cuenta sin contraseña confirme que es suya,
     * y con ese token desactiva ([eliminar] = false) o programa la eliminación ([eliminar] = true).
     */
    fun confirmarConGoogle(context: Context, eliminar: Boolean) {
        if (_uiState.value.isGoogleConfirming || _uiState.value.isDeactivating || _uiState.value.isDeleting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isGoogleConfirming = true, deactivateError = null, deleteError = null) }
            val resultado = GoogleAuthManager(context).getFirebaseIdToken()
            _uiState.update { it.copy(isGoogleConfirming = false) }
            resultado.fold(
                onSuccess = { token ->
                    // token == null: cerró la ventana de Google sin elegir cuenta; no se muestra error
                    if (token != null) {
                        if (eliminar) eliminarCuenta(token) else desactivarCuenta(token)
                    }
                },
                onFailure = { error ->
                    val msg = error.message ?: "No se pudo confirmar con Google. Inténtalo de nuevo"
                    _uiState.update {
                        if (eliminar) it.copy(deleteError = msg) else it.copy(deactivateError = msg)
                    }
                }
            )
        }
    }

    private fun eliminarCuenta(googleToken: String?) {
        val currentState = _uiState.value
        if (currentState.isDeleting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, deleteError = null) }
            try {
                val request = DesactivarCuentaRequest(
                    contrasena = currentState.deletePasswordInput.takeIf { it.isNotBlank() },
                    googleToken = googleToken
                )
                val response = authApi.eliminarCuenta(request)
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    sessionManager.clearSession()
                    // La fecha en el idioma de la app ("5 de diciembre de 2026" / "December 5, 2026")
                    val locale = getApplication<Application>().resources.configuration.locales[0]
                    val fecha = runCatching { PlanDates.formatear(body.fechaEliminacion, locale) }
                        .getOrDefault(body.fechaEliminacionTexto)
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            showDeleteDialog = false,
                            deletePasswordInput = "",
                            deletionScheduledDate = fecha
                        )
                    }
                } else {
                    val rawError = response.errorBody()?.string()
                    val errorMsg = parseErrorMessage(rawError, "No se pudo eliminar la cuenta. Inténtalo de nuevo")
                    _uiState.update { it.copy(isDeleting = false, deleteError = errorMsg) }
                }
            } catch (_: IOException) {
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        deleteError = "No se pudo conectar con el servidor. Revisa tu conexión a internet e inténtalo de nuevo"
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(isDeleting = false, deleteError = "Ocurrió un error inesperado. Inténtalo de nuevo")
                }
            }
        }
    }

    /** Mensaje de error de la API: primero el detalle (más claro), si no, el error general. */
    private fun parseErrorMessage(rawError: String?, defaultMsg: String): String {
        if (rawError.isNullOrBlank()) return defaultMsg
        return try {
            val json = JSONObject(rawError)
            val detalle = json.optJSONArray("detalles")?.optString(0).orEmpty()
            detalle.ifBlank { json.optString("error", defaultMsg) }
        } catch (_: Exception) {
            defaultMsg
        }
    }

    override fun onCleared() {
        super.onCleared()
        resendTimerJob?.cancel()
        emailResendTimerJob?.cancel()
        limpiarEstadoMemoriaContrasena()
        limpiarEstadoMemoriaCorreo()
    }
}
