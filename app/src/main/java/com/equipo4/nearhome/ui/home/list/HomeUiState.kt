package com.equipo4.nearhome.ui.home.list

import com.equipo4.nearhome.domain.model.Property
import com.equipo4.nearhome.ui.home.filter.PropertyFilterState

enum class SortOption {
    DEFAULT,
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW
}

data class HomeUiState(
    val searchQuery: String = "",
    val properties: List<Property> = emptyList(),
    val allProperties: List<Property> = emptyList(),
    val filterState: PropertyFilterState = PropertyFilterState(),
    val selectedTab: Int = 0,
    val sortOption: SortOption = SortOption.DEFAULT,
    val isMapView: Boolean = false,
    val selectedPropertyId: String? = null
) {
    val selectedProperty: Property?
        get() = allProperties.firstOrNull { it.id == selectedPropertyId }
}
