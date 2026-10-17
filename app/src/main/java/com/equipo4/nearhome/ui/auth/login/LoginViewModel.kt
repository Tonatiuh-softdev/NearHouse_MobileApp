package com.equipo4.nearhome.ui.auth.login

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.di.NetworkModule
import com.equipo4.nearhome.network.AuthApi
import com.equipo4.nearhome.network.LoginRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class LoginViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val authApi: AuthApi = NetworkModule.provideRetrofit(
        NetworkModule.provideOkHttpClient(sessionManager)
    ).create(AuthApi::class.java)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        val isValid = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
        _uiState.update {
            it.copy(
                email = email,
                isEmailValid = isValid,
                errorMessage = null
            )
        }
    }

    fun onPasswordChanged(password: String) {
        val isValid = password.isNotEmpty()
        _uiState.update {
            it.copy(
                password = password,
                isPasswordValid = isValid,
                errorMessage = null
            )
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update {
            it.copy(isPasswordVisible = !it.isPasswordVisible)
        }
    }

    fun onLoginClicked() {
        val currentState = _uiState.value
        // Evita enviar dos veces la misma petición
        if (!currentState.isFormValid || currentState.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = authApi.login(
                    LoginRequest(
                        correo = currentState.email.trim(),
                        contrasena = currentState.password
                    )
                )

                val body = response.body()
                if (response.isSuccessful && body != null) {
                    // Persistencia: token y datos básicos (la contraseña jamás se guarda)
                    sessionManager.saveSession(
                        token = body.token,
                        userId = body.usuario.id,
                        nombre = body.usuario.nombre,
                        rol = body.usuario.rol
                    )
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                } else {
                    val rawError = try {
                        response.errorBody()?.string()
                    } catch (e: Exception) {
                        null
                    }
                    val errorMessage = parseErrorMessage(response.code(), rawError)
                    _uiState.update { it.copy(isLoading = false, errorMessage = errorMessage) }
                }
            } catch (e: CancellationException) {
                // No tragarse la cancelación de la corrutina
                throw e
            } catch (e: IOException) {
                // Sin internet, timeout o API apagada
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo conectar con el servidor. Revisa tu conexión a internet e inténtalo de nuevo."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Ocurrió un error inesperado."
                    )
                }
            }
        }
    }

    /**
     * Prioridad: 1) el mensaje que manda la API; 2) un texto según el código HTTP.
     * Así "Demasiados intentos" y "Cuenta suspendida" ya no se convierten en
     * "Correo o contraseña incorrectos".
     */
    private fun parseErrorMessage(code: Int, rawError: String?): String {
        val apiMessage = try {
            val json = JSONObject(rawError ?: "")
            listOf("error", "message", "mensaje", "detail").firstNotNullOfOrNull { key ->
                json.optString(key, "").takeIf { it.isNotBlank() }
            }
        } catch (e: Exception) {
            null
        }
        if (apiMessage != null) return apiMessage

        return when (code) {
            401 -> "Correo o contraseña incorrectos"
            403 -> "Tu cuenta está suspendida. Contacta al administrador"
            429 -> "Demasiados intentos. Espera unos minutos"
            else -> "Ocurrió un error inesperado. Inténtalo de nuevo."
        }
    }

    fun onGoogleLoginClicked() {
        // Fuera del alcance de esta tarea
    }
}