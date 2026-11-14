package com.equipo4.nearhome.data.repository

import android.content.Context
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.remote.BajaDispositivoRequestDto
import com.equipo4.nearhome.data.remote.DispositivoRequestDto
import com.equipo4.nearhome.data.remote.ErrorDto
import com.equipo4.nearhome.data.remote.NotificacionDto
import com.equipo4.nearhome.data.remote.NotificacionesApi
import com.equipo4.nearhome.di.NetworkModule
import com.equipo4.nearhome.domain.model.Notificacion
import com.equipo4.nearhome.domain.model.TipoNotificacion
import com.equipo4.nearhome.util.AppStrings
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.cancellation.CancellationException

class NotificacionesException(mensaje: String) : Exception(mensaje)

class NotificacionesRepositoryApi(context: Context) : NotificacionesRepository {

    private val session = SessionManager(context.applicationContext)
    private val gson = Gson()

    private val api: NotificacionesApi by lazy {
        NetworkModule.provideRetrofit(NetworkModule.provideOkHttpClient(session))
            .create(NotificacionesApi::class.java)
    }

    override suspend fun listar(pagina: Int): Result<List<Notificacion>> =
        withContext(Dispatchers.IO) {
            try {
                val resp = api.listar(pagina)
                Result.success(resp.notificaciones.map { it.toDominio() })
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(NotificacionesException(mensajeDe(e)))
            }
        }

    override suspend fun totalNoLeidas(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.noLeidas().total)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(NotificacionesException(mensajeDe(e)))
        }
    }

    override suspend fun marcarLeida(id: String): Result<Unit> =
        accion(id) { api.marcarLeida(it) }

    override suspend fun marcarTodasLeidas(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val r = api.marcarTodasLeidas()
            if (r.isSuccessful) Result.success(Unit)
            else Result.failure(
                NotificacionesException(mensajeDeRespuesta(r.code(), r.errorBody()?.string()))
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(NotificacionesException(mensajeDe(e)))
        }
    }

    override suspend fun borrar(id: String): Result<Unit> =
        accion(id) { api.borrar(it) }

    override suspend fun borrarTodas(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val r = api.borrarTodas()
            if (r.isSuccessful) Result.success(Unit)
            else Result.failure(
                NotificacionesException(mensajeDeRespuesta(r.code(), r.errorBody()?.string()))
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(NotificacionesException(mensajeDe(e)))
        }
    }

    override suspend fun registrarDispositivo(token: String, idioma: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val r = api.registrarDispositivo(DispositivoRequestDto(token, idioma))
                if (r.isSuccessful) Result.success(Unit)
                else Result.failure(
                    NotificacionesException(mensajeDeRespuesta(r.code(), r.errorBody()?.string()))
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(NotificacionesException(mensajeDe(e)))
            }
        }

    override suspend fun bajaDispositivo(token: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val r = api.bajaDispositivo(BajaDispositivoRequestDto(token))
                if (r.isSuccessful) Result.success(Unit)
                else Result.failure(
                    NotificacionesException(mensajeDeRespuesta(r.code(), r.errorBody()?.string()))
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(NotificacionesException(mensajeDe(e)))
            }
        }

    // ---------- Helpers ----------

    private suspend fun accion(id: String, llamada: suspend (Int) -> Response<Unit>): Result<Unit> =
        withContext(Dispatchers.IO) {
            val intId = id.toIntOrNull()
                ?: return@withContext Result.failure(NotificacionesException("ID inválido"))
            try {
                val r = llamada(intId)
                if (r.isSuccessful) Result.success(Unit)
                else Result.failure(
                    NotificacionesException(mensajeDeRespuesta(r.code(), r.errorBody()?.string()))
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(NotificacionesException(mensajeDe(e)))
            }
        }

    private fun mensajeDeRespuesta(codigo: Int, cuerpo: String?): String {
        when (codigo) {
            401 -> return AppStrings.get(R.string.create_listing_error_session)
            404 -> return AppStrings.get(R.string.notifications_error)
        }
        val detalle = runCatching { gson.fromJson(cuerpo, ErrorDto::class.java) }.getOrNull()
        detalle?.detalles?.takeIf { it.isNotEmpty() }?.let { return it.joinToString("\n") }
        detalle?.error?.let { return it }
        return AppStrings.get(R.string.notifications_error)
    }

    private fun mensajeDe(e: Exception): String = when {
        e is HttpException && e.code() == 401 -> AppStrings.get(R.string.create_listing_error_session)
        e is IOException -> AppStrings.get(R.string.network_error_connection)
        else -> AppStrings.get(R.string.notifications_error)
    }
}

// ---------- Mapeo DTO → dominio ----------

private fun NotificacionDto.toDominio(): Notificacion {
    val tipoDominio = TipoNotificacion.fromApi(tipo) ?: TipoNotificacion.PUBLICACION_EN_REVISION
    val imagen = idFotoPublicacion?.let { fotoPublicacionUrl(it) } // se reutiliza la de PropiedadRepositoryApi
    return Notificacion(
        id = id.toString(),
        tipo = tipoDominio,
        timestampMs = fecha,
        leida = leida,
        imagenUrl = imagen,
        publicacionId = idPublicacion?.toString(),
        reporteId = idReporte?.toString(),
        tituloPropiedad = datos?.get("titulo"),
        motivo = datos?.get("motivo"),
        nombreUsuario = datos?.get("nombre"),
        plan = datos?.get("plan"),
        fechaPlanIso = datos?.get("fecha"),
        usuarioReportadoId = datos?.get("usuarioReportado")
    )
}

/** Formatea una fecha ISO al idioma actual de la app, ej. "6 nov 2026". */
fun formatearFechaPlan(iso: String?): String =
    if (iso.isNullOrBlank()) "" else runCatching {
        OffsetDateTime.parse(iso)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy", AppStrings.locale()))
    }.getOrDefault(iso)