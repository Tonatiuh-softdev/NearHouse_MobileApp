package com.equipo4.nearhome.ui.createlisting

import com.equipo4.nearhome.domain.model.ListingType
import com.equipo4.nearhome.domain.model.PropertyType

enum class LandUse { HABITACIONAL, AGRICOLA }

const val MAX_TITLE = 80
const val MAX_DESCRIPTION = 1000
const val MAX_RULES = 300

data class CreateListingState(
    // ---- Paso 1 ----
    val listingType: ListingType? = null,
    val propertyType: PropertyType? = null,
    val price: String = "",          // solo dígitos
    val area: String = "",           // solo dígitos (solo se pide en Terreno)
    val colonia: String? = null,
    val bedrooms: Int? = null,
    val bathrooms: Int? = null,
    val parking: Int? = null,
    val floor: Int? = null,          // 0 = Planta baja
    val levels: Int? = null,
    val furnished: Boolean = false,
    val petsAllowed: Boolean = false,
    val elevator: Boolean = false,
    val garden: Boolean = false,
    val landUse: LandUse? = null,

    // ---- Paso 2 ----
    val photos: List<String> = emptyList(),   // rutas de archivo, en orden (la primera es la portada)
    val latitude: Double? = null,
    val longitude: Double? = null,
    val street: String = "",
    val streetNumber: String = "",
    val postalCode: String = "",
    val locationColonia: String? = null,      // colonia según la ubicación de la foto (manda sobre la del Paso 1)
    val locationConfirmed: Boolean = false,
    val locationLoading: Boolean = false,
    val locationError: Boolean = false,

    // ---- Paso 3 ----
    val title: String = "",
    val description: String = "",
    val rules: String = "",
    val contact: ContactoUsuario? = null,     // solo lectura: sale de la sesión / API

    // ---- Envío (Paso 4 / vista previa) ----
    val submitting: Boolean = false,
    val submitted: Boolean = false,

    // ---- Control de errores visibles ----
    val touched: Set<String> = emptySet()
) {
    val priceValid get() = (price.toLongOrNull() ?: 0L) > 0
    val areaValid get() = (area.toLongOrNull() ?: 0L) > 0

    val isRent get() = listingType == ListingType.RENTA

    /**
     * Campos visibles según operación y tipo:
     * Departamento: 9 al rentar, 8 al comprar. Casa: 8 al rentar, 7 al comprar. Terreno: 4.
     * (Mascotas permitidas solo existe al rentar.)
     */
    val fieldCount: Int
        get() = when (propertyType) {
            PropertyType.DEPARTAMENTO -> if (isRent) 9 else 8
            PropertyType.CASA -> if (isRent) 8 else 7
            PropertyType.TERRENO -> 4
            null -> 0
        }

    val isStep1Valid: Boolean
        get() {
            if (listingType == null || propertyType == null) return false
            if (!priceValid || colonia == null) return false
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
        get() = photos.isNotEmpty() && hasLocation && locationColonia != null && locationConfirmed

    /** Título y descripción son obligatorios; las reglas son opcionales. */
    val isStep3Valid get() = title.isNotBlank() && description.isNotBlank()

    /** Dirección completa para mostrar; calle, número, colonia y C.P. se guardan además por separado. */
    val fullAddress: String
        get() = buildList {
            if (street.isNotBlank()) {
                add(if (streetNumber.equals("S/N", ignoreCase = true)) "$street S/N" else "$street #$streetNumber")
            }
            locationColonia?.let { add("Col. $it") }
            if (postalCode.isNotBlank()) add("C.P. $postalCode")
            add("Manzanillo, Colima")
        }.joinToString(", ")

    /** Valor de una característica numérica por su clave. */
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

    /** Error visible solo si el usuario ya tocó el campo. */
    fun isInvalid(key: String): Boolean = key in touched && when (key) {
        "price" -> !priceValid
        "area" -> !areaValid
        "colonia" -> colonia == null
        "bedrooms", "bathrooms", "parking", "floor", "levels" -> numberOf(key) == null
        "landUse" -> landUse == null
        "title" -> title.isBlank()
        "description" -> description.isBlank()
        else -> false
    }

    /** Para el diálogo "¿Descartar anuncio?". */
    fun hasData(): Boolean = copy(touched = emptySet()) != CreateListingState()
}