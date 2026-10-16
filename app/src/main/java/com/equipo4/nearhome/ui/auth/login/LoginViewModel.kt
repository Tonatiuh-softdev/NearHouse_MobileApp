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
        if (!currentState.isFormValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = authApi.login(
                    LoginRequest(
                        correo = currentState.email.trim(),
                        contrasena = currentState.password
                    )
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    // Persistencia segura: token y datos del usuario (la contraseña jamás se guarda)
                    sessionManager.saveSession(
                        token = body.token,
                        userId = body.usuario.id,
                        nombre = body.usuario.nombre,
                        rol = body.usuario.rol
                    )
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                } else {
                    val rawError = response.errorBody()?.string()
                    val errorMessage = parseErrorMessage(rawError)
                    _uiState.update { it.copy(isLoading = false, errorMessage = errorMessage) }
                }
            } catch (e: IOException) {
                // Error de red / API apagada en Docker
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

    private fun parseErrorMessage(jsonError: String?): String {
        return try {
            val json = JSONObject(jsonError ?: "")
            json.optString("error", "Correo o contraseña incorrectos")
        } catch (e: Exception) {
            "Correo o contraseña incorrectos"
        }
    }

    fun onGoogleLoginClicked() {

    }
}