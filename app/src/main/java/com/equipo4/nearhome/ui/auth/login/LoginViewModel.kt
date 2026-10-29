package com.equipo4.nearhome.ui.auth.login

import android.app.Application
import android.content.Context
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.remote.GoogleAuthManager
import com.equipo4.nearhome.di.NetworkModule
import com.equipo4.nearhome.network.AuthApi
import com.equipo4.nearhome.network.LoginRequest
import com.equipo4.nearhome.util.AppStrings
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

    private val _uiState = MutableStateFlow(
        LoginUiState(
            errorMessage = if (sessionManager.consumeSessionExpired())
                AppStrings.get(R.string.login_session_expired)
            else null
        )
    )
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

    fun onLoginClicked(reactivar: Boolean = false) {
        val currentState = _uiState.value
        // Evita enviar dos veces la misma petición
        if (!currentState.isFormValid || currentState.isLoading) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    showReactivateDialog = false
                )
            }
            try {
                val response = authApi.login(
                    LoginRequest(
                        correo = currentState.email.trim(),
                        contrasena = currentState.password,
                        reactivar = reactivar
                    )
                )

                val body = response.body()
                if (response.isSuccessful && body != null) {
                    // Persistencia: token y datos básicos
                    sessionManager.saveSession(
                        token = body.token,
                        userId = body.usuario.id,
                        nombre = body.usuario.nombre,
                        rol = body.usuario.rol,
                        correo = body.usuario.correo
                    )
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            reactivateSuccessMessage = if (reactivar) "Tu cuenta está activa otra vez" else null
                        )
                    }
                } else {
                    val rawError = try {
                        response.errorBody()?.string()
                    } catch (e: Exception) {
                        null
                    }
                    val isDeactivated = response.code() == 409 || (rawError?.contains("CUENTA_DESACTIVADA", ignoreCase = true) == true) || (rawError?.contains("desactivada", ignoreCase = true) == true)
                    if (isDeactivated) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                showReactivateDialog = true
                            )
                        }
                    } else {
                        val errorMessage = parseErrorMessage(response.code(), rawError)
                        _uiState.update { it.copy(isLoading = false, errorMessage = errorMessage) }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = AppStrings.get(R.string.login_error_network)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = AppStrings.get(R.string.error_unexpected)
                    )
                }
            }
        }
    }

    fun onDismissReactivateDialog() {
        _uiState.update { it.copy(showReactivateDialog = false) }
    }

    fun onConfirmReactivate() {
        onLoginClicked(reactivar = true)
    }

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
            401 -> AppStrings.get(R.string.login_error_401)
            403 -> AppStrings.get(R.string.login_error_403)
            429 -> AppStrings.get(R.string.login_error_429)
            else -> AppStrings.get(R.string.error_unexpected_retry)
        }
    }

    fun onGoogleLoginClicked(context: Context, onTokenReceived: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    googleSuccessMessage = null
                )
            }

            val googleAuthManager = GoogleAuthManager(context)
            val result = googleAuthManager.getFirebaseIdToken()

            result.fold(
                onSuccess = { firebaseToken ->
                    _uiState.update { it.copy(isLoading = false) }
                    if (firebaseToken != null) {
                        onTokenReceived(firebaseToken)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "No se pudo iniciar sesión con Google. Inténtalo de nuevo."
                        )
                    }
                }
            )
        }
    }
}
