package com.equipo4.nearhome.domain.repository

import android.content.Context
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.remote.ErrorDto
import com.equipo4.nearhome.data.remote.MotivoDto
import com.equipo4.nearhome.data.remote.PublicacionDto
import com.equipo4.nearhome.data.remote.PublicacionesApi
import com.equipo4.nearhome.data.remote.RechazarDto
import com.equipo4.nearhome.data.repository.PublicacionException
import com.equipo4.nearhome.data.repository.Repositories
import com.equipo4.nearhome.data.repository.fotoPublicacionUrl
import com.equipo4.nearhome.data.repository.fotoUsuarioUrl
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
import com.equipo4.nearhome.domain.model.DeactivationInfo

/**
 * Anuncios del administrador con datos reales. Aprobar, rechazar, desactivar, reactivar y eliminar
 * pasan por el API, que valida que quien llama sea Administrador o Super Administrador.
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

    /**
     * Desactiva el anuncio con motivo. El API responde 204 sin cuerpo,
     * así que se vuelve a pedir la lista para tener el estado y el historial actualizados.
     */
    override suspend fun desactivar(id: String, motivo: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val respuesta = api.desactivarAdmin(id.toInt(), MotivoDto(motivo.trim()))
            if (respuesta.isSuccessful || respuesta.code() == 204) {
                refrescarLista()
            } else {
                Result.failure(PublicacionException(mensajeHttp(respuesta.code())))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(PublicacionException(mensajeDe(e)))
        }
    }

    /** Reactiva el anuncio desactivado por el administrador. */
    override suspend fun reactivar(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val respuesta = api.reactivarAdmin(id.toInt())
            if (respuesta.isSuccessful || respuesta.code() == 204) {
                refrescarLista()
            } else {
                Result.failure(PublicacionException(mensajeHttp(respuesta.code())))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(PublicacionException(mensajeDe(e)))
        }
    }

    /** Elimina el anuncio de forma permanente y lo quita de la lista. */
    override suspend fun eliminar(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val respuesta = api.eliminarAdmin(id.toInt())
            if (respuesta.isSuccessful || respuesta.code() == 204) {
                _listings.update { lista -> lista.filterNot { it.id == id } }
                Repositories.propiedades.refresh()
                Result.success(Unit)
            } else {
                Result.failure(PublicacionException(mensajeHttp(respuesta.code())))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(PublicacionException(mensajeDe(e)))
        }
    }

    /**
     * Llama al API y, si sale bien, reemplaza el anuncio en la lista con el que regresó (ya con el estado nuevo)
     * y avisa al Home que vuelva a pedir los anuncios publicados.
     */
    private suspend fun revisar(llamada: suspend () -> PublicacionDto): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val actualizado = llamada().toAdminListing()
            _listings.update { lista -> lista.map { if (it.id == actualizado.id) actualizado else it } }
            Repositories.propiedades.refresh()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(PublicacionException(mensajeDe(e)))
        }
    }

    /** Vuelve a pedir la lista completa (después de desactivar o reactivar) y avisa al Home. */
    private suspend fun refrescarLista(): Result<Unit> {
        _listings.value = api.listarAdmin().map { it.toAdminListing() }
        Repositories.propiedades.refresh()
        return Result.success(Unit)
    }

    private fun mensajeDe(e: Exception): String = when (e) {
        is HttpException -> mensajeHttpCuerpo(e)
        is IOException -> AppStrings.get(R.string.create_listing_error_network)
        else -> AppStrings.get(R.string.create_listing_error_generic)
    }

    /** Mensaje según el código HTTP, cuando no hay cuerpo de error que mostrar. */
    private fun mensajeHttp(codigo: Int): String = when (codigo) {
        401 -> AppStrings.get(R.string.create_listing_error_session)
        403 -> AppStrings.get(R.string.create_listing_error_forbidden)
        404 -> AppStrings.get(R.string.detail_error_not_found)
        else -> AppStrings.get(R.string.create_listing_error_generic)
    }

    /** Mensaje del cuerpo del error si el API lo explica; si no, el mensaje por código. */
    private fun mensajeHttpCuerpo(e: HttpException): String {
        if (e.code() == 401) return AppStrings.get(R.string.create_listing_error_session)
        val cuerpo = try {
            gson.fromJson(e.response()?.errorBody()?.string(), ErrorDto::class.java)
        } catch (x: Exception) {
            null
        }
        // El API ya explica el problema (por ejemplo "Este anuncio ya fue revisado")
        return cuerpo?.detalles?.takeIf { it.isNotEmpty() }?.joinToString("\n")
            ?: cuerpo?.error
            ?: mensajeHttp(e.code())
    }
}

/**
 * Convierte lo que regresa el API al modelo del administrador.
 * El estado DESACTIVADO sale de desactivadaPorAdmin (no de activa, que es la pausa del vendedor).
 * El historial incluye la desactivación con su motivo cuando existe.
 */
private fun PublicacionDto.toAdminListing(): AdminListing {
    val estadoApi = estado.uppercase()
    val status = when {
        estadoApi == "PUBLICADA" && desactivadaPorAdmin -> ListingStatus.DESACTIVADO
        estadoApi == "PUBLICADA" -> ListingStatus.ACTIVO
        estadoApi == "RECHAZADA" -> ListingStatus.RECHAZADO
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
    // Foto del anunciante: la misma URL que usa el resto de la app (se cachea por la versión)
    val fotoAnunciante = if (vendedor.tieneFoto && vendedor.fotoVersion != null) {
        fotoUsuarioUrl(vendedor.id, vendedor.fotoVersion)
    } else null

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
        advertiser = Advertiser(
            id = vendedor.id,
            name = vendedor.nombre,
            email = vendedor.correo,
            verified = false,
            photoUrl = fotoAnunciante
        ),
        registeredAt = fechaRegistro,
        status = status,
        history = history,
        deactivation = if (desactivadaPorAdmin) {
            DeactivationInfo(cause = motivoDesactivacion.orEmpty(), timestamp = fechaRegistro, actor = "")
        } else null
    )
}