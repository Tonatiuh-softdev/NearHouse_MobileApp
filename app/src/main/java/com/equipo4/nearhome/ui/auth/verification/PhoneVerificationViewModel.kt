package com.equipo4.nearhome.ui.auth.verification

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.model.RegistroTemporal
import com.equipo4.nearhome.data.remote.TelefonoFirebase
import com.equipo4.nearhome.data.repository.AuthRepository
import com.equipo4.nearhome.util.AppStrings
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * Pantalla del SMS: manda el código con Firebase, lo revisa y hace el registro final en la API
 * con todos los datos guardados en [RegistroTemporal] más el token de Firebase.
 */
class PhoneVerificationViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhoneVerificationUiState())
    val uiState: StateFlow<PhoneVerificationUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    // Lo que da Firebase al mandar el SMS
    private var verificationId: String? = null
    private var tokenReenvio: PhoneAuthProvider.ForceResendingToken? = null
    private var primerEnvioHecho = false

    // Si el SMS ya se verificó pero el registro en la API falló (por ejemplo, sin internet),
    // se reusa este token al reintentar: el mismo código de SMS no sirve dos veces en Firebase.
    private var tokenFirebaseVerificado: String? = null

    fun setPhoneNumber(number: String) {
        if (number.isNotEmpty()) {
            _uiState.update { it.copy(phoneNumber = number) }
        }
    }

    /** Se llama al abrir la pantalla. Manda el primer SMS una sola vez, aunque se gire la pantalla. */
    fun enviarPrimerCodigo(activity: Activity) {
        if (primerEnvioHecho) return
        primerEnvioHecho = true
        enviarCodigo(activity, reenvio = false)
    }

    fun onOtpCodeChanged(newCode: String) {
        if (newCode.length <= 6 && newCode.all { it.isDigit() }) {
            _uiState.update {
                it.copy(
                    otpCode = newCode,
                    isError = false,
                    errorMessage = null
                )
            }
        }
    }

    fun onVerifyClicked() {
        val state = _uiState.value
        if (state.isLoading) return

        // Reintento después de que falló la API: el SMS ya está verificado
        tokenFirebaseVerificado?.let { token ->
            registrar(token)
            return
        }

        val id = verificationId
        if (id == null) {
            mostrarError(AppStrings.get(R.string.phone_sms_wait_for_sms))
            return
        }
        if (state.otpCode.length != 6) {
            mostrarError(AppStrings.get(R.string.phone_sms_enter_6))
            return
        }
        verificarCredencial(TelefonoFirebase.credencialConCodigo(id, state.otpCode))
    }

    /** "Reenviar SMS" */
    fun onResendSmsClicked(activity: Activity) {
        val state = _uiState.value
        if (state.canResend && !state.isSending && !state.isLoading) {
            enviarCodigo(activity, reenvio = true)
        }
    }

    private fun enviarCodigo(activity: Activity, reenvio: Boolean) {
        val telefono = RegistroTemporal.telefono.orEmpty() // ya viene con +52
        if (telefono.isBlank()) {
            mostrarError(AppStrings.get(R.string.phone_sms_error_restart))
            return
        }

        tokenFirebaseVerificado = null
        _uiState.update {
            it.copy(isSending = true, canResend = false, otpCode = "", isError = false, errorMessage = null)
        }

        TelefonoFirebase.enviarCodigo(
            activity = activity,
            telefono = telefono,
            tokenReenvio = if (reenvio) tokenReenvio else null,
            eventos = object : TelefonoFirebase.Eventos {
                override fun codigoEnviado(
                    verificationId: String,
                    tokenReenvio: PhoneAuthProvider.ForceResendingToken
                ) {
                    this@PhoneVerificationViewModel.verificationId = verificationId
                    this@PhoneVerificationViewModel.tokenReenvio = tokenReenvio
                    _uiState.update { it.copy(isSending = false) }
                    startResendTimer()
                }

                override fun verificadoAutomaticamente(credencial: PhoneAuthCredential) {
                    _uiState.update { it.copy(isSending = false) }
                    verificarCredencial(credencial)
                }

                override fun error(mensaje: String) {
                    _uiState.update { it.copy(isSending = false, canResend = true) }
                    mostrarError(mensaje)
                }
            }
        )
    }

    /** Firebase revisa el código; si está bien, sigue con el registro en la API. */
    private fun verificarCredencial(credencial: PhoneAuthCredential) {
        val state = _uiState.value
        if (state.isLoading || state.isVerified) return

        _uiState.update { it.copy(isLoading = true, isError = false, errorMessage = null) }
        viewModelScope.launch {
            val token = try {
                TelefonoFirebase.obtenerToken(credencial)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                mostrarError(e.message ?: AppStrings.get(R.string.signup_otp_error))
                return@launch
            }
            tokenFirebaseVerificado = token
            registrarEnApi(token)
        }
    }

    private fun registrar(tokenFirebase: String) {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, isError = false, errorMessage = null) }
        viewModelScope.launch { registrarEnApi(tokenFirebase) }
    }

    /** Registro final: datos del registro + comprobante del correo + token de Firebase. */
    private suspend fun registrarEnApi(tokenFirebase: String) {
        val result = authRepository.registrarUsuario(
            correo = RegistroTemporal.correo.orEmpty(),
            contrasena = RegistroTemporal.contrasena.orEmpty(),
            nombre = RegistroTemporal.nombre.orEmpty(),
            primerApellido = RegistroTemporal.primerApellido.orEmpty(),
            segundoApellido = RegistroTemporal.segundoApellido?.takeIf { it.isNotBlank() },
            telefono = RegistroTemporal.telefono.orEmpty(),
            comprobanteCorreo = RegistroTemporal.comprobanteCorreo.orEmpty(),
            tokenFirebase = tokenFirebase
        )

        if (result.isSuccess) {
            RegistroTemporal.limpiar()
            tokenFirebaseVerificado = null
            timerJob?.cancel()
            _uiState.update { it.copy(isLoading = false, isVerified = true) }
        } else {
            _uiState.update { it.copy(isLoading = false) }
            mostrarError(
                result.exceptionOrNull()?.message ?: AppStrings.get(R.string.phone_verify_register_error)
            )
        }
    }

    private fun mostrarError(mensaje: String) {
        _uiState.update { it.copy(isError = true, errorMessage = mensaje) }
    }

    private fun startResendTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(canResend = false, timerSeconds = TelefonoFirebase.ESPERA_SEGUNDOS) }
        timerJob = viewModelScope.launch {
            while (_uiState.value.timerSeconds > 0) {
                delay(1000L)
                _uiState.update { it.copy(timerSeconds = it.timerSeconds - 1) }
            }
            _uiState.update { it.copy(canResend = true) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
