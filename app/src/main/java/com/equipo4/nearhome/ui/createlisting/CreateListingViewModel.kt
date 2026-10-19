package com.equipo4.nearhome.ui.createlisting

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.domain.model.ListingType
import com.equipo4.nearhome.domain.model.PropertyType
import com.equipo4.nearhome.ui.home.filter.ALL_COLONIAS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
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
            touched = s.touched.filter { it in setOf("price", "area", "colonia") }.toSet()
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

    /** Ubicación GPS al tomar la primera foto. Corre aquí para sobrevivir a girar el celular. */
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

    /** Al soltar el pin se recalcula la dirección (y la colonia) y hay que volver a confirmar. */
    fun onPinMoved(context: Context, lat: Double, lng: Double) {
        val app = context.applicationContext
        viewModelScope.launch { resolve(app, lat, lng) }
    }

    private suspend fun resolve(app: Context, lat: Double, lng: Double) {
        val a = LocationHelper.reverseGeocode(app, lat, lng)
        // Se intenta reconocer la colonia con todos los hints del geocoder.
        // Si no hay coincidencia confiable, queda en null y la pantalla pide elegirla a mano.
        val matched = a?.let { ColoniaMatcher.match(it.hints, ALL_COLONIAS) }
        _state.update {
            if (it.photos.isEmpty()) it   // el usuario borró todas las fotos mientras cargaba
            else it.copy(
                latitude = lat, longitude = lng,
                street = a?.street.orEmpty(),
                streetNumber = a?.number.orEmpty(),
                postalCode = a?.postalCode.orEmpty(),
                locationColonia = matched,
                colonia = matched ?: it.colonia,
                locationConfirmed = false, locationLoading = false, locationError = false
            )
        }
    }

    /** Colonia elegida a mano cuando el geocoder no la reconoció (o al corregir). */
    fun setLocationColonia(colonia: String) = _state.update {
        it.copy(
            locationColonia = colonia,
            colonia = colonia,
            locationConfirmed = false
        )
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
     * Envía el anuncio a revisión.
     * TODO: conectar con el endpoint de crear anuncio (subir fotos + datos) cuando esté disponible.
     * Por ahora solo simula el envío para poder probar el flujo completo.
     */
    fun submit() {
        if (_state.value.submitting || _state.value.submitted) return
        viewModelScope.launch {
            _state.update { it.copy(submitting = true) }
            delay(800)
            _state.update { it.copy(submitting = false, submitted = true) }
        }
    }

    // ---------------- General ----------------

    /** Descartar: borra las fotos del disco y limpia todo. */
    fun reset() {
        _state.value.photos.forEach { File(it).delete() }
        _state.value = CreateListingState()
    }
}