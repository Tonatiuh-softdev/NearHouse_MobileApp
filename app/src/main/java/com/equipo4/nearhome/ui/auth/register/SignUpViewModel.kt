package com.equipo4.nearhome.ui.auth.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.di.NetworkModule
import com.equipo4.nearhome.network.AuthApi
import com.equipo4.nearhome.network.RequestCodigoCorreo
import com.equipo4.nearhome.network.VerifyCodigoCorreoRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException
import com.equipo4.nearhome.data.model.RegistroTemporal


class SignUpViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val authApi: AuthApi = NetworkModule.provideRetrofit(
        NetworkModule.provideOkHttpClient(sessionManager)
    ).create(AuthApi::class.java)

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword) }
    }

    fun onTermsAcceptedChanged(isAccepted: Boolean) {
        _uiState.update { it.copy(isTermsAccepted = isAccepted, showTermsError = false) }
    }

    fun onSubmitForm() {
        val state = _uiState.value
        if (!state.isTermsAccepted) {
            _uiState.update { it.copy(showTermsError = true) }
            return
        }
        if (!state.isFormValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, emailErrorMessage = null) }
            try {
                val cleanEmail = state.email.trim()
                val response = authApi.solicitarCodigoCorreo(RequestCodigoCorreo(correo = cleanEmail))

                if (response.isSuccessful) {
                    // Guardar correo y contraseña estrictamente en memoria RAM
                    RegistroTemporal.correo = cleanEmail
                    RegistroTemporal.contrasena = state.password

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            step = SignUpStep.OTP_VERIFICATION
                        )
                    }
                    startResendTimer()
                } else {
                    val rawError = response.errorBody()?.string()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            emailErrorMessage = parseError(rawError, "El correo ya está registrado o no es válido.")
                        )
                    }
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        emailErrorMessage = "No se pudo conectar con el servidor. Revisa tu conexión a internet."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, emailErrorMessage = "Ocurrió un error inesperado.")
                }
            }
        }
    }

    fun onOtpDigitChanged(index: Int, digit: String) {
        val currentOtp = _uiState.value.otpCode.toMutableList()
        currentOtp[index] = digit
        _uiState.update { it.copy(otpCode = currentOtp, isOtpError = false, otpErrorMessage = null) }
    }

    fun onVerifyOtp() {
        val state = _uiState.value
        val fullCode = state.otpCode.joinToString("")
        if (fullCode.length != 6) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isOtpError = false, otpErrorMessage = null) }
            try {
                val response = authApi.verificarCodigoCorreo(
                    VerifyCodigoCorreoRequest(
                        correo = RegistroTemporal.correo.orEmpty(),
                        codigo = fullCode
                    )
                )

                if (response.isSuccessful && response.body() != null) {
                    // Guardar comprobante JWT emitido por la API
                    RegistroTemporal.comprobanteCorreo = response.body()!!.comprobanteCorreo

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            step = SignUpStep.SUCCESS
                        )
                    }
                } else {
                    val rawError = response.errorBody()?.string()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isOtpError = true,
                            otpErrorMessage = parseError(rawError, "El código es incorrecto o ha expirado.")
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isOtpError = true,
                        otpErrorMessage = "Error al conectar con el servidor."
                    )
                }
            }
        }
    }

    fun onResendCode() {
        if (!_uiState.value.canResendCode) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = authApi.solicitarCodigoCorreo(
                    RequestCodigoCorreo(correo = RegistroTemporal.correo.orEmpty())
                )
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false) }
                    startResendTimer()
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onBackToForm() {
        timerJob?.cancel()
        _uiState.update { it.copy(step = SignUpStep.FORM) }
    }

    private fun startResendTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(canResendCode = false, resendCountdown = 30) }

        timerJob = viewModelScope.launch {
            for (i in 30 downTo 1) {
                _uiState.update { it.copy(resendCountdown = i) }
                delay(1000)
            }
            _uiState.update { it.copy(canResendCode = true, resendCountdown = 0) }
        }
    }

    private fun parseError(jsonError: String?, defaultMessage: String): String {
        return try {
            val json = JSONObject(jsonError ?: "")
            json.optString("error", defaultMessage)
        } catch (e: Exception) {
            defaultMessage
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}