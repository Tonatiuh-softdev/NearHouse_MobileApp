package com.equipo4.nearhome.ui.profile.seller

import androidx.annotation.StringRes
import com.equipo4.nearhome.R
import com.equipo4.nearhome.domain.model.Property
import com.equipo4.nearhome.ui.profile.UserRole

enum class ListingStatus(@StringRes val labelRes: Int, val colorHex: Long) {
    IN_REVIEW(R.string.status_in_review, 0xFFF97316),
    PUBLISHED(R.string.status_published, 0xFF22C55E),
    REJECTED(R.string.status_rejected, 0xFFEF4444)
}

data class MyListingItem(
    val property: Property,
    val status: ListingStatus,
    /** Motivo que escribió el administrador al rechazar. Solo viene en los rechazados. */
    val rejectionReason: String? = null,
    /** False si el vendedor pausó el anuncio. */
    val isActive: Boolean = true,
    /** True si un administrador lo desactivó: el vendedor no puede reactivarlo. */
    val desactivadaPorAdmin: Boolean = false,
    /** Motivo que escribió el administrador al desactivar. */
    val motivoDesactivacion: String? = null,
    /** Publicado, pero oculto en Inicio porque excede el límite del plan (por ejemplo, al bajar de plan). */
    val ocultaPorLimite: Boolean = false
)

data class MyListingsUiState(
    val userName: String = "",
    val userEmail: String = "",
    val userPhone: String = "",
    val userPhotoUrl: String? = null,
    val role: UserRole = UserRole.COMPRADOR,
    val listings: List<MyListingItem> = emptyList(),
    val isLoading: Boolean = false,
    /** true mientras se reintenta desde la pantalla "sin conexión". */
    val isRetrying: Boolean = false,
    /** True si no se pudieron cargar los anuncios. */
    val loadError: Boolean = false,
    /**
     * Admin, o vendedor con plan vigente. Solo así puede crear, editar y reactivar.
     * Si es false, los anuncios se ven pero no se pueden editar ni reactivar.
     */
    val puedePublicar: Boolean = false
)