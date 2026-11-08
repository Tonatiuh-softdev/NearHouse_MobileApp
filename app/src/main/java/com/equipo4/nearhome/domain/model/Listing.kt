package com.equipo4.nearhome.domain.model

enum class ListingStatus { PENDIENTE, ACTIVO, RECHAZADO, DESACTIVADO, RENTADO_VENDIDO }

enum class ListingAction {
    APROBAR, RECHAZAR, DESACTIVAR, REACTIVAR, ELIMINAR;

    /** Estado al que pasa el anuncio. null = el anuncio se elimina. */
    val targetStatus: ListingStatus?
        get() = when (this) {
            APROBAR, REACTIVAR -> ListingStatus.ACTIVO
            RECHAZAR -> ListingStatus.RECHAZADO
            DESACTIVAR -> ListingStatus.DESACTIVADO
            ELIMINAR -> null
        }

    companion object {
        /**
         * Botones del Detalle según el estado, en el orden en que se muestran.
         * Por ahora solo se puede aprobar o rechazar un anuncio pendiente: desactivar, reactivar y eliminar
         * no tienen dónde guardarse en la base de datos.
         */
        fun availableFor(status: ListingStatus): List<ListingAction> = when (status) {
            ListingStatus.PENDIENTE -> listOf(RECHAZAR, APROBAR)
            else -> emptyList()
        }
    }
}

data class Advertiser(
    val name: String,
    val email: String,
    val verified: Boolean
)

/** Una línea del historial. from = null cuando el anuncio se registró. */
data class StatusChange(
    val from: ListingStatus?,
    val to: ListingStatus,
    val timestamp: Long,
    val actor: String,
    val reason: String? = null
)

data class DeactivationInfo(
    val cause: String,
    val timestamp: Long,
    val actor: String
)

data class AdminListing(
    val id: String,
    val title: String,
    val type: PropertyType,
    val listingType: ListingType,
    val price: Double,
    val location: String,
    /** m² del terreno. En casa y departamento es 0 (la superficie va en la descripción). */
    val areaSqM: Int,
    val bedrooms: Int? = null,
    val bathrooms: Int? = null,
    val garages: Int? = null,
    val description: String,
    /** Fotos: URLs (String) del API o R.drawable.* (Int) en los datos de prueba. */
    val images: List<Any>,
    val advertiser: Advertiser,
    val registeredAt: Long,
    val status: ListingStatus,
    /** Del más viejo al más nuevo; la pantalla lo invierte. */
    val history: List<StatusChange>,
    val deactivation: DeactivationInfo? = null
)