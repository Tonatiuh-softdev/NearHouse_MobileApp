package com.equipo4.nearhome.ui.home.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.repository.Repositories
import com.equipo4.nearhome.domain.model.Property
import com.equipo4.nearhome.domain.repository.PropiedadRepository
import com.equipo4.nearhome.ui.home.filter.PropertyFilterManager
import com.equipo4.nearhome.ui.home.filter.matchesFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: PropiedadRepository = Repositories.propiedades
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var allProperties: List<Property> = emptyList()

    init {
        viewModelScope.launch {
            launch {
                repository.observeAll().collect { list ->
                    allProperties = list
                    updateFilteredProperties()
                }
            }
            launch {
                PropertyFilterManager.filterState.collect { filter ->
                    _uiState.update { it.copy(filterState = filter) }
                    updateFilteredProperties()
                }
            }
        }
    }

    private fun updateFilteredProperties() {
        val query = _uiState.value.searchQuery.trim()
        val filter = _uiState.value.filterState
        val filtered = allProperties.filter { property ->
            val matchesSearch = query.isEmpty() ||
                property.title.contains(query, ignoreCase = true) ||
                property.location.contains(query, ignoreCase = true) ||
                property.colonia.contains(query, ignoreCase = true)
            matchesSearch && property.matchesFilter(filter)
        }
        _uiState.update { it.copy(properties = filtered) }
    }

    fun onSearchQueryChange(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery) }
        updateFilteredProperties()
    }

    fun clearFilters() {
        PropertyFilterManager.resetFilters()
    }

    fun toggleSaveProperty(propertyId: String) {
        val current = allProperties.firstOrNull { it.id == propertyId } ?: return
        viewModelScope.launch { repository.setSaved(propertyId, !current.isSaved) }
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }
}
