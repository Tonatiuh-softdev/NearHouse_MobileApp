package com.equipo4.nearhome.data.remote

import com.equipo4.nearhome.di.NetworkModule
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

// ---------- Quien apela ----------

/** [token] solo al apelar una suspensión: lo da el inicio de sesión (la cuenta no tiene sesión). */
data class CrearApelacionDto(
    val descripcion: String,
    val token: String? = null
)

data class ApelacionCreadaDto(
    val id: Int,
    val estado: String
)

// ---------- Administrador ----------

data class ApelanteDto(
    val id: Int,
    val nombre: String,
    val correo: String,
    val tieneFoto: Boolean
)

data class ApelacionPublicacionDto(
    val id: Int,
    val titulo: String,
    val fotoId: Int?,
    val estado: String
)

/** [tipo]: PUBLICACION o SUSPENSION. [publicacion] solo viene en PUBLICACION. */
data class ApelacionListaDto(
    val id: Int,
    val tipo: String,
    val descripcionCorta: String,
    val fecha: Long,
    val estado: String,
    val imagenes: Int,
    val apelante: ApelanteDto,
    val publicacion: ApelacionPublicacionDto?
)

data class ApelacionDetalleDto(
    val id: Int,
    val tipo: String,
    val descripcion: String,
    val fecha: Long,
    val estado: String,
    val imagenes: List<Int>,
    val apelante: ApelanteDto,
    val publicacion: ApelacionPublicacionDto?,
    val motivoMedida: String,
    val fechaMedida: Long,
    val medidaPorId: Int?,
    val medidaPorNombre: String?,
    val suspendidaHasta: Long?,
    val suspensionVigente: Boolean,
    val nota: String?,
    val resueltoPor: String?,
    val fechaResolucion: Long?
)

/** [estado]: ACEPTADA (se revierte la decisión) o RECHAZADA (se mantiene). */
data class ResolverApelacionDto(
    val estado: String,
    val nota: String
)

/** Imagen de una apelación. Necesita el token de la sesión (va en el encabezado). */
fun evidenciaApelacionUrl(evidenciaId: Int): String =
    "${NetworkModule.BASE_URL}api/v1/admin/apelaciones/evidencias/$evidenciaId"

interface ApelacionesApi {

    /** POST /api/v1/publicaciones/{id}/apelaciones (requiere sesión). Campo "datos" + de 0 a 5 archivos en "fotos". */
    @Multipart
    @POST("api/v1/publicaciones/{id}/apelaciones")
    suspend fun apelarPublicacion(
        @Path("id") id: Int,
        @Part datos: MultipartBody.Part,
        @Part fotos: List<MultipartBody.Part>
    ): Response<ApelacionCreadaDto>

    /** POST /api/v1/apelaciones/suspension (sin sesión). El comprobante va dentro de "datos". */
    @Multipart
    @POST("api/v1/apelaciones/suspension")
    suspend fun apelarSuspension(
        @Part datos: MultipartBody.Part,
        @Part fotos: List<MultipartBody.Part>
    ): Response<ApelacionCreadaDto>

    /** Pendientes: de la más vieja a la más nueva. Resueltas: de la más nueva a la más vieja. */
    @GET("api/v1/admin/apelaciones")
    suspend fun listarAdmin(
        @Query("estado") estado: String,
        @Query("pagina") pagina: Int
    ): List<ApelacionListaDto>

    @GET("api/v1/admin/apelaciones/{id}")
    suspend fun detalleAdmin(@Path("id") id: Int): ApelacionDetalleDto

    @PATCH("api/v1/admin/apelaciones/{id}")
    suspend fun resolver(
        @Path("id") id: Int,
        @Body body: ResolverApelacionDto
    ): ApelacionDetalleDto
}
