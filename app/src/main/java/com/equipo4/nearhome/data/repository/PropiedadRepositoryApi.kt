package com.equipo4.nearhome.data.repository

import android.content.Context
import android.util.Log
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.remote.PublicacionDto
import com.equipo4.nearhome.data.remote.PublicacionesApi
import com.equipo4.nearhome.di.NetworkModule
import com.equipo4.nearhome.domain.model.ListingType
import com.equipo4.nearhome.domain.model.Property
import com.equipo4.nearhome.domain.model.PropertyType
import com.equipo4.nearhome.domain.repository.PropiedadRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Collections
import kotlin.coroutines.cancellation.CancellationException

/** URL de una foto del anuncio. Es pública: Coil la carga sin sesión. */
fun fotoPublicacionUrl(fotoId: Int): String = "${NetworkModule.BASE_URL}api/v1/fotos/$fotoId"

/** Foto de perfil de cualquier usuario. La versión cambia cuando el usuario sube otra, así Coil no muestra la vieja. */
fun fotoUsuarioUrl(userId: Int, version: Long): String =
    "${NetworkModule.BASE_URL}api/v1/usuarios/$userId/foto?v=$version"

/**
 * Anuncios reales y guardados persistidos en PostgreSQL a través de la API.
 */
class PropiedadRepositoryApi(context: Context) : PropiedadRepository {

    private val appContext = context.applicationContext
    private val session = SessionManager(appContext)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val api: PublicacionesApi by lazy {
        NetworkModule.provideRetrofit(NetworkModule.provideOkHttpClient(session))
            .create(PublicacionesApi::class.java)
    }

    private val savedIds = MutableStateFlow<Set<String>>(emptySet())
    private val remote = MutableStateFlow<List<Property>>(emptyList())
    private val inFlightToggles = Collections.synchronizedSet(mutableSetOf<String>())
    private var loadJob: Job? = null
    @Volatile private var reloadPending = false

    private val all: Flow<List<Property>> =
        combine(remote, savedIds) { list, saved -> list.map { it.copy(isSaved = it.id in saved) } }

    override fun observeAll(): Flow<List<Property>> = all.onStart { reload() }

    override fun observeSaved(): Flow<List<Property>> =
        all.map { list -> list.filter { it.isSaved } }.onStart { reload() }

    override suspend fun getById(propertyId: String): Property? {
        val saved = propertyId in savedIds.value
        remote.value.firstOrNull { it.id == propertyId }?.let { return it.copy(isSaved = saved) }
        val id = propertyId.toIntOrNull() ?: return null
        return try {
            val dto = api.obtener(id)
            val property = dto.toProperty(session.getUserId())
            if (dto.guardada) {
                savedIds.update { it + propertyId }
            }
            property.copy(isSaved = propertyId in savedIds.value)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo cargar el anuncio $id: ${e.message}")
            null
        }
    }

    override suspend fun setSaved(propertyId: String, saved: Boolean) {
        val id = propertyId.toIntOrNull() ?: return
        if (!inFlightToggles.add(propertyId)) return // Ya hay una petición en curso para esta propiedad

        val previousSaved = savedIds.value
        val nextSaved = if (saved) previousSaved + propertyId else previousSaved - propertyId
        savedIds.value = nextSaved

        try {
            val response = if (saved) api.guardar(id) else api.eliminarGuardado(id)
            if (!response.isSuccessful) {
                savedIds.value = previousSaved
                throw Exception("No se pudo guardar. Revisa tu conexión.")
            }
        } catch (e: Exception) {
            savedIds.value = previousSaved
            if (e is CancellationException) throw e
            throw Exception("No se pudo guardar. Revisa tu conexión.")
        } finally {
            inFlightToggles.remove(propertyId)
        }
    }

    override suspend fun refresh(): Boolean {
        return try {
            val userId = session.getUserId()
            val list = api.listar().map { pub ->
                if (pub.guardada) {
                    savedIds.update { it + pub.id.toString() }
                }
                pub.toProperty(userId)
            }
            remote.value = list

            // Cargar también la lista completa de guardados desde el API si hay sesión iniciada.
            // Si el servidor responde con error (p. ej. sin guardados o 401), no es un problema de
            // conexión: los anuncios ya cargaron, así que no marcamos la recarga como fallida.
            // Solo un error de red (IOException) cuenta como "sin conexión".
            if (session.isLoggedIn()) {
                try {
                    val guardadosDtos = api.listarGuardados(1)
                    val newSavedIds = guardadosDtos.map { it.id.toString() }.toSet()
                    savedIds.update { it + newSavedIds }
                } catch (e: IOException) {
                    throw e
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.w(TAG, "No se pudieron cargar los guardados: ${e.message}")
                }
            }

            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "No se pudieron cargar los anuncios: ${e.message}")
            false
        }
    }

    fun clearSavedMemory() {
        savedIds.value = emptySet()
        inFlightToggles.clear()
    }

    private fun reload() {
        if (loadJob?.isActive == true) {
            reloadPending = true
            return
        }
        loadJob = scope.launch {
            do {
                reloadPending = false
                refresh()
            } while (reloadPending)
        }
    }

    private companion object {
        const val TAG = "NearHomeApi"
    }
}

/** Convierte lo que regresa el API al modelo que usan las pantallas. */
internal fun PublicacionDto.toProperty(userId: Int?): Property {
    val propertyType = runCatching { PropertyType.valueOf(tipo.uppercase()) }.getOrDefault(PropertyType.CASA)
    val esDueno = userId != null && vendedor.id == userId
    return Property(
        id = id.toString(),
        title = titulo,
        price = precio,
        listingType = if (modalidad.equals("Renta", ignoreCase = true)) ListingType.RENTA else ListingType.VENTA,
        location = listOf("$calle $numero".trim(), colonia, "Manzanillo, Colima").joinToString(", "),
        type = propertyType,
        areaSqM = superficie ?: 0,
        bedrooms = dormitorios,
        bathrooms = banos,
        garages = cochera,
        images = fotos.map { fotoPublicacionUrl(it) },
        latitude = latitud,
        longitude = longitud,
        colonia = colonia,
        niveles = niveles,
        piso = piso,
        jardinPatio = patio,
        mascotasPermitidas = mascotas,
        elevador = elevador,
        amueblado = amueblado,
        usoSuelo = when (usoSuelo?.uppercase()) {
            "HABITACIONAL" -> "Habitacional"
            "AGRICOLA" -> "Agrícola"
            else -> null
        },
        superficieSqM = superficie?.toDouble(),
        description = descripcion,
        rules = reglas,
        sellerId = vendedor.id,
        sellerName = vendedor.nombre,
        sellerPhotoUrl = if (vendedor.tieneFoto && vendedor.fotoVersion != null) {
            if (esDueno) {
                NetworkModule.fotoPerfilUrl(vendedor.fotoVersion.toString())
            } else {
                fotoUsuarioUrl(vendedor.id, vendedor.fotoVersion)
            }
        } else null,
        sellerEmail = vendedor.correo,
        sellerPhone = vendedor.telefono,
        isOwner = esDueno,
        isSaved = guardada,
        registeredAt = fechaRegistro
    )
}
