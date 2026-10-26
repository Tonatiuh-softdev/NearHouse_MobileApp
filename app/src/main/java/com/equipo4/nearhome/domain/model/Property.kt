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
    val areaSqM: Int,
    val bedrooms: Int? = null,
    val bathrooms: Int? = null,
    val garages: Int? = null,
    val frontMeters: Double? = null,
    val depthMeters: Double? = null,
    /** IDs de recursos drawable (R.drawable.*). Con backend pasarán a ser URLs. */
    val images: List<Int> = emptyList(),
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
    val superficieSqM: Double? = null
)