package com.equipo4.nearhome.data.repository

import android.content.Context
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.remote.CrearPublicacionDto
import com.equipo4.nearhome.data.remote.ErrorDto
import com.equipo4.nearhome.data.remote.PublicacionApi
import com.equipo4.nearhome.data.remote.PublicacionCreadaDto
import com.equipo4.nearhome.di.NetworkModule
import com.equipo4.nearhome.domain.model.PropertyType
import com.equipo4.nearhome.ui.createlisting.CreateListingState
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

/** Error con un mensaje listo para mostrarle al usuario. */
class PublicacionException(message: String) : Exception(message)

/**
 * Envía el anuncio al API (datos + fotos). Usa el mismo cliente con sesión que el resto de la app,
 * pero con tiempos de espera más largos porque se suben hasta 5 fotos.
 */
class PublicacionRepository(context: Context) {

    private val appContext = context.applicationContext
    private val gson = Gson()

    private val api: PublicacionApi by lazy {
        val cliente = NetworkModule.provideOkHttpClient(SessionManager(appContext)).newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
        NetworkModule.provideRetrofit(cliente).create(PublicacionApi::class.java)
    }

    suspend fun crear(state: CreateListingState): Result<PublicacionCreadaDto> = withContext(Dispatchers.IO) {
        try {
            val datos = MultipartBody.Part.createFormData(
                "datos", null,
                gson.toJson(state.toDto()).toRequestBody("text/plain; charset=utf-8".toMediaType())
            )
            // La primera foto es la portada: el orden de la lista se respeta
            val fotos = state.photos.mapIndexed { i, ruta ->
                MultipartBody.Part.createFormData(
                    "fotos", "foto_${i + 1}.jpg",
                    PhotoCompressor.compress(ruta).toRequestBody("image/jpeg".toMediaType())
                )
            }

            val respuesta = api.crear(datos, fotos)
            val cuerpo = respuesta.body()
            if (respuesta.isSuccessful && cuerpo != null) {
                Result.success(cuerpo)
            } else {
                Result.failure(PublicacionException(mensajeDeError(respuesta.code(), respuesta.errorBody()?.string())))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            Result.failure(PublicacionException(AppStrings.get(R.string.create_listing_error_network)))
        } catch (e: Exception) {
            Result.failure(PublicacionException(AppStrings.get(R.string.create_listing_error_generic)))
        }
    }

    private fun mensajeDeError(codigo: Int, cuerpo: String?): String {
        if (codigo == 401) return AppStrings.get(R.string.create_listing_error_session)
        if (codigo == 403) return AppStrings.get(R.string.create_listing_error_forbidden)
        val error = try {
            gson.fromJson(cuerpo, ErrorDto::class.java)
        } catch (e: Exception) {
            null
        }
        return error?.detalles?.takeIf { it.isNotEmpty() }?.joinToString("\n")
            ?: error?.error
            ?: AppStrings.get(R.string.create_listing_error_generic)
    }

    private fun CreateListingState.toDto(): CrearPublicacionDto {
        val tipo = requireNotNull(propertyType)
        return CrearPublicacionDto(
            titulo = title.trim(),
            descripcion = description.trim(),
            reglas = rules.trim().ifBlank { null },
            precio = price.toLong(),
            modalidad = if (isRent) "Renta" else "Venta",
            tipo = tipo.name,
            calle = street.trim(),
            numero = streetNumber.trim(),
            colonia = requireNotNull(locationColonia),
            codigoPostal = postalCode.ifBlank { null },
            latitud = requireNotNull(latitude),
            longitud = requireNotNull(longitude),
            // La superficie solo se pide en terreno; en casa y departamento va dentro de la descripción
            superficie = if (tipo == PropertyType.TERRENO) area.toIntOrNull() else null,
            dormitorios = bedrooms,
            banos = bathrooms,
            cochera = parking,
            piso = floor,
            niveles = levels,
            amueblado = furnished,
            mascotas = petsAllowed,
            elevador = elevator,
            patio = garden,
            usoSuelo = landUse?.name
        )
    }
}