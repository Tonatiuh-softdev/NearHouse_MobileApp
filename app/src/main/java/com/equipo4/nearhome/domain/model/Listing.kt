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
        /** Botones del Detalle según el estado, en el orden en que se muestran. */
        fun availableFor(status: ListingStatus): List<ListingAction> = when (status) {
            ListingStatus.PENDIENTE -> listOf(RECHAZAR, APROBAR, ELIMINAR)
            ListingStatus.ACTIVO -> listOf(DESACTIVAR, ELIMINAR)
            ListingStatus.DESACTIVADO -> listOf(REACTIVAR, ELIMINAR)
            ListingStatus.RECHAZADO, ListingStatus.RENTADO_VENDIDO -> listOf(ELIMINAR)
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
    val areaSqM: Int,
    val bedrooms: Int? = null,
    val bathrooms: Int? = null,
    val garages: Int? = null,
    val description: String,
    /** IDs de drawable (con backend serán URLs). */
    val images: List<Int>,
    val advertiser: Advertiser,
    val registeredAt: Long,
    val status: ListingStatus,
    /** Del más viejo al más nuevo; la pantalla lo invierte. */
    val history: List<StatusChange>,
    val deactivation: DeactivationInfo? = null
)
