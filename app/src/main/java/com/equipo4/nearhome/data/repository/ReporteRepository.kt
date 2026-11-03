package com.equipo4.nearhome.data.repository

import android.content.Context
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.remote.CrearReporteDto
import com.equipo4.nearhome.data.remote.ErrorDto
import com.equipo4.nearhome.data.remote.ReporteCreadoDto
import com.equipo4.nearhome.data.remote.ReportesApi
import com.equipo4.nearhome.di.NetworkModule
import com.equipo4.nearhome.util.AppStrings
import com.equipo4.nearhome.util.PhotoCompressor
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

/** Error con un mensaje listo para mostrar al usuario. */
class ReporteException(message: String) : Exception(message)

/**
 * Envía un reporte al API. No borra nada de lo que capturó el usuario:
 * si falla, la pantalla conserva los datos para que pueda reintentar.
 */
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

    /** [fotos]: rutas de archivos locales (máximo 5). La compresión las deja por debajo de 2 MB. */
    suspend fun reportar(
        publicacionId: Int,
        categoria: String,
        motivo: String,
        descripcion: String,
        fotos: List<String>
    ): Result<ReporteCreadoDto> = withContext(Dispatchers.IO) {
        try {
            val datos = MultipartBody.Part.createFormData(
                "datos", null,
                gson.toJson(CrearReporteDto(categoria, motivo, descripcion.trim()))
                    .toRequestBody("text/plain; charset=utf-8".toMediaType())
            )
            val imagenes = fotos.mapIndexed { i, ruta ->
                MultipartBody.Part.createFormData(
                    "fotos", "evidencia_${i + 1}.jpg",
                    PhotoCompressor.compress(ruta).toRequestBody("image/jpeg".toMediaType())
                )
            }

            val respuesta = api.crear(publicacionId, datos, imagenes)
            val cuerpo = respuesta.body()
            if (respuesta.isSuccessful && cuerpo != null) {
                Result.success(cuerpo)
            } else {
                Result.failure(ReporteException(mensajeDeError(respuesta.code(), respuesta.errorBody()?.string())))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            Result.failure(ReporteException(AppStrings.get(R.string.create_listing_error_network)))
        } catch (e: Exception) {
            Result.failure(ReporteException(AppStrings.get(R.string.create_listing_error_generic)))
        }
    }

    private fun mensajeDeError(codigo: Int, cuerpo: String?): String = when (codigo) {
        401 -> AppStrings.get(R.string.create_listing_error_session)
        403 -> AppStrings.get(R.string.report_error_own)
        404 -> AppStrings.get(R.string.report_error_not_available)
        409 -> AppStrings.get(R.string.report_error_duplicate)
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
}