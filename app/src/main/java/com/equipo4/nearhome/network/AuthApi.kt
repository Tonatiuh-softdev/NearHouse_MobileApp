package com.equipo4.nearhome.network

import com.equipo4.nearhome.data.remote.dto.GoogleAuthRequestDto
import com.equipo4.nearhome.data.remote.dto.GoogleAuthResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PATCH
import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.PUT

// --- Modelos para Login ---
data class LoginRequest(
    val correo: String,
    val contrasena: String
)

data class LoginResponse(
    val token: String,
    val usuario: UsuarioDto
)

data class UsuarioDto(
    val id: Int,
    val correo: String,
    val nombre: String,
    val rol: String
)

// --- Datos del usuario logueado (/api/v1/usuarios/me) ---
data class UsuarioMeDto(
    val id: Int,
    val nombre: String,
    val primerApellido: String,
    val segundoApellido: String? = null,
    val correo: String,
    val telefono: String,
    val rol: String,
    val activo: Boolean = true,
    val correoVerificado: Boolean = false,
    val telefonoVerificado: Boolean = false,
    val fechaRegistro: String? = null,
    // Todavía no los manda la API. Con valor por defecto no rompen nada.
    val tieneFoto: Boolean = false,
    val fotoVersion: String? = null
)

// --- Modelos para Registro y Verificación ---
data class RequestCodigoCorreo(
    val correo: String
)

data class VerifyCodigoCorreoRequest(
    val correo: String,
    val codigo: String
)

data class VerifyCodigoCorreoResponse(
    val comprobanteCorreo: String
)

data class CheckTelefonoRequest(
    val telefono: String
)

data class RegistroFinalRequest(
    val correo: String,
    val contrasena: String,
    val nombre: String,
    val primerApellido: String,
    val segundoApellido: String? = null,
    val telefono: String,
    val comprobanteCorreo: String,
    val tokenFirebase: String
)

data class UpdateMeRequest(
    val nombre: String,
    val primerApellido: String,
    val segundoApellido: String? = null
)

// --- Interfaz API ---
interface AuthApi {

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // Endpoint para iniciar sesión / registrar con Google
    @POST("api/v1/auth/google")
    suspend fun loginWithGoogle(
        @Body request: GoogleAuthRequestDto
    ): Response<GoogleAuthResponseDto>

    // 1. Solicitar código de 6 dígitos al correo (Brevo)
    @POST("api/v1/auth/solicitar-codigo-correo")
    suspend fun solicitarCodigoCorreo(@Body request: RequestCodigoCorreo): Response<Unit>

    // 2. Verificar código de correo y obtener comprobanteCorreo
    @POST("api/v1/auth/verificar-codigo-correo")
    suspend fun verificarCodigoCorreo(@Body request: VerifyCodigoCorreoRequest): Response<VerifyCodigoCorreoResponse>

    // 3. Validar si el teléfono está disponible en la BD antes del SMS
    @POST("api/v1/auth/check-telefono")
    suspend fun checkTelefonoDisponible(@Body request: CheckTelefonoRequest): Response<Unit>

    // 4. Registro final enviando todos los datos
    @POST("api/v1/auth/registro")
    suspend fun registrarUsuario(@Body request: RegistroFinalRequest): Response<LoginResponse>

    // 5. Datos del usuario logueado (requiere token)
    @GET("api/v1/usuarios/me")
    suspend fun me(): Response<UsuarioMeDto>

    @PATCH("api/v1/usuarios/me")
    suspend fun updateMe(@Body request: UpdateMeRequest): Response<UsuarioMeDto>

    // Sube la foto de perfil (campo "foto")
    @Multipart
    @PUT("api/v1/usuarios/me/foto")
    suspend fun subirFoto(@Part foto: MultipartBody.Part): Response<UsuarioMeDto>

    // Borra la foto de perfil
    @DELETE("api/v1/usuarios/me/foto")
    suspend fun borrarFoto(): Response<UsuarioMeDto>
}