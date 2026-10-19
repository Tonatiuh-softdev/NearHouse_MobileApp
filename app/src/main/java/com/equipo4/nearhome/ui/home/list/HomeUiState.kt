package com.equipo4.nearhome.ui.home.list

import com.equipo4.nearhome.domain.model.Property
import com.equipo4.nearhome.ui.home.filter.PropertyFilterState

data class HomeUiState(
    val searchQuery: String = "",
    val properties: List<Property> = emptyList(),
    val filterState: PropertyFilterState = PropertyFilterState(),
    val selectedTab: Int = 0
)
