package com.equipo4.nearhome.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(
    val correo: String,
    val contrasena: String
)

data class UsuarioDto(
    val id: Int,
    val nombre: String,
    val rol: String
)

data class LoginResponse(
    val token: String,
    val usuario: UsuarioDto
)

interface AuthApi {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}