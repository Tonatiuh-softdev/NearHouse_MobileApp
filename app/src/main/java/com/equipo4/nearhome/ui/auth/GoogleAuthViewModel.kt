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
    data class RequiresReactivation(val idToken: String) : GoogleAuthState()
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
    fun procesarTokenGoogle(idToken: String, reactivar: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = GoogleAuthState.Loading
            try {
                val response = apiService.autenticarConGoogle(GoogleAuthRequest(idToken, reactivar))
                if (response.isSuccessful && response.body() != null) {
                    val json: JsonObject = response.body()!!

                    if (json.has("requiresRegistration") && json.get("requiresRegistration").asBoolean) {
                        // Caso C: Usuario nuevo -> Guarda comprobante en memoria y pasa a Completar Perfil
                        val proofResponse = gson.fromJson(json, GoogleProofResponse::class.java)
                        _uiState.value = GoogleAuthState.RequiresRegistration(proofResponse)
                    } else {
                        // Casos A y B: Usuario existente o correo vinculado -> Inicia sesión
                        guardarSesionDesdeJson(json)
                        _uiState.value = GoogleAuthState.SessionSuccess
                    }
                } else if (response.code() == 409) {
                    _uiState.value = GoogleAuthState.RequiresReactivation(idToken)
                } else {
                    val mensajeError = extraerMensajeError(response.errorBody()?.string())
                    if (mensajeError.contains("CUENTA_DESACTIVADA", ignoreCase = true) || mensajeError.contains("desactivada", ignoreCase = true)) {
                        _uiState.value = GoogleAuthState.RequiresReactivation(idToken)
                    } else {
                        _uiState.value = GoogleAuthState.Error(mensajeError)
                    }
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
                    val json: JsonObject = response.body()!!
                    guardarSesionDesdeJson(json)
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

    private fun guardarSesionDesdeJson(json: JsonObject) {
        val token = if (json.has("token")) json.get("token").asString else ""
        if (json.has("usuario") && !json.get("usuario").isJsonNull) {
            val usuarioObj = json.getAsJsonObject("usuario")
            val id = if (usuarioObj.has("id")) usuarioObj.get("id").asInt else 0
            val nombre = if (usuarioObj.has("nombre")) usuarioObj.get("nombre").asString else ""
            val primerApellido = if (usuarioObj.has("primerApellido") && !usuarioObj.get("primerApellido").isJsonNull) usuarioObj.get("primerApellido").asString else ""
            val nombreCompleto = if (primerApellido.isNotBlank()) "$nombre $primerApellido" else nombre
            val rol = if (usuarioObj.has("rol")) usuarioObj.get("rol").asString else "Comprador"
            val correo = if (usuarioObj.has("correo") && !usuarioObj.get("correo").isJsonNull) usuarioObj.get("correo").asString else null
            val telefono = if (usuarioObj.has("telefono") && !usuarioObj.get("telefono").isJsonNull) usuarioObj.get("telefono").asString else null

            sessionManager.saveSession(
                token = token,
                userId = id,
                nombre = nombreCompleto,
                rol = rol,
                correo = correo,
                telefono = telefono
            )
        } else {
            sessionManager.saveToken(token)
            sessionManager.setLoggedIn(true)
        }
    }

    private fun extraerMensajeError(errorJson: String?): String {
        if (errorJson.isNullOrBlank()) return "Error al procesar la solicitud"
        return try {
            val json = gson.fromJson(errorJson, JsonObject::class.java)
            json.get("error")?.asString
                ?: json.get("mensaje")?.asString
                ?: json.get("message")?.asString
                ?: "Error al procesar la solicitud"
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
