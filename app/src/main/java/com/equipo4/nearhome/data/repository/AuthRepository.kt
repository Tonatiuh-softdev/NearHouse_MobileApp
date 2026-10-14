package com.equipo4.nearhome.data.repository

import com.equipo4.nearhome.network.AuthApi
import com.equipo4.nearhome.network.CheckTelefonoRequest
import com.equipo4.nearhome.network.NetworkClient

class AuthRepository(
    private val authApi: AuthApi = NetworkClient.authApi
) {

    /**
     * Consulta /api/v1/auth/check-telefono para verificar que el número esté disponible.
     */
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
}