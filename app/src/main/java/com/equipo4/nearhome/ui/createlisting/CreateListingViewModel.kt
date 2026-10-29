package com.equipo4.nearhome.ui.createlisting

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.repository.PublicacionRepository
import com.equipo4.nearhome.domain.model.ListingType
import com.equipo4.nearhome.domain.model.PropertyType
import com.equipo4.nearhome.util.AppStrings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

/** Un solo ViewModel para todo el flujo (se comparte con el sub-gráfico de navegación). */
class CreateListingViewModel : ViewModel() {

    private val _state = MutableStateFlow(CreateListingState())
    val state: StateFlow<CreateListingState> = _state.asStateFlow()

    // ---------------- Paso 1 ----------------

    fun update(touch: String? = null, block: (CreateListingState) -> CreateListingState) {
        _state.update { s ->
            val next = block(s)
            if (touch != null) next.copy(touched = next.touched + touch) else next
        }
    }

    fun touch(key: String) = _state.update { it.copy(touched = it.touched + key) }

    /** "Mascotas permitidas" solo existe al rentar: al comprar se limpia para no guardar un valor oculto. */
    fun setListingType(type: ListingType) = _state.update {
        it.copy(
            listingType = type,
            petsAllowed = if (type == ListingType.VENTA) false else it.petsAllowed
        )
    }

    /** Conserva precio, superficie y colonia (y fotos/ubicación); borra los campos que ya no aplican. */
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

    fun addPhoto(path: String) = _state.update {
        if (it.photos.size >= 5) it else it.copy(photos = it.photos + path)
    }

    fun removePhoto(path: String) {
        File(path).delete()
        _state.update { s ->
            val left = s.photos - path
            if (left.isEmpty()) {
                // Sin fotos se descarta la ubicación: se vuelve a tomar con la siguiente primera foto.
                s.copy(
                    photos = left, latitude = null, longitude = null,
                    street = "", streetNumber = "", postalCode = "",
                    locationColonia = null,
                    locationConfirmed = false, locationLoading = false, locationError = false
                )
            } else s.copy(photos = left)
        }
    }

    /** Ubicación GPS al tomar la primera foto (o al volver a detectar). Corre aquí para sobrevivir a girar el celular. */
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
        // La colonia sale del geocoder; si no se logra detectar queda en null y la pantalla pide volver a detectar
        val colonia = a?.let { ColoniaDetector.detect(it) }
        _state.update {
            if (it.photos.isEmpty()) it   // el usuario borró todas las fotos mientras cargaba
            else it.copy(
                latitude = lat, longitude = lng,
                street = a?.street.orEmpty(),
                streetNumber = a?.number.orEmpty(),
                postalCode = a?.postalCode.orEmpty(),
                locationColonia = colonia,
                locationConfirmed = false, locationLoading = false, locationError = false
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

    /**
     * Envía el anuncio al API: datos + fotos comprimidas. Queda "En revisión".
     * Si falla, [CreateListingState.submitError] trae el mensaje para mostrarlo en la vista previa.
     */
    fun submit(context: Context) {
        val actual = _state.value
        if (actual.submitting || actual.submitted) return
        val repositorio = PublicacionRepository(context)
        viewModelScope.launch {
            _state.update { it.copy(submitting = true, submitError = null) }
            repositorio.crear(_state.value)
                .onSuccess {
                    _state.update { it.copy(submitting = false, submitted = true) }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            submitting = false,
                            submitError = e.message ?: AppStrings.get(R.string.create_listing_error_generic)
                        )
                    }
                }
        }
    }

    // ---------------- General ----------------

    /** Descartar: borra las fotos del disco y limpia todo. */
    fun reset() {
        _state.value.photos.forEach { File(it).delete() }
        _state.value = CreateListingState()
    }
}