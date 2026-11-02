package com.equipo4.nearhome.ui.createlisting

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.remote.PublicacionDto
import com.equipo4.nearhome.data.remote.PublicacionesApi
import com.equipo4.nearhome.data.repository.PublicacionRepository
import com.equipo4.nearhome.data.repository.fotoPublicacionUrl
import com.equipo4.nearhome.di.NetworkModule
import com.equipo4.nearhome.domain.model.ListingType
import com.equipo4.nearhome.domain.model.PropertyType
import com.equipo4.nearhome.util.AppStrings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

class CreateListingViewModel : ViewModel() {

    private val _state = MutableStateFlow(CreateListingState())
    val state: StateFlow<CreateListingState> = _state.asStateFlow()

    // ---------------- Cargar para editar ----------------

    fun loadForEdit(context: Context, id: Int) {
        val actual = _state.value
        if (actual.editingId == id && !actual.loadingEdit && actual.loadEditError == null &&
            (actual.title.isNotBlank() || actual.existingPhotos.isNotEmpty())
        ) return

        viewModelScope.launch {
            _state.update {
                it.copy(loadingEdit = true, loadEditError = null, editingId = id)
            }
            try {
                val session = SessionManager(context.applicationContext)
                val api = NetworkModule.provideRetrofit(NetworkModule.provideOkHttpClient(session))
                    .create(PublicacionesApi::class.java)
                val dto = api.obtener(id)
                _state.value = dto.toCreateListingState()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        loadingEdit = false,
                        loadEditError = e.message
                            ?: AppStrings.get(R.string.create_listing_error_generic)
                    )
                }
            }
        }
    }

    private fun PublicacionDto.toCreateListingState(): CreateListingState {
        val tipo = runCatching { PropertyType.valueOf(tipo.uppercase()) }.getOrNull()
        val listingType =
            if (modalidad.equals("Renta", ignoreCase = true)) ListingType.RENTA else ListingType.VENTA
        return CreateListingState(
            editingId = id,
            listingType = listingType,
            propertyType = tipo,
            price = precio.toLong().toString(),
            area = superficie?.toString().orEmpty(),
            bedrooms = dormitorios,
            bathrooms = banos,
            parking = cochera,
            floor = piso,
            levels = niveles,
            furnished = amueblado,
            petsAllowed = mascotas,
            elevator = elevador,
            garden = patio,
            landUse = when (usoSuelo?.uppercase()) {
                "HABITACIONAL" -> LandUse.HABITACIONAL
                "AGRICOLA" -> LandUse.AGRICOLA
                else -> null
            },
            existingPhotos = fotos.map { ExistingPhoto(it, fotoPublicacionUrl(it)) },
            photos = emptyList(),
            fotosEliminar = emptyList(),
            reemplazaPortada = false,
            latitude = latitud,
            longitude = longitud,
            street = calle,
            streetNumber = numero,
            postalCode = codigoPostal.orEmpty(),
            locationColonia = colonia,
            locationConfirmed = true,
            title = titulo,
            description = descripcion,
            rules = reglas.orEmpty(),
            loadingEdit = false,
            loadEditError = null
        )
    }

    // ---------------- Paso 1 ----------------

    fun update(touch: String? = null, block: (CreateListingState) -> CreateListingState) {
        _state.update { s ->
            val next = block(s)
            if (touch != null) next.copy(touched = next.touched + touch) else next
        }
    }

    fun touch(key: String) = _state.update { it.copy(touched = it.touched + key) }

    fun setListingType(type: ListingType) = _state.update {
        it.copy(
            listingType = type,
            petsAllowed = if (type == ListingType.VENTA) false else it.petsAllowed
        )
    }

    fun setPropertyType(type: PropertyType) = _state.update { s ->
        if (s.propertyType == type) s
        else s.copy(
            propertyType = type,
            bedrooms = null, bathrooms = null, parking = null, floor = null, levels = null,
            furnished = false, petsAllowed = false, elevator = false, garden = false,
            landUse = null,
            touched = s.touched.filter { it in setOf("price", "area") }.toSet()
        )
    }

    // ---------------- Paso 2 ----------------

    fun addPhoto(path: String) = _state.update { s ->
        if (s.totalPhotoCount >= MAX_PHOTOS_APP) s
        else s.copy(photos = s.photos + path)
    }

    fun removePhoto(path: String) {
        File(path).delete()
        _state.update { s ->
            val left = s.photos - path
            if (left.isEmpty() && s.existingPhotos.isEmpty()) {
                s.copy(
                    photos = left,
                    latitude = null, longitude = null,
                    street = "", streetNumber = "", postalCode = "",
                    locationColonia = null,
                    locationConfirmed = false, locationLoading = false, locationError = false
                )
            } else {
                s.copy(photos = left)
            }
        }
    }

    fun removeExistingPhoto(id: Int) {
        _state.update { s ->
            val left = s.existingPhotos.filterNot { it.id == id }
            val wasCover = s.existingPhotos.firstOrNull()?.id == id
            s.copy(
                existingPhotos = left,
                fotosEliminar = (s.fotosEliminar + id).distinct(),
                // Si se quitó la portada y hay fotos nuevas, la primera nueva será portada
                reemplazaPortada = s.reemplazaPortada || (wasCover && s.photos.isNotEmpty())
            )
        }
    }

    fun locate(context: Context) {
        val app = context.applicationContext
        viewModelScope.launch {
            _state.update { it.copy(locationLoading = true, locationError = false) }
            val loc = LocationHelper.current(app)
            if (loc == null) {
                _state.update { it.copy(locationLoading = false, locationError = true) }
            } else {
                resolve(app, loc.latitude, loc.longitude)
            }
        }
    }

    private suspend fun resolve(app: Context, lat: Double, lng: Double) {
        val a = LocationHelper.reverseGeocode(app, lat, lng)
        val colonia = a?.let { ColoniaDetector.detect(it) }
        _state.update {
            if (it.totalPhotoCount == 0) it
            else it.copy(
                latitude = lat, longitude = lng,
                street = a?.street.orEmpty(),
                streetNumber = a?.number.orEmpty(),
                postalCode = a?.postalCode.orEmpty(),
                locationColonia = colonia,
                locationConfirmed = false,
                locationLoading = false,
                locationError = false
            )
        }
    }

    fun updateAddress(street: String? = null, number: String? = null, postalCode: String? = null) =
        _state.update {
            it.copy(
                street = street ?: it.street,
                streetNumber = number ?: it.streetNumber,
                postalCode = postalCode ?: it.postalCode,
                locationConfirmed = false
            )
        }

    fun confirmLocation() = _state.update {
        if (it.hasLocation && it.locationColonia != null &&
            it.street.isNotBlank() && it.streetNumber.isNotBlank()
        ) it.copy(locationConfirmed = true) else it
    }

    // ---------------- Paso 3 ----------------

    fun setContact(contact: ContactoUsuario) = _state.update { it.copy(contact = contact) }

    // ---------------- Envío ----------------

    fun submit(context: Context) {
        val actual = _state.value
        if (actual.submitting || actual.submitted) return
        val repositorio = PublicacionRepository(context)
        viewModelScope.launch {
            _state.update { it.copy(submitting = true, submitError = null) }
            val result = if (actual.editingId != null) {
                repositorio.editar(actual.editingId, actual).map { Unit }
            } else {
                repositorio.crear(actual).map { Unit }
            }
            result
                .onSuccess {
                    _state.update { it.copy(submitting = false, submitted = true) }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            submitting = false,
                            submitError = e.message
                                ?: AppStrings.get(R.string.create_listing_error_generic)
                        )
                    }
                }
        }
    }

    // ---------------- General ----------------

    fun reset() {
        _state.value.photos.forEach { File(it).delete() }
        _state.value = CreateListingState()
    }
}