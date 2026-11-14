package com.equipo4.nearhome.data.repository

import android.content.Context
import android.net.Uri
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.remote.CrearReporteDto
import com.equipo4.nearhome.data.remote.ErrorDto
import com.equipo4.nearhome.data.remote.ReporteCreadoDto
import com.equipo4.nearhome.data.remote.ReportesApi
import com.equipo4.nearhome.di.NetworkModule
import com.equipo4.nearhome.domain.model.ReportType
import com.equipo4.nearhome.util.AppStrings
import com.equipo4.nearhome.util.PhotoCompressor
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

/** Error con un mensaje listo para mostrar. [codigoHttp] permite a la pantalla decidir qué hacer (por ejemplo, 404). */
class ReporteException(message: String, val codigoHttp: Int? = null) : Exception(message)

class ReporteRepository(context: Context) {

    private val appContext = context.applicationContext
    private val gson = Gson()

    private val api: ReportesApi by lazy {
        val cliente = NetworkModule.provideOkHttpClient(SessionManager(appContext)).newBuilder()
            .writeTimeout(90, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
        NetworkModule.provideRetrofit(cliente).create(ReportesApi::class.java)
    }

    /**
     * Reporta una publicación o a un usuario, según [tipo]. [objetivoId] es el id de uno u otro.
     * [fotos]: imágenes elegidas en el selector (máximo 5). Se comprimen antes de subirlas.
     */
    suspend fun reportar(
        tipo: ReportType,
        objetivoId: Int,
        categoria: String,
        motivo: String,
        descripcion: String,
        fotos: List<Uri>
    ): Result<ReporteCreadoDto> = withContext(Dispatchers.IO) {
        val temporales = mutableListOf<File>()
        try {
            val datos = MultipartBody.Part.createFormData(
                "datos", null,
                gson.toJson(CrearReporteDto(categoria, motivo, descripcion.trim()))
                    .toRequestBody("text/plain; charset=utf-8".toMediaType())
            )

            // PhotoCompressor necesita una ruta de archivo (lee la orientación EXIF), por eso se copian primero
            val imagenes = fotos.mapIndexed { i, uri ->
                val archivo = copiarTemporal(uri, i).also { temporales += it }
                MultipartBody.Part.createFormData(
                    "fotos", "evidencia_${i + 1}.jpg",
                    PhotoCompressor.compress(archivo.absolutePath).toRequestBody("image/jpeg".toMediaType())
                )
            }

            val respuesta = when (tipo) {
                ReportType.PUBLICACION -> api.crear(objetivoId, datos, imagenes)
                ReportType.USUARIO -> api.crearDeUsuario(objetivoId, datos, imagenes)
            }
            val cuerpo = respuesta.body()
            if (respuesta.isSuccessful && cuerpo != null) {
                Result.success(cuerpo)
            } else {
                Result.failure(
                    ReporteException(
                        mensajeDeError(tipo, respuesta.code(), respuesta.errorBody()?.string()),
                        respuesta.code()
                    )
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            Result.failure(ReporteException(AppStrings.get(R.string.create_listing_error_network)))
        } catch (e: Exception) {
            Result.failure(ReporteException(AppStrings.get(R.string.create_listing_error_generic)))
        } finally {
            temporales.forEach { it.delete() }
        }
    }

    private fun copiarTemporal(uri: Uri, indice: Int): File {
        val destino = File(appContext.cacheDir, "evidencia_${System.currentTimeMillis()}_$indice.jpg")
        val entrada = appContext.contentResolver.openInputStream(uri)
            ?: error("No se pudo leer la imagen")
        entrada.use { input -> destino.outputStream().use { input.copyTo(it) } }
        return destino
    }

    /** Solo para leer el "codigo" del error (por ejemplo NO_REPORTABLE). */
    private data class CodigoError(val codigo: String? = null)

    private fun mensajeDeError(tipo: ReportType, codigo: Int, cuerpo: String?): String = when (codigo) {
        401 -> AppStrings.get(R.string.create_listing_error_session)
        403 -> when {
            tipo == ReportType.PUBLICACION -> AppStrings.get(R.string.report_error_own)
            codigoDe(cuerpo) == "NO_REPORTABLE" -> AppStrings.get(R.string.report_user_error_not_reportable)
            else -> AppStrings.get(R.string.report_user_error_own)
        }
        404 -> AppStrings.get(
            if (tipo == ReportType.USUARIO) R.string.report_user_error_not_available else R.string.report_error_not_available
        )
        409 -> AppStrings.get(
            if (tipo == ReportType.USUARIO) R.string.report_user_error_duplicate else R.string.report_error_duplicate
        )
        429 -> AppStrings.get(R.string.report_error_limit)
        400 -> {
            val error = try {
                gson.fromJson(cuerpo, ErrorDto::class.java)
            } catch (e: Exception) {
                null
            }
            error?.detalles?.takeIf { it.isNotEmpty() }?.joinToString("\n")
                ?: error?.error
                ?: AppStrings.get(R.string.create_listing_error_generic)
        }
        else -> AppStrings.get(R.string.create_listing_error_generic)
    }

    private fun codigoDe(cuerpo: String?): String? = try {
        gson.fromJson(cuerpo, CodigoError::class.java)?.codigo
    } catch (e: Exception) {
        null
    }
}