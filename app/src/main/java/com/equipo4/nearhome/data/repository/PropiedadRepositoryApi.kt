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
import kotlin.coroutines.cancellation.CancellationException

/** URL de una foto del anuncio. Es pública: Coil la carga sin sesión. */
fun fotoPublicacionUrl(fotoId: Int): String = "${NetworkModule.BASE_URL}api/v1/fotos/$fotoId"

/**
 * Anuncios reales: lee del API los que están PUBLICADA. Cada vez que una pantalla empieza a observar
 * se vuelve a pedir la lista, así un anuncio recién aprobado aparece sin reiniciar la app.
 * "Guardados" se queda en el teléfono (la base no tiene tabla de favoritos).
 */
class PropiedadRepositoryApi(context: Context) : PropiedadRepository {

    private val appContext = context.applicationContext
    private val session = SessionManager(appContext)
    private val prefs = appContext.getSharedPreferences("nearhome_saved", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val api: PublicacionesApi by lazy {
        NetworkModule.provideRetrofit(NetworkModule.provideOkHttpClient(session))
            .create(PublicacionesApi::class.java)
    }

    private val savedIds = MutableStateFlow(prefs.getStringSet(KEY_SAVED, emptySet()).orEmpty().toSet())
    private val remote = MutableStateFlow<List<Property>>(emptyList())
    private var loadJob: Job? = null

    private val all: Flow<List<Property>> =
        combine(remote, savedIds) { list, saved -> list.map { it.copy(isSaved = it.id in saved) } }

    override fun observeAll(): Flow<List<Property>> = all.onStart { reload() }

    override fun observeSaved(): Flow<List<Property>> =
        all.map { list -> list.filter { it.isSaved } }.onStart { reload() }

    override suspend fun getById(propertyId: String): Property? {
        val saved = propertyId in savedIds.value
        remote.value.firstOrNull { it.id == propertyId }?.let { return it.copy(isSaved = saved) }
        // Un anuncio que aún no está publicado (el del propio vendedor) no está en la lista: se pide aparte
        val id = propertyId.toIntOrNull() ?: return null
        return try {
            api.obtener(id).toProperty(session.getUserId()).copy(isSaved = saved)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo cargar el anuncio $id: ${e.message}")
            null
        }
    }

    override suspend fun setSaved(propertyId: String, saved: Boolean) {
        val next = if (saved) savedIds.value + propertyId else savedIds.value - propertyId
        prefs.edit().putStringSet(KEY_SAVED, next).apply()
        savedIds.update { next }
    }

    /** Pide la lista al API. Si ya hay una petición en curso no lanza otra. */
    private fun reload() {
        if (loadJob?.isActive == true) return
        loadJob = scope.launch {
            try {
                val userId = session.getUserId()
                remote.value = api.listar().map { it.toProperty(userId) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Si falla la red se conserva lo que ya estaba cargado
                Log.w(TAG, "No se pudieron cargar los anuncios: ${e.message}")
            }
        }
    }

    private companion object {
        const val TAG = "NearHomeApi"
        const val KEY_SAVED = "saved_ids"
    }
}

/** Convierte lo que regresa el API al modelo que usan las pantallas. */
internal fun PublicacionDto.toProperty(userId: Int?): Property {
    val propertyType = runCatching { PropertyType.valueOf(tipo.uppercase()) }.getOrDefault(PropertyType.CASA)
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
        sellerName = vendedor.nombre,
        sellerEmail = vendedor.correo,
        sellerPhone = vendedor.telefono,
        isOwner = userId != null && vendedor.id == userId,
        registeredAt = fechaRegistro
    )
}