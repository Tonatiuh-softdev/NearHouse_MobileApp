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

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadProperties()
        viewModelScope.launch {
            PropertyFilterManager.filterState.collect { filter ->
                _uiState.update { it.copy(filterState = filter) }
            }
        }
    }

    fun loadProperties() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
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
        _uiState.update { state ->
            if (type == null) return@update state.copy(selectedTypes = emptySet())
            val next = if (type in state.selectedTypes) state.selectedTypes - type else state.selectedTypes + type
            state.copy(selectedTypes = if (next.size == PropertyType.entries.size) emptySet() else next)
        }
    }

    fun onSortSelected(sort: SortOption) = _uiState.update { it.copy(sort = sort) }

    fun onMarkerSelected(propertyId: String) = _uiState.update { it.copy(selectedPropertyId = propertyId) }

    fun onSheetDismissed() = _uiState.update { it.copy(selectedPropertyId = null) }

    fun toggleSaveProperty(propertyId: String) {
        val current = _uiState.value.allProperties.firstOrNull { it.id == propertyId } ?: return
        viewModelScope.launch { repository.setSaved(propertyId, !current.isSaved) }
    }
}