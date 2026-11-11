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
         * Botones de la barra inferior del detalle según el estado, en el orden del mockup.
         * Desactivar, reactivar y eliminar se muestran aunque el API todavía no los atienda;
         * el ViewModel avisa con un error si se pulsan.
         */
        fun availableFor(status: ListingStatus): List<ListingAction> = when (status) {
            ListingStatus.PENDIENTE -> listOf(RECHAZAR, APROBAR, ELIMINAR)
            ListingStatus.ACTIVO -> listOf(DESACTIVAR, ELIMINAR)
            ListingStatus.DESACTIVADO -> listOf(REACTIVAR, ELIMINAR)
            ListingStatus.RECHAZADO -> listOf(APROBAR, ELIMINAR)
            ListingStatus.RENTADO_VENDIDO -> emptyList()
        }
    }
}

data class Advertiser(
    /** Id de la cuenta, para abrir su perfil. Null en datos de prueba. */
    val id: Int? = null,
    val name: String,
    val email: String,
    val verified: Boolean,
    /** URL de la foto de perfil del anunciante. Null si no tiene. */
    val photoUrl: String? = null
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