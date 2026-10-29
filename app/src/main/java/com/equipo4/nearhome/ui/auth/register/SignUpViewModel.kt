package com.equipo4.nearhome.ui.auth.register

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.model.RegistroTemporal
import com.equipo4.nearhome.data.remote.GoogleAuthManager
import com.equipo4.nearhome.di.NetworkModule
import com.equipo4.nearhome.network.AuthApi
import com.equipo4.nearhome.network.RequestCodigoCorreo
import com.equipo4.nearhome.network.VerifyCodigoCorreoRequest
import com.equipo4.nearhome.util.AppStrings
import com.equipo4.nearhome.util.mensajeDeErrorApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

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

    /**
     * Entregable 1: Inicia la autenticación de Google desde la pantalla de Registro
     */
    fun onGoogleLoginClicked(context: Context) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    emailErrorMessage = null,
                    googleSuccessMessage = null
                )
            }

            val googleAuthManager = GoogleAuthManager(context)
            val result = googleAuthManager.getFirebaseIdToken()

            result.fold(
                onSuccess = { firebaseToken ->
                    _uiState.update { it.copy(isLoading = false) }
                    if (firebaseToken != null) {
                        // Requisito Entregable 1: Mostrar mensaje temporal al obtener el token
                        _uiState.update { it.copy(googleSuccessMessage = "Cuenta de Google lista") }
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            emailErrorMessage = error.message ?: "No se pudo iniciar sesión con Google. Inténtalo de nuevo."
                        )
                    }
                }
            )
        }
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
                            emailErrorMessage = parseError(rawError, AppStrings.get(R.string.signup_error_email_taken))
                        )
                    }
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        emailErrorMessage = AppStrings.get(R.string.signup_error_network)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, emailErrorMessage = AppStrings.get(R.string.error_unexpected))
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
                            otpErrorMessage = parseError(rawError, AppStrings.get(R.string.signup_error_otp_expired))
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isOtpError = true,
                        otpErrorMessage = AppStrings.get(R.string.error_server_connection)
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
                    _uiState.update { it.copy(isLoading = false, isOtpError = false, otpErrorMessage = null) }
                    startResendTimer()
                } else {
                    // Por ejemplo 429: "Espera 20 segundos antes de pedir otro código."
                    val mensaje = parseError(
                        response.errorBody()?.string(),
                        AppStrings.get(R.string.error_server_connection)
                    )
                    _uiState.update { it.copy(isLoading = false, isOtpError = true, otpErrorMessage = mensaje) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isOtpError = true,
                        otpErrorMessage = AppStrings.get(R.string.signup_error_network)
                    )
                }
            }
        }
    }

    fun onBackToForm() {
        timerJob?.cancel()
        _uiState.update { it.copy(step = SignUpStep.FORM) }
    }

    private fun startResendTimer() {
        timerJob?.cancel()
        // La API solo deja pedir otro código cada 60 segundos
        _uiState.update { it.copy(canResendCode = false, resendCountdown = ESPERA_REENVIO_SEGUNDOS) }

        timerJob = viewModelScope.launch {
            for (i in ESPERA_REENVIO_SEGUNDOS downTo 1) {
                _uiState.update { it.copy(resendCountdown = i) }
                delay(1000)
            }
            _uiState.update { it.copy(canResendCode = true, resendCountdown = 0) }
        }
    }

    // Usa "detalles" de la API cuando vienen (por ejemplo "Te quedan 4 intentos") y si no, "error"
    private fun parseError(jsonError: String?, defaultMessage: String): String =
        mensajeDeErrorApi(jsonError, defaultMessage)

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    private companion object {
        const val ESPERA_REENVIO_SEGUNDOS = 60
    }
}