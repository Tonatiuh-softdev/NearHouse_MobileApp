package com.equipo4.nearhome.data.remote

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

data class CrearReporteDto(
    val categoria: String,
    val motivo: String,
    val descripcion: String
)

data class ReporteCreadoDto(
    val id: Int,
    val estado: String
)

interface ReportesApi {
    /** Igual que PublicacionApi: "datos" (JSON) y de 0 a 5 imágenes en "fotos". */
    @Multipart
    @POST("api/v1/publicaciones/{id}/reportes")
    suspend fun crear(
        @Path("id") id: Int,
        @Part datos: MultipartBody.Part,
        @Part fotos: List<MultipartBody.Part>
    ): Response<ReporteCreadoDto>
}