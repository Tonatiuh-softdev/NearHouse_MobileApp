package com.equipo4.nearhome.ui.home.map

import com.equipo4.nearhome.domain.model.Property
import com.equipo4.nearhome.domain.model.PropertyType
import com.equipo4.nearhome.ui.home.filter.PropertyFilterState
import com.equipo4.nearhome.ui.home.filter.matchesFilter

enum class SortOption(val label: String) {
    NONE("Ordenar"),
    PRICE_ASC("Precio: menor a mayor"),
    PRICE_DESC("Precio: mayor a menor")
}

data class MapUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val allProperties: List<Property> = emptyList(),
    val searchQuery: String = "",
    val selectedTypes: Set<PropertyType> = emptySet(),
    val filterState: PropertyFilterState = PropertyFilterState(),
    val sort: SortOption = SortOption.NONE,
    val selectedPropertyId: String? = null
) {
    val visibleProperties: List<Property>
        get() {
            val query = searchQuery.trim()
            return allProperties
                .asSequence()
                .filter { it.latitude != null && it.longitude != null }
                .filter { selectedTypes.isEmpty() || it.type in selectedTypes }
                .filter { query.isEmpty() || it.location.contains(query, ignoreCase = true) || it.colonia.contains(query, ignoreCase = true) }
                .filter { it.matchesFilter(filterState) }
                .toList()
                .let {
                    when (sort) {
                        SortOption.NONE -> it
                        SortOption.PRICE_ASC -> it.sortedBy { p -> p.price }
                        SortOption.PRICE_DESC -> it.sortedByDescending { p -> p.price }
                    }
                }
        }

    val selectedProperty: Property?
        get() = allProperties.firstOrNull { it.id == selectedPropertyId }
}
