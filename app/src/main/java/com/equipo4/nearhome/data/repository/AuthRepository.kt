package com.equipo4.nearhome.data.repository

import com.equipo4.nearhome.network.AuthApi
import com.equipo4.nearhome.network.CheckTelefonoRequest
import com.equipo4.nearhome.network.LoginResponse
import com.equipo4.nearhome.network.NetworkClient
import com.equipo4.nearhome.network.RegistroFinalRequest

import com.equipo4.nearhome.util.AppStrings
import com.equipo4.nearhome.R

class AuthRepository(
    private val authApi: AuthApi = NetworkClient.authApi
) {

    suspend fun checkTelefono(telefono: String): Result<Unit> {
        return try {
            val response = authApi.checkTelefonoDisponible(CheckTelefonoRequest(telefono))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(AppStrings.get(R.string.auth_error_phone_taken)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(AppStrings.get(R.string.auth_error_connection, e.message.orEmpty())))
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
        tokenFirebase: String = "token_fcm_dev_mock"
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
                val errorDetail = response.errorBody()?.string() ?: AppStrings.get(R.string.auth_error_invalid_data)
                Result.failure(Exception(AppStrings.get(R.string.auth_error_server, response.code(), errorDetail)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(AppStrings.get(R.string.auth_error_comm, e.message.orEmpty())))
        }
    }
}