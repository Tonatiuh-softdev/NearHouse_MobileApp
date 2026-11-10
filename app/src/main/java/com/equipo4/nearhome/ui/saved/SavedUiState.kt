package com.equipo4.nearhome.ui.saved

import com.equipo4.nearhome.domain.model.Property

data class SavedUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val loadError: Boolean = false,
    val errorMessage: String? = null,
    val properties: List<Property> = emptyList()
)
