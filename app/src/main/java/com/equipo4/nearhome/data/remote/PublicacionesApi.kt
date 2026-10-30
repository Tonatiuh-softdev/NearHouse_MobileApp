package com.equipo4.nearhome.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class VendedorDto(
    val id: Int,
    val nombre: String,
    val correo: String,
    val telefono: String
)

data class RevisionDto(
    val fecha: Long,
    val revisor: String?,
    val motivo: String?
)

/** Anuncio completo tal como lo regresa el API. Las fotos son ids: la imagen está en GET /api/v1/fotos/{id}. */
data class PublicacionDto(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val reglas: String?,
    val precio: Double,
    val modalidad: String,        // "Venta" o "Renta"
    val tipo: String,             // DEPARTAMENTO, CASA o TERRENO
    val estado: String,           // EN_REVISION, PUBLICADA o RECHAZADA
    val activa: Boolean,
    val calle: String,
    val numero: String,
    val colonia: String,
    val codigoPostal: String?,
    val latitud: Double,
    val longitud: Double,
    val superficie: Int?,         // solo TERRENO
    val usoSuelo: String?,        // HABITACIONAL o AGRICOLA
    val dormitorios: Int?,
    val banos: Int?,
    val cochera: Int?,
    val piso: Int?,
    val niveles: Int?,
    val amueblado: Boolean,
    val mascotas: Boolean,
    val elevador: Boolean,
    val patio: Boolean,
    val fotos: List<Int>,
    val vendedor: VendedorDto,
    val fechaRegistro: Long,
    val revision: RevisionDto?
)

data class RechazarDto(val motivo: String)

interface PublicacionesApi {
    /** Anuncios publicados y activos (el home y el mapa). */
    @GET("api/v1/publicaciones")
    suspend fun listar(): List<PublicacionDto>

    /** Los anuncios del usuario con sesión, en cualquier estado (Mis anuncios). */
    @GET("api/v1/publicaciones/mias")
    suspend fun misAnuncios(): List<PublicacionDto>

    @GET("api/v1/publicaciones/{id}")
    suspend fun obtener(@Path("id") id: Int): PublicacionDto

    // ---- Administrador ----

    @GET("api/v1/admin/publicaciones")
    suspend fun listarAdmin(@Query("estado") estado: String? = null): List<PublicacionDto>

    @POST("api/v1/admin/publicaciones/{id}/aprobar")
    suspend fun aprobar(@Path("id") id: Int): PublicacionDto

    @POST("api/v1/admin/publicaciones/{id}/rechazar")
    suspend fun rechazar(@Path("id") id: Int, @Body body: RechazarDto): PublicacionDto
}