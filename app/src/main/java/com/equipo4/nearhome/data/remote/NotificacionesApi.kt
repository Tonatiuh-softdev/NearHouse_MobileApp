package com.equipo4.nearhome.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// ---------- DTOs ----------

data class NotificacionDto(
    val id: Int,
    val tipo: String,
    val datos: Map<String, String?>? = null,
    val leida: Boolean,
    val fecha: String,               // ISO
    val idPublicacion: Int? = null,
    val idFotoPublicacion: Int? = null,
    val idReporte: Int? = null
)

data class NotificacionesPaginaDto(
    val pagina: Int,
    val porPagina: Int,
    val total: Int,
    val totalNoLeidas: Int,
    val notificaciones: List<NotificacionDto>
)

data class NoLeidasDto(val totalNoLeidas: Int)

data class DispositivoRequestDto(
    val token: String,
    val idioma: String               // "es" | "en"
)

data class BajaDispositivoRequestDto(val token: String)

// ---------- API ----------

interface NotificacionesApi {

    @GET("api/v1/notificaciones")
    suspend fun listar(@Query("pagina") pagina: Int = 1): NotificacionesPaginaDto

    @GET("api/v1/notificaciones/no-leidas")
    suspend fun noLeidas(): NoLeidasDto

    @PATCH("api/v1/notificaciones/{id}/leida")
    suspend fun marcarLeida(@Path("id") id: Int): Response<Unit>

    @PATCH("api/v1/notificaciones/leidas")
    suspend fun marcarTodasLeidas(): Response<Unit>

    @DELETE("api/v1/notificaciones/{id}")
    suspend fun borrar(@Path("id") id: Int): Response<Unit>

    @DELETE("api/v1/notificaciones")
    suspend fun borrarTodas(): Response<Unit>

    @POST("api/v1/dispositivos")
    suspend fun registrarDispositivo(@Body body: DispositivoRequestDto): Response<Unit>

    @POST("api/v1/dispositivos/baja")
    suspend fun bajaDispositivo(@Body body: BajaDispositivoRequestDto): Response<Unit>
}