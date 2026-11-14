package com.equipo4.nearhome.data.remote
import com.equipo4.nearhome.di.NetworkModule

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Query

// ---------- Vendedor / comprador ----------

data class CrearReporteDto(
    val categoria: String,
    val motivo: String,
    val descripcion: String
)

data class ReporteCreadoDto(
    val id: Int,
    val estado: String
)

// ---------- Administrador ----------

data class PublicacionResumenDto(
    val id: Int,
    val titulo: String,
    val fotoId: Int?,
    val estado: String,
    val reportesPendientes: Int
)

/** La cuenta reportada (solo en reportes de usuario). */
data class UsuarioReportadoDto(
    val id: Int,
    val nombre: String,
    val tieneFoto: Boolean,
    val reportesPendientes: Int
)

/**
 * Un renglón de la lista. [imagenes] es la cantidad de evidencias, no los ids.
 * [tipo]: PUBLICACION o USUARIO. Según el tipo viene [publicacion] o [usuario]; el otro es null.
 */
data class ReporteListaDto(
    val id: Int,
    val tipo: String?,
    val categoria: String,
    val motivo: String,
    val descripcionCorta: String,
    val fecha: Long,
    val estado: String,
    val imagenes: Int,
    val publicacion: PublicacionResumenDto?,
    val usuario: UsuarioReportadoDto?
)

/** Detalle completo. [imagenes] son los ids de evidencia (ver [evidenciaReporteUrl]). */
data class ReporteDetalleDto(
    val id: Int,
    val tipo: String?,
    val categoria: String,
    val motivo: String,
    val descripcion: String,
    val fecha: Long,
    val estado: String,
    val imagenes: List<Int>,
    val publicacion: PublicacionResumenDto?,
    val usuario: UsuarioReportadoDto?,
    /** null = la cuenta que reportó ya no existe. */
    val reportadoPorId: Int?,
    val reportadoPorNombre: String?,
    val reportadoPorCorreo: String?,
    val nota: String?,
    val resueltoPor: String?,
    val fechaResolucion: Long?
)

data class ResolverReporteDto(
    val estado: String,
    val nota: String,
    val retirarPublicacion: Boolean
)

/** URL de una evidencia. Requiere token: la carga Coil con el encabezado Authorization. */
fun evidenciaReporteUrl(evidenciaId: Int): String =
    "${NetworkModule.BASE_URL}api/v1/admin/reportes/evidencias/$evidenciaId"

interface ReportesApi {

    /** Vendedor/comprador: "datos" (JSON) y de 0 a 5 imágenes en "fotos". */
    @Multipart
    @POST("api/v1/publicaciones/{id}/reportes")
    suspend fun crear(
        @Path("id") id: Int,
        @Part datos: MultipartBody.Part,
        @Part fotos: List<MultipartBody.Part>
    ): Response<ReporteCreadoDto>

    /** Reportar a un usuario (bandera de su perfil). Mismo formato que el de publicaciones. */
    @Multipart
    @POST("api/v1/usuarios/{id}/reportes")
    suspend fun crearDeUsuario(
        @Path("id") id: Int,
        @Part datos: MultipartBody.Part,
        @Part fotos: List<MultipartBody.Part>
    ): Response<ReporteCreadoDto>

    /** Pendientes: del más viejo al más nuevo. Procedentes y descartados: del más nuevo al más viejo. */
    @GET("api/v1/admin/reportes")
    suspend fun listarAdmin(
        @Query("estado") estado: String,
        @Query("pagina") pagina: Int
    ): List<ReporteListaDto>

    @GET("api/v1/admin/reportes/{id}")
    suspend fun detalleAdmin(@Path("id") id: Int): ReporteDetalleDto

    /** Devuelve el detalle ya actualizado. */
    @PATCH("api/v1/admin/reportes/{id}")
    suspend fun resolver(
        @Path("id") id: Int,
        @Body body: ResolverReporteDto
    ): ReporteDetalleDto
}