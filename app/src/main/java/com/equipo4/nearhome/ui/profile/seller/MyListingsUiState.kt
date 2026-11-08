package com.equipo4.nearhome.ui.profile.seller

import androidx.annotation.StringRes
import com.equipo4.nearhome.R
import com.equipo4.nearhome.domain.model.Property
import com.equipo4.nearhome.ui.profile.UserRole

enum class ListingStatus(@StringRes val labelRes: Int, val colorHex: Long) {
    IN_REVIEW(R.string.status_in_review, 0xFFF97316), // Naranja
    PUBLISHED(R.string.status_published, 0xFF22C55E),   // Verde
    REJECTED(R.string.status_rejected, 0xFFEF4444)     // Rojo
}

data class MyListingItem(
    val property: Property,
    val status: ListingStatus,
    /** Motivo que escribió el administrador. Solo viene en los rechazados. */
    val rejectionReason: String? = null,
    /** False si el vendedor pausó el anuncio. */
    val isActive: Boolean = true
)

data class MyListingsUiState(
    val userName: String = "",
    val userEmail: String = "",
    val userPhone: String = "",
    val userPhotoUrl: String? = null,
    val role: UserRole = UserRole.COMPRADOR,
    val listings: List<MyListingItem> = emptyList(),
    val isLoading: Boolean = false,
    /** True si no se pudieron cargar los anuncios. */
    val loadError: Boolean = false
)