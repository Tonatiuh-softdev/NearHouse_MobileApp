package com.equipo4.nearhome.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.Response

data class ListaUsuariosDto(
    val totalCuentas: Int,
    val totalResultados: Int,
    val pagina: Int,
    val porPagina: Int,
    val usuarios: List<UsuarioListaDto>
)

data class UsuarioListaDto(
    val id: Int,
    val nombre: String,
    val correo: String,
    val rol: String,
    val estatus: String,
    val tieneFoto: Boolean
)

data class UsuarioDetalleDto(
    val id: Int,
    val nombre: String,
    val primerApellido: String,
    val segundoApellido: String?,
    val correo: String,
    val telefono: String,
    val rol: String,
    val estatus: String,
    val fechaRegistro: String,
    val plan: PlanAdminDto?,
    val publicaciones: ConteoPublicacionesDto,
    val reportesPendientes: Int
)

data class PlanAdminDto(val nombre: String, val vencimiento: String?)

data class ConteoPublicacionesDto(val enRevision: Int, val publicadas: Int, val rechazadas: Int)

data class SuspenderUsuarioDto(val dias: Int?, val motivo: String)

interface AdminUsuariosApi {
    @GET("api/v1/admin/usuarios")
    suspend fun listar(
        @Query("buscar") buscar: String? = null,
        @Query("rol") rol: String? = null,
        @Query("estatus") estatus: String? = null,
        @Query("pagina") pagina: Int = 1
    ): ListaUsuariosDto

    @GET("api/v1/admin/usuarios/{id}")
    suspend fun detalle(@Path("id") id: Int): UsuarioDetalleDto

    @POST("api/v1/admin/usuarios/{id}/suspender")
    suspend fun suspender(@Path("id") id: Int, @Body body: SuspenderUsuarioDto): Response<Unit>

    @POST("api/v1/admin/usuarios/{id}/levantar-suspension")
    suspend fun levantarSuspension(@Path("id") id: Int): Response<Unit>

    @DELETE("api/v1/admin/usuarios/{id}")
    suspend fun eliminar(@Path("id") id: Int): Response<Unit>
}