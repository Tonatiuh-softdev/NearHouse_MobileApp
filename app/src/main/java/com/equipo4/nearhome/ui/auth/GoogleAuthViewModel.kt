package com.equipo4.nearhome.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.remote.dto.GoogleAuthRequest
import com.equipo4.nearhome.data.remote.dto.GoogleProofResponse
import com.equipo4.nearhome.data.remote.dto.GoogleRegisterRequest
import com.equipo4.nearhome.network.AuthApi
import com.equipo4.nearhome.network.NetworkClient
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

sealed class GoogleAuthState {
    object Idle : GoogleAuthState()
    object Loading : GoogleAuthState()
    object SessionSuccess : GoogleAuthState()
    data class RequiresRegistration(val proofData: GoogleProofResponse) : GoogleAuthState()
    data class Error(val mensaje: String) : GoogleAuthState()
}

class GoogleAuthViewModel(
    private val apiService: AuthApi,
    private val sessionManager: SessionManager,
    private val gson: Gson = Gson()
) : ViewModel() {

    private val _uiState = MutableStateFlow<GoogleAuthState>(GoogleAuthState.Idle)
    val uiState: StateFlow<GoogleAuthState> = _uiState.asStateFlow()

    /**
     * Inicia autenticación con Google usando el token obtenido de Firebase.
     */
    fun procesarTokenGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = GoogleAuthState.Loading
            try {
                val response = apiService.autenticarConGoogle(GoogleAuthRequest(idToken))
                if (response.isSuccessful && response.body() != null) {
                    val json: JsonObject = response.body()!!

                    if (json.has("requiresRegistration") && json.get("requiresRegistration").asBoolean) {
                        // Caso C: Usuario nuevo -> Guarda comprobante en memoria y pasa a Completar Perfil
                        val proofResponse = gson.fromJson(json, GoogleProofResponse::class.java)
                        _uiState.value = GoogleAuthState.RequiresRegistration(proofResponse)
                    } else {
                        // Casos A y B: Usuario existente o correo vinculado -> Inicia sesión
                        sessionManager.setLoggedIn(true)
                        _uiState.value = GoogleAuthState.SessionSuccess
                    }
                } else {
                    val mensajeError = extraerMensajeError(response.errorBody()?.string())
                    _uiState.value = GoogleAuthState.Error(mensajeError)
                }
            } catch (_: IOException) {
                _uiState.value = GoogleAuthState.Error("No se pudo conectar con el servidor. Revisa tu conexión a internet e inténtalo de nuevo")
            } catch (_: Exception) {
                _uiState.value = GoogleAuthState.Error("Ocurrió un error inesperado. Inténtalo de nuevo")
            }
        }
    }

    /**
     * Envía los datos finales de registro desde la pantalla Completar Perfil
     */
    fun registrarConGoogleFinal(
        nombre: String,
        apellidos: String,
        telefono: String,
        googleProofToken: String,
        smsToken: String
    ) {
        viewModelScope.launch {
            _uiState.value = GoogleAuthState.Loading
            try {
                val req = GoogleRegisterRequest(
                    nombre = nombre.trim(),
                    apellidos = apellidos.trim(),
                    telefono = telefono.trim(),
                    googleProofToken = googleProofToken,
                    smsToken = smsToken
                )
                val response = apiService.registrarConGoogleFinal(req)
                if (response.isSuccessful && response.body() != null) {
                    sessionManager.setLoggedIn(true)
                    _uiState.value = GoogleAuthState.SessionSuccess
                } else {
                    val mensajeError = extraerMensajeError(response.errorBody()?.string())
                    _uiState.value = GoogleAuthState.Error(mensajeError)
                }
            } catch (_: IOException) {
                _uiState.value = GoogleAuthState.Error("No se pudo conectar con el servidor. Revisa tu conexión a internet e inténtalo de nuevo")
            } catch (_: Exception) {
                _uiState.value = GoogleAuthState.Error("Ocurrió un error inesperado. Inténtalo de nuevo")
            }
        }
    }

    private fun extraerMensajeError(errorJson: String?): String {
        if (errorJson.isNullOrBlank()) return "Error al procesar la solicitud"
        return try {
            val json = gson.fromJson(errorJson, JsonObject::class.java)
            json.get("error")?.asString ?: "Error al procesar la solicitud"
        } catch (_: Exception) {
            "Error al procesar la solicitud"
        }
    }

    fun reiniciarEstado() {
        _uiState.value = GoogleAuthState.Idle
    }
}

class GoogleAuthViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GoogleAuthViewModel::class.java)) {
            val sessionManager = SessionManager(context)
            val apiService = NetworkClient.authApi
            return GoogleAuthViewModel(apiService, sessionManager) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}