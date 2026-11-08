package com.equipo4.nearhome.data.repository

import com.equipo4.nearhome.data.remote.dto.GoogleAuthRequestDto
import com.equipo4.nearhome.data.remote.dto.GoogleAuthResponseDto
import com.equipo4.nearhome.network.AuthApi
import com.equipo4.nearhome.network.CheckTelefonoRequest
import com.equipo4.nearhome.network.LoginResponse
import com.equipo4.nearhome.network.NetworkClient
import com.equipo4.nearhome.network.RegistroFinalRequest
import com.equipo4.nearhome.util.AppStrings
import com.equipo4.nearhome.util.mensajeDeErrorApi
import com.equipo4.nearhome.R

class AuthRepository(
    private val authApi: AuthApi = NetworkClient.authApi
) {

    suspend fun checkTelefono(telefono: String): Result<Unit> {
        return try {
            val response = authApi.checkTelefonoDisponible(CheckTelefonoRequest(telefono))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else if (response.code() == 409) {
                Result.failure(Exception(AppStrings.get(R.string.auth_error_phone_taken)))
            } else {
                val mensaje = mensajeDeErrorApi(
                    response.errorBody()?.string(),
                    AppStrings.get(R.string.auth_error_invalid_data)
                )
                Result.failure(Exception(mensaje))
            }
        } catch (e: Exception) {
            Result.failure(Exception(AppStrings.get(R.string.auth_error_connection, e.message.orEmpty())))
        }
    }

    /**
     * Consume POST /auth/google enviando el idToken obtenido de Firebase.
     */
    suspend fun loginWithGoogle(firebaseToken: String): Result<GoogleAuthResponseDto> {
        return try {
            val response = authApi.loginWithGoogle(GoogleAuthRequestDto(firebaseToken))
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                val errorDetail = response.errorBody()?.string() ?: AppStrings.get(R.string.auth_error_invalid_data)
                Result.failure(Exception(AppStrings.get(R.string.auth_error_server, response.code(), errorDetail)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(AppStrings.get(R.string.auth_error_comm, e.message.orEmpty())))
        }
    }

    /**
     * Consume POST /api/v1/auth/registro enviando todos los datos consolidados en RAM.
     */
    suspend fun registrarUsuario(
        correo: String,
        contrasena: String,
        nombre: String,
        primerApellido: String,
        segundoApellido: String?,
        telefono: String,
        comprobanteCorreo: String,
        tokenFirebase: String // el que da Firebase después de verificar el SMS
    ): Result<LoginResponse> {
        return try {
            val request = RegistroFinalRequest(
                correo = correo,
                contrasena = contrasena,
                nombre = nombre,
                primerApellido = primerApellido,
                segundoApellido = segundoApellido,
                telefono = telefono,
                comprobanteCorreo = comprobanteCorreo,
                tokenFirebase = tokenFirebase
            )
            val response = authApi.registrarUsuario(request)
            val body = response.body()

            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                // Por ejemplo: "El número que verificaste por SMS no coincide con el que escribiste"
                val mensaje = mensajeDeErrorApi(
                    response.errorBody()?.string(),
                    AppStrings.get(R.string.auth_error_invalid_data)
                )
                Result.failure(Exception(mensaje))
            }
        } catch (e: Exception) {
            Result.failure(Exception(AppStrings.get(R.string.auth_error_comm, e.message.orEmpty())))
        }
    }
}