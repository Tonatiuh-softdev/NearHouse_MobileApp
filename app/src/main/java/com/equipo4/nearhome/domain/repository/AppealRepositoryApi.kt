package com.equipo4.nearhome.domain.repository

import android.content.Context
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.remote.ApelacionDetalleDto
import com.equipo4.nearhome.data.remote.ApelacionListaDto
import com.equipo4.nearhome.data.remote.ApelacionesApi
import com.equipo4.nearhome.data.remote.ApelanteDto
import com.equipo4.nearhome.data.remote.ErrorDto
import com.equipo4.nearhome.data.remote.ResolverApelacionDto
import com.equipo4.nearhome.data.repository.PublicacionException
import com.equipo4.nearhome.data.repository.fotoUsuarioAdminUrl
import com.equipo4.nearhome.di.NetworkModule
import com.equipo4.nearhome.domain.model.Appeal
import com.equipo4.nearhome.domain.model.AppealStatus
import com.equipo4.nearhome.domain.model.AppealType
import com.equipo4.nearhome.util.AppStrings
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.coroutines.cancellation.CancellationException

/**
 * Apelaciones del administrador con datos reales (igual que los reportes).
 * Pendientes: de la más vieja a la más nueva. Resueltas: aceptadas y rechazadas, de la más nueva a la más vieja.
 */
class AppealRepositoryApi(context: Context) : AppealRepository {

    private val session = SessionManager(context.applicationContext)
    private val gson = Gson()

    private val api: ApelacionesApi by lazy {
        NetworkModule.provideRetrofit(NetworkModule.provideOkHttpClient(session))
            .create(ApelacionesApi::class.java)
    }

    private val _appeals = MutableStateFlow<List<Appeal>>(emptyList())

    override fun observeAppeals(): Flow<List<Appeal>> = _appeals.asStateFlow()

    override suspend fun refresh(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val pendientes = api.listarAdmin("PENDIENTE", 1)
            val resueltas = (api.listarAdmin("ACEPTADA", 1) + api.listarAdmin("RECHAZADA", 1))
                .sortedByDescending { it.fecha }
            _appeals.value = (pendientes + resueltas).map { it.toDomain() }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(PublicacionException(mensajeDe(e)))
        }
    }

    override suspend fun getAppealById(appealId: String): Appeal? = withContext(Dispatchers.IO) {
        val id = appealId.toIntOrNull() ?: return@withContext null
        try {
            api.detalleAdmin(id).toDomain()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun resolver(appealId: String, estado: String, nota: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                api.resolver(appealId.toInt(), ResolverApelacionDto(estado, nota))
                refresh()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(PublicacionException(mensajeDe(e)))
            }
        }

    // ---------- Errores ----------

    private fun mensajeDe(e: Exception): String = when (e) {
        is HttpException -> mensajeHttp(e)
        is IOException -> AppStrings.get(R.string.create_listing_error_network)
        else -> AppStrings.get(R.string.create_listing_error_generic)
    }

    private fun mensajeHttp(e: HttpException): String {
        if (e.code() == 401) return AppStrings.get(R.string.create_listing_error_session)
        val cuerpo = try {
            gson.fromJson(e.response()?.errorBody()?.string(), ErrorDto::class.java)
        } catch (x: Exception) {
            null
        }
        // El API ya explica el problema (por ejemplo "Esta apelación ya fue resuelta")
        return cuerpo?.detalles?.takeIf { it.isNotEmpty() }?.joinToString("\n")
            ?: cuerpo?.error
            ?: AppStrings.get(R.string.create_listing_error_generic)
    }

    // ---------- Mapeo ----------

    private fun ApelacionListaDto.toDomain(): Appeal = Appeal(
        id = id.toString(),
        type = tipoDominio(tipo),
        description = descripcionCorta,
        date = formatearFecha(fecha),
        status = estadoDominio(estado),
        evidences = emptyList(),
        evidenceCount = imagenes,
        appellantId = apelante.id,
        appellantName = apelante.nombre,
        appellantEmail = apelante.correo,
        appellantPhotoUrl = apelante.fotoUrl(),
        publicationId = publicacion?.id?.toString(),
        publicationTitle = publicacion?.titulo
    )

    private fun ApelacionDetalleDto.toDomain(): Appeal = Appeal(
        id = id.toString(),
        type = tipoDominio(tipo),
        description = descripcion,
        date = formatearFecha(fecha),
        status = estadoDominio(estado),
        evidences = imagenes,
        evidenceCount = imagenes.size,
        appellantId = apelante.id,
        appellantName = apelante.nombre,
        appellantEmail = apelante.correo,
        appellantPhotoUrl = apelante.fotoUrl(),
        publicationId = publicacion?.id?.toString(),
        publicationTitle = publicacion?.titulo,
        measureReason = motivoMedida,
        measureDate = formatearFecha(fechaMedida),
        measureById = medidaPorId,
        measureByName = medidaPorNombre,
        suspensionUntil = suspendidaHasta?.let { formatearDia(it) },
        suspensionActive = suspensionVigente,
        note = nota,
        resolvedBy = resueltoPor,
        resolvedDate = fechaResolucion?.let { AppStrings.get(R.string.attended_date_prefix, formatearFecha(it)) }
    )

    private fun tipoDominio(tipo: String): AppealType =
        if (tipo == "SUSPENSION") AppealType.SUSPENSION else AppealType.PUBLICACION

    private fun estadoDominio(estado: String): AppealStatus = when (estado) {
        "ACEPTADA" -> AppealStatus.ACEPTADA
        "RECHAZADA" -> AppealStatus.RECHAZADA
        else -> AppealStatus.PENDIENTE
    }

    /** La foto del panel pide token: la carga AdminImageLoader (UserAvatar). */
    private fun ApelanteDto.fotoUrl(): String? = if (tieneFoto) fotoUsuarioAdminUrl(id.toString()) else null

    private fun formatearFecha(millis: Long): String =
        SimpleDateFormat("dd MMM · HH:mm", AppStrings.locale()).format(Date(millis))

    private fun formatearDia(millis: Long): String =
        SimpleDateFormat("d MMM yyyy", AppStrings.locale()).format(Date(millis))
}
