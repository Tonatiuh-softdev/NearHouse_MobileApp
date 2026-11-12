package com.equipo4.nearhome.ui.home.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.repository.Repositories
import com.equipo4.nearhome.domain.model.Property
import com.equipo4.nearhome.domain.model.PropertyType
import com.equipo4.nearhome.domain.repository.PropiedadRepository
import com.equipo4.nearhome.ui.home.filter.PropertyFilterManager
import com.equipo4.nearhome.ui.home.filter.matchesFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    private val repository: PropiedadRepository = Repositories.propiedades
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var allProperties: List<Property> = emptyList()

    init {
        viewModelScope.launch {
            // 1. Escuchar los cambios de la BD local
            launch {
                repository.observeAll().collect { list ->
                    allProperties = list
                    updateFilteredProperties()
                }
            }

            // 2. Escuchar cambios en los filtros
            launch {
                PropertyFilterManager.filterState.collect { filter ->
                    _uiState.update { it.copy(filterState = filter) }
                    updateFilteredProperties()
                }
            }

            // 3. Ejecutar la sincronización/carga inicial de la BD/Red.
            launch {
                try {
                    _uiState.update { it.copy(isLoading = true, loadError = false) }
                    val success = repository.refresh()
                    _uiState.update { it.copy(loadError = !success) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(loadError = true) }
                } finally {
                    withContext(Dispatchers.Default) {
                        computeFilteredList()
                    }
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    private fun updateFilteredProperties() {
        viewModelScope.launch(Dispatchers.Default) {
            val filtered = computeFilteredList()
            _uiState.update { it.copy(properties = filtered, allProperties = allProperties) }
        }
    }

    private fun computeFilteredList(): List<Property> {
        val query = _uiState.value.searchQuery.trim()
        val filter = _uiState.value.filterState
        val sortOption = _uiState.value.sortOption

        var filtered = allProperties.filter { property ->
            val matchesSearch = query.isEmpty() ||
                    property.title.contains(query, ignoreCase = true) ||
                    property.location.contains(query, ignoreCase = true) ||
                    property.colonia.contains(query, ignoreCase = true)
            matchesSearch && property.matchesFilter(filter)
        }

        return when (sortOption) {
            SortOption.PRICE_LOW_TO_HIGH -> filtered.sortedBy { it.price }
            SortOption.PRICE_HIGH_TO_LOW -> filtered.sortedByDescending { it.price }
            SortOption.DEFAULT -> filtered
        }
    }

    fun setSortOption(option: SortOption) {
        _uiState.update { it.copy(sortOption = option) }
        updateFilteredProperties()
    }

    fun onSearchQueryChange(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery) }
        updateFilteredProperties()
    }

    fun setMapView(isMap: Boolean) {
        _uiState.update { it.copy(isMapView = isMap) }
    }

    fun onTypeToggled(type: PropertyType?) {
        val currentFilter = PropertyFilterManager.filterState.value
        val nextTypes = if (type == null) {
            emptySet()
        } else {
            if (type in currentFilter.propertyTypes) currentFilter.propertyTypes - type else currentFilter.propertyTypes + type
        }
        PropertyFilterManager.updateFilters(currentFilter.copy(propertyTypes = nextTypes))
    }

    fun onMarkerSelected(propertyId: String) {
        _uiState.update { it.copy(selectedPropertyId = propertyId) }
    }

    fun onSheetDismissed() {
        _uiState.update { it.copy(selectedPropertyId = null) }
    }

    fun clearFilters() {
        PropertyFilterManager.resetFilters()
    }

    fun toggleSaveProperty(propertyId: String, onError: (String) -> Unit = {}) {
        val current = allProperties.firstOrNull { it.id == propertyId } ?: return
        viewModelScope.launch {
            try {
                repository.setSaved(propertyId, !current.isSaved)
            } catch (e: Exception) {
                onError("No se pudo guardar. Revisa tu conexión.")
            }
        }
    }

    fun recargar(onFinished: (Boolean) -> Unit = {}) {
        val current = _uiState.value
        if (current.isRefreshing || current.isLoading) return
        // Sin datos (p. ej. tras "Reintentar" en la pantalla sin conexión) mostramos
        // los skeletons en lugar del estado vacío + indicador de pull-to-refresh.
        val showSkeleton = allProperties.isEmpty()
        _uiState.update {
            if (showSkeleton) it.copy(isLoading = true, loadError = false)
            else it.copy(isRefreshing = true, loadError = false)
        }
        viewModelScope.launch {
            val success = try {
                repository.refresh()
            } catch (e: Exception) {
                false
            }
            _uiState.update { it.copy(isLoading = false, isRefreshing = false, loadError = !success) }
            onFinished(success)
        }
    }

    fun onScreenResumed() {
        viewModelScope.launch {
            if (_uiState.value.properties.isEmpty()) {
                _uiState.update { it.copy(isLoading = true, loadError = false) }
            }
            val success = repository.refresh()
            _uiState.update { it.copy(isLoading = false, loadError = !success) }
        }
    }
}
