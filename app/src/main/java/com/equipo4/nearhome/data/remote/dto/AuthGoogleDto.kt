package com.equipo4.nearhome.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Petición que se envía a la API con el token obtenido de Firebase.
 */
data class GoogleAuthRequestDto(
    @SerializedName("idToken") val idToken: String
)

/**
 * Respuesta que devuelve la API.
 */
data class GoogleAuthResponseDto(
    @SerializedName("status") val status: String, // "SUCCESS", "REQUIRES_COMPLETION", "SUSPENDED"
    @SerializedName("message") val message: String? = null,

    // Si el usuario ya existe y la sesión inicia directo (Casos A y B)
    @SerializedName("sessionToken") val sessionToken: String? = null,
    @SerializedName("userId") val userId: String? = null,

    // Si es un usuario nuevo que debe completar su perfil (Caso C)
    @SerializedName("googleProofToken") val googleProofToken: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null
)