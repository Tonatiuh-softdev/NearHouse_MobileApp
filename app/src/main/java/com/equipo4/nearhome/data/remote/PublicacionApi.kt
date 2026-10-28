package com.equipo4.nearhome.data.remote

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/** Datos del anuncio que se mandan en el campo "datos" (JSON). Los null no se envían. */
data class CrearPublicacionDto(
    val titulo: String,
    val descripcion: String,
    val reglas: String?,
    val precio: Long,
    val modalidad: String,        // "Venta" o "Renta"
    val tipo: String,             // DEPARTAMENTO, CASA o TERRENO
    val calle: String,
    val numero: String,
    val colonia: String,
    val codigoPostal: String?,
    val latitud: Double,
    val longitud: Double,
    val superficie: Int?,         // solo TERRENO
    val dormitorios: Int?,
    val banos: Int?,
    val cochera: Int?,
    val piso: Int?,               // solo DEPARTAMENTO
    val niveles: Int?,            // solo CASA
    val amueblado: Boolean,
    val mascotas: Boolean,
    val elevador: Boolean,
    val patio: Boolean,
    val usoSuelo: String?         // HABITACIONAL o AGRICOLA
)

data class PublicacionCreadaDto(
    val id: Int,
    val titulo: String,
    val estado: String,
    val fotos: List<Int> = emptyList()
)

/** Formato de error del API: {"error": "...", "detalles": ["..."]} */
data class ErrorDto(
    val error: String? = null,
    val detalles: List<String>? = null
)

interface PublicacionApi {
    /** POST /api/v1/publicaciones (requiere sesión). Campo "datos" + de 1 a 5 archivos en "fotos". */
    @Multipart
    @POST("api/v1/publicaciones")
    suspend fun crear(
        @Part datos: MultipartBody.Part,
        @Part fotos: List<MultipartBody.Part>
    ): Response<PublicacionCreadaDto>
}