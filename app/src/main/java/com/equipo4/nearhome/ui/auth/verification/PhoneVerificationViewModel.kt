package com.equipo4.nearhome.ui.auth.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.model.RegistroTemporal
import com.equipo4.nearhome.data.repository.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PhoneVerificationViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhoneVerificationUiState())
    val uiState: StateFlow<PhoneVerificationUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun setPhoneNumber(number: String) {
        if (number.isNotEmpty()) {
            _uiState.update { it.copy(phoneNumber = number) }
        }
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
        val currentCode = _uiState.value.otpCode

        if (currentCode != "123456") {
            _uiState.update {
                it.copy(
                    isError = true,
                    errorMessage = "El código es incorrecto"
                )
            }
            return
        }

        // Código mock (123456) válido -> Consumir API de registro final en Ktor
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isError = false, errorMessage = null) }

            val correo = RegistroTemporal.correo.orEmpty()
            val contrasena = RegistroTemporal.contrasena.orEmpty()
            val nombre = RegistroTemporal.nombre.orEmpty()
            val primerAp = RegistroTemporal.primerApellido.orEmpty()
            val segAp = RegistroTemporal.segundoApellido?.takeIf { it.isNotBlank() }
            val telefono = RegistroTemporal.telefono.orEmpty()
            val comprobante = RegistroTemporal.comprobanteCorreo.orEmpty()

            val result = authRepository.registrarUsuario(
                correo = correo,
                contrasena = contrasena,
                nombre = nombre,
                primerApellido = primerAp,
                segundoApellido = segAp,
                telefono = telefono,
                comprobanteCorreo = comprobante
            )

            if (result.isSuccess) {
                RegistroTemporal.limpiar()
                _uiState.update { it.copy(isLoading = false, isVerified = true) }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isError = true,
                        errorMessage = result.exceptionOrNull()?.message ?: "Error al registrar usuario"
                    )
                }
            }
        }
    }

    fun onResendSmsClicked() {
        if (_uiState.value.canResend) {
            _uiState.update {
                it.copy(
                    otpCode = "",
                    isError = false,
                    errorMessage = null,
                    canResend = false,
                    timerSeconds = 30
                )
            }
            startResendTimer()
        }
    }

    private fun startResendTimer() {
        timerJob?.cancel()
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