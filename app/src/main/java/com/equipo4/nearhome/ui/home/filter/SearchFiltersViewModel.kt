package com.equipo4.nearhome.ui.home.filter

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SearchFiltersViewModel : ViewModel() {
    private val _filterState = MutableStateFlow(PropertyFilterManager.filterState.value)
    val filterState: StateFlow<PropertyFilterState> = _filterState.asStateFlow()

    fun updateFilter(updateBlock: (PropertyFilterState) -> PropertyFilterState) {
        _filterState.update(updateBlock)
    }

    fun clearFilters() {
        _filterState.value = PropertyFilterState()
    }

    fun applyFilters() {
        PropertyFilterManager.updateFilters(_filterState.value)
    }
}
