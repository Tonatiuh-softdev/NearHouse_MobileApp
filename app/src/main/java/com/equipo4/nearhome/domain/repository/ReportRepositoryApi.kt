package com.equipo4.nearhome.domain.repository

import android.content.Context
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.remote.ErrorDto
import com.equipo4.nearhome.data.remote.ReporteDetalleDto
import com.equipo4.nearhome.data.remote.ReporteListaDto
import com.equipo4.nearhome.data.remote.ReportesApi
import com.equipo4.nearhome.data.remote.ResolverReporteDto
import com.equipo4.nearhome.data.repository.PublicacionException
import com.equipo4.nearhome.di.NetworkModule
import com.equipo4.nearhome.domain.model.Report
import com.equipo4.nearhome.domain.model.ReportStatus
import com.equipo4.nearhome.domain.model.ReportType
import com.equipo4.nearhome.ui.publicacion.report.ReportReason
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
 * Reportes del administrador con datos reales.
 * Pendientes: API en orden ascendente. Atendidos: procedentes y descartados, del más nuevo al más viejo.
 */
class ReportRepositoryApi(context: Context) : ReportRepository {

    private val session = SessionManager(context.applicationContext)
    private val gson = Gson()

    private val api: ReportesApi by lazy {
        NetworkModule.provideRetrofit(NetworkModule.provideOkHttpClient(session))
            .create(ReportesApi::class.java)
    }

    private val _reports = MutableStateFlow<List<Report>>(emptyList())

    override fun observeReports(): Flow<List<Report>> = _reports.asStateFlow()

    override suspend fun refresh(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val pendientes = api.listarAdmin("PENDIENTE", 1)
            val atendidos = (api.listarAdmin("PROCEDENTE", 1) + api.listarAdmin("DESCARTADO", 1))
                .sortedByDescending { it.fecha }
            _reports.value = (pendientes + atendidos).map { it.toDomain() }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(PublicacionException(mensajeDe(e)))
        }
    }

    override suspend fun getReportById(reportId: String): Report? = withContext(Dispatchers.IO) {
        val id = reportId.toIntOrNull() ?: return@withContext null
        try {
            api.detalleAdmin(id).toDomain()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun resolver(
        reportId: String,
        estado: String,
        nota: String,
        retirarPublicacion: Boolean
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            api.resolver(reportId.toInt(), ResolverReporteDto(estado, nota, retirarPublicacion))
            // Un retiro también cierra otros reportes de la misma publicación: se vuelve a pedir la lista
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
        // El API ya explica el problema (por ejemplo "Este reporte ya fue resuelto")
        return cuerpo?.detalles?.takeIf { it.isNotEmpty() }?.joinToString("\n")
            ?: cuerpo?.error
            ?: AppStrings.get(R.string.create_listing_error_generic)
    }

    // ---------- Mapeo ----------

    private fun ReporteListaDto.toDomain(): Report = Report(
        id = id.toString(),
        type = ReportType.PUBLICACION,
        motive = motivoTexto(categoria, motivo),
        description = descripcionCorta,
        date = formatearFecha(fecha),
        status = estadoDominio(estado),
        result = resultadoTexto(estado, publicacion.estado),
        // La lista no trae la fecha de resolución: se muestra la de registro hasta abrir el detalle
        attendedDate = null,
        note = null,
        evidences = emptyList(),
        evidenceCount = imagenes,
        targetId = publicacion.id.toString(),
        reportedByName = null,
        reportedByEmail = null
    )

    private fun ReporteDetalleDto.toDomain(): Report = Report(
        id = id.toString(),
        type = ReportType.PUBLICACION,
        motive = motivoTexto(categoria, motivo),
        description = descripcion,
        date = formatearFecha(fecha),
        status = estadoDominio(estado),
        result = resultadoTexto(estado, publicacion.estado),
        attendedDate = fechaResolucion?.let { AppStrings.get(R.string.attended_date_prefix, formatearFecha(it)) },
        note = nota,
        evidences = imagenes,
        evidenceCount = imagenes.size,
        targetId = publicacion.id.toString(),
        reportedByName = reportadoPorNombre,
        reportedByEmail = reportadoPorCorreo
    )

    private fun estadoDominio(estado: String): ReportStatus =
        if (estado == "PENDIENTE") ReportStatus.PENDIENTE else ReportStatus.ATENDIDO

    /** PROCEDENTE con la publicación rechazada = se retiró. Descartado = cerrado sin medida. */
    private fun resultadoTexto(estado: String, estadoPublicacion: String): String? = when (estado) {
        "PROCEDENTE" -> if (estadoPublicacion == "RECHAZADA") {
            AppStrings.get(R.string.report_measure_listing_removed)
        } else {
            AppStrings.get(R.string.report_measure_applied)
        }
        "DESCARTADO" -> AppStrings.get(R.string.closed_without_measure)
        else -> null
    }

    /** El API manda el código (categoría + motivo); aquí se busca el texto del idioma actual. */
    private fun motivoTexto(categoria: String, motivo: String): String =
        ReportReason.entries
            .firstOrNull { it.category.name == categoria && it.codigo == motivo }
            ?.let { AppStrings.get(it.titleRes) }
            ?: motivo

    private fun formatearFecha(millis: Long): String =
        SimpleDateFormat("dd MMM · HH:mm", AppStrings.locale()).format(Date(millis))
}