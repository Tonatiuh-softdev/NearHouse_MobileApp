package com.equipo4.nearhome.ui.home.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.repository.ColoniaRepository
import com.equipo4.nearhome.domain.model.ListingType
import com.equipo4.nearhome.domain.model.PropertyType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado del catálogo de colonias del filtro de Ubicación. */
data class ColoniasUiState(
    val items: List<String> = emptyList(),
    val loading: Boolean = true,
    val error: Boolean = false
)

class SearchFiltersViewModel : ViewModel() {
    private val coloniaRepository = ColoniaRepository()

    private val _filterState = MutableStateFlow(PropertyFilterManager.filterState.value)
    val filterState: StateFlow<PropertyFilterState> = _filterState.asStateFlow()

    private val _coloniasState = MutableStateFlow(ColoniasUiState())
    val coloniasState: StateFlow<ColoniasUiState> = _coloniasState.asStateFlow()

    private val reintento = MutableStateFlow(0)

    init {
        // El catálogo se vuelve a pedir cuando cambia la operación (comprar/rentar) o el tipo de inmueble
        viewModelScope.launch {
            combine(
                _filterState.map { it.listingType to it.propertyTypes }.distinctUntilChanged(),
                reintento
            ) { clave, _ -> clave }
                .collectLatest { (operacion, tipos) -> cargarColonias(operacion, tipos) }
        }
    }

    private suspend fun cargarColonias(
        operacion: ListingType?,
        tipos: Set<PropertyType>
    ) {
        _coloniasState.update { it.copy(loading = true, error = false) }
        coloniaRepository.obtener(operacion, tipos)
            .onSuccess { nombres ->
                _coloniasState.value = ColoniasUiState(items = nombres, loading = false)
                // Si había colonias elegidas que ya no tienen anuncios en este catálogo, se quitan
                _filterState.update { s ->
                    val validas = s.selectedColonias.intersect(nombres.toSet())
                    if (validas.size == s.selectedColonias.size) s else s.copy(selectedColonias = validas)
                }
            }
            .onFailure {
                _coloniasState.value = ColoniasUiState(loading = false, error = true)
            }
    }

    fun reintentarColonias() {
        reintento.update { it + 1 }
    }

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