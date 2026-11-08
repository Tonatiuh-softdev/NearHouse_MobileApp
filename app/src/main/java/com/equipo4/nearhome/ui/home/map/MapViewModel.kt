package com.equipo4.nearhome.ui.home.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.repository.Repositories
import com.equipo4.nearhome.domain.model.PropertyType
import com.equipo4.nearhome.domain.repository.PropiedadRepository
import com.equipo4.nearhome.ui.home.filter.PropertyFilterManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.equipo4.nearhome.util.AppStrings
import com.equipo4.nearhome.R

class MapViewModel(
    private val repository: PropiedadRepository = Repositories.propiedades
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MapUiState(
            isLoading = false,
            filterState = PropertyFilterManager.filterState.value,
            selectedTypes = PropertyFilterManager.filterState.value.propertyTypes
        )
    )
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadProperties()
        viewModelScope.launch {
            PropertyFilterManager.filterState.collect { filter ->
                _uiState.update { it.copy(filterState = filter, selectedTypes = filter.propertyTypes) }
            }
        }
    }

    fun loadProperties() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            repository.observeAll()
                .catch {
                    _uiState.update { s ->
                        s.copy(isLoading = false, errorMessage = AppStrings.get(R.string.map_error_load))
                    }
                }
                .collect { list ->
                    _uiState.update { it.copy(isLoading = false, allProperties = list) }
                }
        }
    }

    fun onSearchQueryChange(query: String) = _uiState.update { it.copy(searchQuery = query) }

    fun onTypeToggled(type: PropertyType?) {
        val currentFilter = PropertyFilterManager.filterState.value
        val nextTypes = if (type == null) {
            emptySet()
        } else {
            if (type in currentFilter.propertyTypes) currentFilter.propertyTypes - type else currentFilter.propertyTypes + type
        }
        PropertyFilterManager.updateFilters(currentFilter.copy(propertyTypes = nextTypes))
        _uiState.update { it.copy(selectedTypes = nextTypes) }
    }

    fun onSortSelected(sort: SortOption) = _uiState.update { it.copy(sort = sort) }

    fun onMarkerSelected(propertyId: String) = _uiState.update { it.copy(selectedPropertyId = propertyId) }

    fun onSheetDismissed() = _uiState.update { it.copy(selectedPropertyId = null) }

    fun toggleSaveProperty(propertyId: String) {
        val current = _uiState.value.allProperties.firstOrNull { it.id == propertyId } ?: return
        viewModelScope.launch { repository.setSaved(propertyId, !current.isSaved) }
    }
}
