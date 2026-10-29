package com.equipo4.nearhome.domain.model

enum class ListingType {
    RENTA,
    VENTA
}

enum class PropertyType {
    DEPARTAMENTO,
    CASA,
    TERRENO
}

data class Property(
    val id: String,
    val title: String,
    val price: Double,
    val listingType: ListingType,
    val location: String,
    val type: PropertyType,
    /** m² del terreno. En casa y departamento es 0 (la superficie va en la descripción). */
    val areaSqM: Int,
    val bedrooms: Int? = null,
    val bathrooms: Int? = null,
    val garages: Int? = null,
    val frontMeters: Double? = null,
    val depthMeters: Double? = null,
    /** Fotos: URLs (String) que vienen del API, o R.drawable.* (Int) en los datos de prueba. Coil carga las dos. */
    val images: List<Any> = emptyList(),
    val isSaved: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val colonia: String = "Centro",
    val niveles: Int? = null,
    val piso: Int? = null,
    val jardinPatio: Boolean = false,
    val mascotasPermitidas: Boolean = false,
    val elevador: Boolean = false,
    val amueblado: Boolean = false,
    val usoSuelo: String? = null,
    val superficieSqM: Double? = null,
    // ---- Datos que solo trae el API (null en los datos de prueba) ----
    val description: String? = null,
    val rules: String? = null,
    val sellerName: String? = null,
    val sellerEmail: String? = null,
    val sellerPhone: String? = null,
    /** True si el anuncio es del usuario que tiene la sesión iniciada. */
    val isOwner: Boolean = false,
    val registeredAt: Long = 0L
)