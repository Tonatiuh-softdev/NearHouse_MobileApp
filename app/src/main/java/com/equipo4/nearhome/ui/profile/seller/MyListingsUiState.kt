package com.equipo4.nearhome.ui.profile.seller

import com.equipo4.nearhome.domain.model.Property
import com.equipo4.nearhome.ui.profile.UserRole

enum class ListingStatus(val label: String, val colorHex: Long) {
    IN_REVIEW("En revisión", 0xFFF97316), // Naranja
    PUBLISHED("Publicado", 0xFF22C55E),   // Verde
    REJECTED("Rechazado", 0xFFEF4444)     // Rojo
}

data class MyListingItem(
    val property: Property,
    val status: ListingStatus
)

data class MyListingsUiState(
    val userName: String = "",
    val userEmail: String = "",
    val userPhone: String = "",
    val userPhotoUrl: String? = null,
    val role: UserRole = UserRole.COMPRADOR,
    val listings: List<MyListingItem> = emptyList(),
    val isLoading: Boolean = false
)