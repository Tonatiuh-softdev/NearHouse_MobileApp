package com.equipo4.nearhome.data.repository

import com.equipo4.nearhome.network.AuthApi
import com.equipo4.nearhome.network.CheckTelefonoRequest
import com.equipo4.nearhome.network.LoginResponse
import com.equipo4.nearhome.network.NetworkClient
import com.equipo4.nearhome.network.RegistroFinalRequest

class AuthRepository(
    private val authApi: AuthApi = NetworkClient.authApi
) {

    suspend fun checkTelefono(telefono: String): Result<Unit> {
        return try {
            val response = authApi.checkTelefonoDisponible(CheckTelefonoRequest(telefono))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("El número telefónico ya está registrado"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión con el servidor: ${e.message}"))
        }
    }

    /**
     * Consume POST /api/v1/auth/registro enviando todos los datos consolidados en RAM.
     */
    suspend fun registrarUsuario(
        correo: String,
        contrasena: String,
        nombre: String,
        apellidos: String,
        telefono: String,
        comprobanteCorreo: String,
        tokenFirebase: String = "token_fcm_dev_mock"
    ): Result<LoginResponse> {
        return try {
            val request = RegistroFinalRequest(
                correo = correo,
                contrasena = contrasena,
                nombre = nombre,
                apellidos = apellidos,
                telefono = telefono,
                comprobanteCorreo = comprobanteCorreo,
                tokenFirebase = tokenFirebase
            )
            val response = authApi.registrarUsuario(request)
            val body = response.body()

            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception("Error en el servidor al registrar cuenta (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de comunicación: ${e.message}"))
        }
    }
}