package com.equipo4.nearhome.ui.createlisting

import com.equipo4.nearhome.domain.model.ListingType
import com.equipo4.nearhome.domain.model.PropertyType
import com.equipo4.nearhome.util.AppStrings
import com.equipo4.nearhome.R

enum class LandUse { HABITACIONAL, AGRICOLA }

const val MAX_TITLE = 60
const val MIN_DESCRIPTION = 30
const val MAX_DESCRIPTION = 1000
const val MAX_RULES = 300
const val MAX_PHOTOS_APP = 5

/** Foto que ya está en el servidor (modo edición). */
data class ExistingPhoto(val id: Int, val url: String)

data class CreateListingState(
    // ---- Modo edición ----
    val editingId: Int? = null,
    val existingPhotos: List<ExistingPhoto> = emptyList(),
    val fotosEliminar: List<Int> = emptyList(),
    val reemplazaPortada: Boolean = false,
    val loadingEdit: Boolean = false,
    val loadEditError: String? = null,

    // ---- Paso 1 ----
    val listingType: ListingType? = null,
    val propertyType: PropertyType? = null,
    val price: String = "",
    val area: String = "",
    val bedrooms: Int? = null,
    val bathrooms: Int? = null,
    val parking: Int? = null,
    val floor: Int? = null,
    val levels: Int? = null,
    val furnished: Boolean = false,
    val petsAllowed: Boolean = false,
    val elevator: Boolean = false,
    val garden: Boolean = false,
    val landUse: LandUse? = null,

    // ---- Paso 2 ----
    /** Solo rutas locales de cámara (fotos nuevas). */
    val photos: List<String> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val street: String = "",
    val streetNumber: String = "",
    val postalCode: String = "",
    val locationColonia: String? = null,
    val locationConfirmed: Boolean = false,
    val locationLoading: Boolean = false,
    val locationError: Boolean = false,

    // ---- Paso 3 ----
    val title: String = "",
    val description: String = "",
    val rules: String = "",
    val contact: ContactoUsuario? = null,

    // ---- Envío ----
    val submitting: Boolean = false,
    val submitted: Boolean = false,
    val submitError: String? = null,

    val touched: Set<String> = emptySet()
) {
    val isEditing get() = editingId != null

    val totalPhotoCount get() = existingPhotos.size + photos.size

    val priceValid get() = (price.toLongOrNull() ?: 0L) > 0
    val areaValid get() = (area.toLongOrNull() ?: 0L) > 0
    val isRent get() = listingType == ListingType.RENTA

    val fieldCount: Int
        get() = when (propertyType) {
            PropertyType.DEPARTAMENTO -> if (isRent) 8 else 7
            PropertyType.CASA -> if (isRent) 7 else 6
            PropertyType.TERRENO -> 3
            null -> 0
        }

    val isStep1Valid: Boolean
        get() {
            if (listingType == null || propertyType == null) return false
            if (!priceValid) return false
            return when (propertyType) {
                PropertyType.DEPARTAMENTO ->
                    bedrooms != null && bathrooms != null && parking != null && floor != null
                PropertyType.CASA ->
                    bedrooms != null && bathrooms != null && parking != null && levels != null
                PropertyType.TERRENO -> areaValid && landUse != null
            }
        }

    val hasLocation get() = latitude != null && longitude != null

    val isStep2Valid
        get() = totalPhotoCount >= 1 &&
                hasLocation &&
                locationColonia != null &&
                locationConfirmed

    val descriptionValid get() = description.trim().length >= MIN_DESCRIPTION
    val isStep3Valid get() = title.isNotBlank() && descriptionValid

    val fullAddress: String
        get() = buildList {
            if (street.isNotBlank()) {
                add(if (streetNumber.equals("S/N", ignoreCase = true)) "$street S/N" else "$street #$streetNumber")
            }
            locationColonia?.let { add(AppStrings.get(R.string.address_colonia_format, it)) }
            if (postalCode.isNotBlank()) add(AppStrings.get(R.string.address_postal_format, postalCode))
            add("Manzanillo, Colima")
        }.joinToString(", ")

    fun numberOf(key: String): Int? = when (key) {
        "bedrooms" -> bedrooms
        "bathrooms" -> bathrooms
        "parking" -> parking
        "floor" -> floor
        "levels" -> levels
        else -> null
    }

    fun withNumber(key: String, value: Int): CreateListingState = when (key) {
        "bedrooms" -> copy(bedrooms = value)
        "bathrooms" -> copy(bathrooms = value)
        "parking" -> copy(parking = value)
        "floor" -> copy(floor = value)
        "levels" -> copy(levels = value)
        else -> this
    }

    fun isInvalid(key: String): Boolean = key in touched && when (key) {
        "price" -> !priceValid
        "area" -> !areaValid
        "bedrooms", "bathrooms", "parking", "floor", "levels" -> numberOf(key) == null
        "landUse" -> landUse == null
        "title" -> title.isBlank()
        "description" -> !descriptionValid
        else -> false
    }

    fun hasData(): Boolean {
        if (isEditing) return true
        return copy(touched = emptySet()) != CreateListingState()
    }
}