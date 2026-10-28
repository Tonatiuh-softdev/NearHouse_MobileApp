package com.equipo4.nearhome.domain.repository

import android.content.Context
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.remote.ErrorDto
import com.equipo4.nearhome.data.remote.PublicacionDto
import com.equipo4.nearhome.data.remote.PublicacionesApi
import com.equipo4.nearhome.data.remote.RechazarDto
import com.equipo4.nearhome.data.repository.PublicacionException
import com.equipo4.nearhome.data.repository.fotoPublicacionUrl
import com.equipo4.nearhome.di.NetworkModule
import com.equipo4.nearhome.domain.model.AdminListing
import com.equipo4.nearhome.domain.model.Advertiser
import com.equipo4.nearhome.domain.model.ListingStatus
import com.equipo4.nearhome.domain.model.ListingType
import com.equipo4.nearhome.domain.model.PropertyType
import com.equipo4.nearhome.domain.model.StatusChange
import com.equipo4.nearhome.util.AppStrings
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Anuncios del administrador con datos reales. Solo se puede aprobar o rechazar un anuncio en revisión;
 * el API valida que quien llama sea Administrador o Super Administrador.
 */
class AdminListingRepositoryApi(context: Context) : AdminListingRepository {

    private val session = SessionManager(context.applicationContext)
    private val gson = Gson()

    private val api: PublicacionesApi by lazy {
        NetworkModule.provideRetrofit(NetworkModule.provideOkHttpClient(session))
            .create(PublicacionesApi::class.java)
    }

    private val _listings = MutableStateFlow<List<AdminListing>>(emptyList())
    override val listings: StateFlow<List<AdminListing>> = _listings.asStateFlow()

    override fun getById(id: String): AdminListing? = _listings.value.firstOrNull { it.id == id }

    override suspend fun refresh(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            _listings.value = api.listarAdmin().map { it.toAdminListing() }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(PublicacionException(mensajeDe(e)))
        }
    }

    override suspend fun approve(id: String, actor: String): Result<Unit> =
        revisar { api.aprobar(id.toInt()) }

    override suspend fun reject(id: String, reason: String, actor: String): Result<Unit> =
        revisar { api.rechazar(id.toInt(), RechazarDto(reason.trim())) }

    /** Llama al API y, si sale bien, reemplaza el anuncio en la lista con el que regresó (ya con el estado nuevo). */
    private suspend fun revisar(llamada: suspend () -> PublicacionDto): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val actualizado = llamada().toAdminListing()
            _listings.update { lista -> lista.map { if (it.id == actualizado.id) actualizado else it } }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(PublicacionException(mensajeDe(e)))
        }
    }

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
        // El API ya explica el problema (por ejemplo "Este anuncio ya fue revisado")
        return cuerpo?.detalles?.takeIf { it.isNotEmpty() }?.joinToString("\n")
            ?: cuerpo?.error
            ?: AppStrings.get(R.string.create_listing_error_generic)
    }
}

/** El historial se arma con lo que guarda la base: cuándo se registró y, si ya se revisó, quién y por qué. */
private fun PublicacionDto.toAdminListing(): AdminListing {
    val estadoApi = estado.uppercase()
    val status = when (estadoApi) {
        "PUBLICADA" -> if (activa) ListingStatus.ACTIVO else ListingStatus.DESACTIVADO
        "RECHAZADA" -> ListingStatus.RECHAZADO
        else -> ListingStatus.PENDIENTE
    }
    val history = buildList {
        add(StatusChange(from = null, to = ListingStatus.PENDIENTE, timestamp = fechaRegistro, actor = vendedor.nombre))
        revision?.let { r ->
            val destino = when (estadoApi) {
                "PUBLICADA" -> ListingStatus.ACTIVO
                "RECHAZADA" -> ListingStatus.RECHAZADO
                else -> null
            }
            if (destino != null) {
                add(
                    StatusChange(
                        from = ListingStatus.PENDIENTE,
                        to = destino,
                        timestamp = r.fecha,
                        actor = r.revisor ?: AppStrings.get(R.string.listing_default_actor),
                        reason = r.motivo
                    )
                )
            }
        }
    }
    return AdminListing(
        id = id.toString(),
        title = titulo,
        type = runCatching { PropertyType.valueOf(tipo.uppercase()) }.getOrDefault(PropertyType.CASA),
        listingType = if (modalidad.equals("Renta", ignoreCase = true)) ListingType.RENTA else ListingType.VENTA,
        price = precio,
        location = listOf("$calle $numero".trim(), colonia, "Manzanillo, Colima").joinToString(", "),
        areaSqM = superficie ?: 0,
        bedrooms = dormitorios,
        bathrooms = banos,
        garages = cochera,
        description = descripcion,
        images = fotos.map { fotoPublicacionUrl(it) },
        advertiser = Advertiser(name = vendedor.nombre, email = vendedor.correo, verified = false),
        registeredAt = fechaRegistro,
        status = status,
        history = history
    )
}