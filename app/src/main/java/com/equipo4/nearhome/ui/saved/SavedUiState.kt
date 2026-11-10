package com.equipo4.nearhome.ui.saved

import com.equipo4.nearhome.domain.model.Property

data class SavedUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isPageLoading: Boolean = false,
    val properties: List<Property> = emptyList(),
    val loadError: Boolean = false,
    val errorMessage: String? = null,
    val currentPage: Int = 1,
    val canLoadMore: Boolean = true,
    val lastUnsavedProperty: Property? = null,
    val snackbarMessage: String? = null
)
