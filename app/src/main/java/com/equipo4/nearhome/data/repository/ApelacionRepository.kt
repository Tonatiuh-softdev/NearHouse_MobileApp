package com.equipo4.nearhome.data.repository

import android.content.Context
import android.net.Uri
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.remote.ApelacionCreadaDto
import com.equipo4.nearhome.data.remote.ApelacionesApi
import com.equipo4.nearhome.data.remote.CrearApelacionDto
import com.equipo4.nearhome.data.remote.ErrorDto
import com.equipo4.nearhome.di.NetworkModule
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

/** Error con un mensaje listo para mostrar. [codigoHttp] permite a la pantalla decidir qué hacer. */
class ApelacionException(message: String, val codigoHttp: Int? = null) : Exception(message)

/** Manda la apelación de quien no está de acuerdo con una decisión (anuncio retirado o cuenta suspendida). */
class ApelacionRepository(context: Context) {

    private val appContext = context.applicationContext
    private val gson = Gson()

    private val api: ApelacionesApi by lazy {
        val cliente = NetworkModule.provideOkHttpClient(SessionManager(appContext)).newBuilder()
            .writeTimeout(90, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
        NetworkModule.provideRetrofit(cliente).create(ApelacionesApi::class.java)
    }

    /** Apelación del retiro de un anuncio propio. */
    suspend fun apelarPublicacion(publicacionId: Int, descripcion: String, fotos: List<Uri>): Result<ApelacionCreadaDto> =
        enviar(CrearApelacionDto(descripcion.trim()), fotos, esSuspension = false) { datos, imagenes ->
            api.apelarPublicacion(publicacionId, datos, imagenes)
        }

    /** Apelación de una suspensión, con el comprobante que dio el inicio de sesión. */
    suspend fun apelarSuspension(token: String, descripcion: String, fotos: List<Uri>): Result<ApelacionCreadaDto> =
        enviar(CrearApelacionDto(descripcion.trim(), token), fotos, esSuspension = true) { datos, imagenes ->
            api.apelarSuspension(datos, imagenes)
        }

    private suspend fun enviar(
        dto: CrearApelacionDto,
        fotos: List<Uri>,
        esSuspension: Boolean,
        llamada: suspend (MultipartBody.Part, List<MultipartBody.Part>) -> retrofit2.Response<ApelacionCreadaDto>
    ): Result<ApelacionCreadaDto> = withContext(Dispatchers.IO) {
        val temporales = mutableListOf<File>()
        try {
            val datos = MultipartBody.Part.createFormData(
                "datos", null,
                gson.toJson(dto).toRequestBody("text/plain; charset=utf-8".toMediaType())
            )
            // PhotoCompressor necesita una ruta de archivo (lee la orientación EXIF), por eso se copian primero
            val imagenes = fotos.mapIndexed { i, uri ->
                val archivo = copiarTemporal(uri, i).also { temporales += it }
                MultipartBody.Part.createFormData(
                    "fotos", "evidencia_${i + 1}.jpg",
                    PhotoCompressor.compress(archivo.absolutePath).toRequestBody("image/jpeg".toMediaType())
                )
            }

            val respuesta = llamada(datos, imagenes)
            val cuerpo = respuesta.body()
            if (respuesta.isSuccessful && cuerpo != null) {
                Result.success(cuerpo)
            } else {
                Result.failure(
                    ApelacionException(
                        mensajeDeError(respuesta.code(), respuesta.errorBody()?.string(), esSuspension),
                        respuesta.code()
                    )
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            Result.failure(ApelacionException(AppStrings.get(R.string.create_listing_error_network)))
        } catch (e: Exception) {
            Result.failure(ApelacionException(AppStrings.get(R.string.create_listing_error_generic)))
        } finally {
            temporales.forEach { it.delete() }
        }
    }

    private fun copiarTemporal(uri: Uri, indice: Int): File {
        val destino = File(appContext.cacheDir, "apelacion_${System.currentTimeMillis()}_$indice.jpg")
        val entrada = appContext.contentResolver.openInputStream(uri)
            ?: error("No se pudo leer la imagen")
        entrada.use { input -> destino.outputStream().use { input.copyTo(it) } }
        return destino
    }

    private fun mensajeDeError(codigo: Int, cuerpo: String?, esSuspension: Boolean): String = when (codigo) {
        // Sin sesión: en una suspensión es que el comprobante del inicio de sesión ya venció (dura 30 minutos)
        401 -> AppStrings.get(if (esSuspension) R.string.appeal_error_expired else R.string.create_listing_error_session)
        404 -> AppStrings.get(R.string.appeal_error_not_available)
        409 -> AppStrings.get(R.string.appeal_error_duplicate)
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
