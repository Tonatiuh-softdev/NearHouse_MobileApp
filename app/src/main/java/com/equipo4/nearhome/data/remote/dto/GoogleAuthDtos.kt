package com.equipo4.nearhome.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GoogleAuthRequest(
    val idToken: String?
)

@Serializable
data class GoogleRegisterRequest(
    val nombre: String,
    val apellidos: String,
    val telefono: String,
    val googleProofToken: String,
    val smsToken: String
)

@Serializable
data class GoogleProofResponse(
    val requiresRegistration: Boolean = true,
    val googleProofToken: String,
    val nombre: String,
    val apellidos: String,
    val correo: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val usuario: UsuarioDto
)

@Serializable
data class UsuarioDto(
    val id: Int,
    val nombre: String,
    val primerApellido: String,
    val segundoApellido: String? = null,
    val correo: String,
    val telefono: String,
    val rol: String,
    val activo: Boolean,
    val correoVerificado: Boolean,
    val telefonoVerificado: Boolean,
    val fechaRegistro: String
)