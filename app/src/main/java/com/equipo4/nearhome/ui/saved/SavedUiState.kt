package com.equipo4.nearhome.ui.saved

import com.equipo4.nearhome.domain.model.Property

data class SavedUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val properties: List<Property> = emptyList()
)
