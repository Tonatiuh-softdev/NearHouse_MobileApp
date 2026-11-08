package com.equipo4.nearhome.ui.auth.recover

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.di.NetworkModule
import com.equipo4.nearhome.network.AuthApi
import com.equipo4.nearhome.network.NuevaContrasenaRecuperarRequest
import com.equipo4.nearhome.network.SolicitarCodigoRecuperarRequest
import com.equipo4.nearhome.network.VerificarCodigoRecuperarRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException

class RecoverPasswordViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val authApi: AuthApi = NetworkModule.provideRetrofit(
        NetworkModule.provideOkHttpClient(sessionManager)
    ).create(AuthApi::class.java)

    private val _uiState = MutableStateFlow(RecoverPasswordUiState())
    val uiState: StateFlow<RecoverPasswordUiState> = _uiState.asStateFlow()

    private var resendTimerJob: Job? = null

    fun setPrefilledEmail(email: String) {
        if (email.isNotBlank() && _uiState.value.emailInput.isBlank()) {
            _uiState.update { it.copy(emailInput = email.trim()) }
        }
    }

    fun onEvent(event: RecoverPasswordUiEvent, onSuccessFinished: ((String) -> Unit)? = null) {
        when (event) {
            is RecoverPasswordUiEvent.OnEmailChanged -> {
                _uiState.update { it.copy(emailInput = event.text, emailError = null) }
            }
            RecoverPasswordUiEvent.OnRequestCodeSubmitted -> {
                solicitarCodigoRecuperar()
            }
            is RecoverPasswordUiEvent.OnOtpDigitChanged -> {
                val currentOtp = _uiState.value.otpCode.toMutableList()
                currentOtp[event.index] = event.digit
                _uiState.update { it.copy(otpCode = currentOtp, otpErrorMessage = null) }
            }
            RecoverPasswordUiEvent.OnVerifyCodeSubmitted -> {
                verificarCodigoRecuperar()
            }
            RecoverPasswordUiEvent.OnResendCodeRequested -> {
                if (_uiState.value.canResendCode) {
                    solicitarCodigoRecuperar()
                }
            }
            is RecoverPasswordUiEvent.OnNewPasswordChanged -> {
                _uiState.update { it.copy(newPasswordInput = event.text, passwordError = null) }
            }
            is RecoverPasswordUiEvent.OnConfirmPasswordChanged -> {
                _uiState.update { it.copy(confirmPasswordInput = event.text, passwordError = null) }
            }
            RecoverPasswordUiEvent.OnSaveNewPasswordSubmitted -> {
                guardarNuevaContrasena(onSuccessFinished)
            }
            RecoverPasswordUiEvent.OnReturnToEmailStepRequested -> {
                _uiState.update { it.copy(step = RecoverStep.REQUEST_CODE, otpErrorMessage = null) }
            }
        }
    }

    private fun solicitarCodigoRecuperar() {
        val email = _uiState.value.emailInput.trim()
        if (email.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSendingCode = true, emailError = null) }
            try {
                authApi.solicitarCodigoRecuperar(SolicitarCodigoRecuperarRequest(email))
                _uiState.update {
                    it.copy(
                        isSendingCode = false,
                        step = RecoverStep.VERIFY_CODE,
                        otpCode = List(6) { "" },
                        otpErrorMessage = null
                    )
                }
                startResendTimer()
            } catch (_: IOException) {
                // Incluso ante error de red o Brevo, la respuesta a la UI avanza
                _uiState.update {
                    it.copy(
                        isSendingCode = false,
                        step = RecoverStep.VERIFY_CODE,
                        otpCode = List(6) { "" },
                        otpErrorMessage = null
                    )
                }
                startResendTimer()
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isSendingCode = false,
                        step = RecoverStep.VERIFY_CODE,
                        otpCode = List(6) { "" },
                        otpErrorMessage = null
                    )
                }
                startResendTimer()
            }
        }
    }

    private fun verificarCodigoRecuperar() {
        val currentState = _uiState.value
        val email = currentState.emailInput.trim()
        val code = currentState.otpCode.joinToString("")
        if (code.length != 6) return

        viewModelScope.launch {
            _uiState.update { it.copy(isVerifyingCode = true, otpErrorMessage = null) }
            try {
                val response = authApi.verificarCodigoRecuperar(VerificarCodigoRecuperarRequest(email, code))
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    _uiState.update {
                        it.copy(
                            isVerifyingCode = false,
                            proofToken = body.comprobante,
                            step = RecoverStep.NEW_PASSWORD,
                            otpErrorMessage = null
                        )
                    }
                } else {
                    val rawError = response.errorBody()?.string()
                    val errorMsg = parseErrorMessage(rawError, "El código es incorrecto o ya venció")
                    _uiState.update { it.copy(isVerifyingCode = false, otpErrorMessage = errorMsg) }
                }
            } catch (_: IOException) {
                _uiState.update {
                    it.copy(isVerifyingCode = false, otpErrorMessage = "No se pudo conectar con el servidor. Revisa tu conexión a internet e inténtalo de nuevo")
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(isVerifyingCode = false, otpErrorMessage = "Ocurrió un error inesperado. Inténtalo de nuevo")
                }
            }
        }
    }

    private fun guardarNuevaContrasena(onSuccessFinished: ((String) -> Unit)?) {
        val currentState = _uiState.value
        val proof = currentState.proofToken ?: return
        if (!currentState.isNewPasswordFormValid || currentState.isSavingPassword) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingPassword = true, passwordError = null) }
            try {
                val response = authApi.guardarNuevaContrasenaRecuperar(
                    NuevaContrasenaRecuperarRequest(
                        comprobante = proof,
                        contrasenaNueva = currentState.newPasswordInput
                    )
                )
                if (response.isSuccessful) {
                    limpiarEstadoMemoria()
                    onSuccessFinished?.invoke("Contraseña actualizada. Inicia sesión con tu nueva contraseña")
                } else {
                    val rawError = response.errorBody()?.string()
                    val errorMsg = parseErrorMessage(rawError, "Error al guardar la contraseña")
                    if (response.code() == 409) {
                        _uiState.update {
                            it.copy(
                                isSavingPassword = false,
                                step = RecoverStep.REQUEST_CODE,
                                emailError = "El tiempo para cambiar tu contraseña terminó. Vuelve a empezar."
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isSavingPassword = false, passwordError = errorMsg) }
                    }
                }
            } catch (_: IOException) {
                _uiState.update {
                    it.copy(
                        isSavingPassword = false,
                        passwordError = "No se pudo conectar con el servidor. Revisa tu conexión a internet e inténtalo de nuevo"
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isSavingPassword = false,
                        passwordError = "Ocurrió un error inesperado. Inténtalo de nuevo"
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

    private fun limpiarEstadoMemoria() {
        resendTimerJob?.cancel()
        _uiState.update {
            it.copy(
                step = RecoverStep.REQUEST_CODE,
                emailInput = "",
                isSendingCode = false,
                emailError = null,
                otpCode = List(6) { "" },
                isVerifyingCode = false,
                otpErrorMessage = null,
                resendCountdown = 0,
                canResendCode = false,
                newPasswordInput = "",
                confirmPasswordInput = "",
                isSavingPassword = false,
                passwordError = null,
                proofToken = null
            )
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
        limpiarEstadoMemoria()
    }
}
