package com.equipo4.nearhome.data.remote.api

import com.equipo4.nearhome.data.remote.dto.AuthResponse
import com.equipo4.nearhome.data.remote.dto.GoogleAuthRequest
import com.equipo4.nearhome.data.remote.dto.GoogleRegisterRequest
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    /**
     * Autenticación inicial con Google:
     * - Si ya tiene cuenta: regresa token de sesión y datos de usuario.
     * - Si es usuario nuevo: regresa requiresRegistration = true y el googleProofToken.
     */
    @POST("api/v1/auth/google")
    suspend fun autenticarConGoogle(
        @Body request: GoogleAuthRequest
    ): Response<JsonObject>

    /**
     * Registro final para usuario nuevo de Google (+ SMS)
     */
    @POST("api/v1/auth/register/google")
    suspend fun registrarConGoogleFinal(
        @Body request: GoogleRegisterRequest
    ): Response<AuthResponse>
}