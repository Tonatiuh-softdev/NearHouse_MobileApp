package com.equipo4.nearhome.ui.admin.listings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.repository.Repositories
import com.equipo4.nearhome.domain.model.AdminListing
import com.equipo4.nearhome.domain.model.ListingAction
import com.equipo4.nearhome.domain.model.ListingStatus
import com.equipo4.nearhome.domain.model.ListingType
import com.equipo4.nearhome.domain.model.PropertyType
import com.equipo4.nearhome.util.AppStrings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminListingsUiState(
    val query: String = "",
    val filters: ListingFilters = ListingFilters.Queue,
    val results: List<AdminListing> = emptyList(),
    val pendingCount: Int = 0,
    // Cantidades que se muestran en el filtro, contadas de los datos.
    val statusCounts: Map<ListingStatus, Int> = emptyMap(),
    val modalityCounts: Map<ListingType, Int> = emptyMap(),
    val typeCounts: Map<PropertyType, Int> = emptyMap(),
    /** True mientras se piden los anuncios al API. */
    val isLoading: Boolean = true,
    /** True si no se pudieron cargar. */
    val loadError: Boolean = false
) {
    /** True cuando se ve la cola de revisión sin búsqueda ni filtros distintos. */
    val isQueue: Boolean get() = query.isBlank() && filters == ListingFilters.Queue
}

/** Lista y filtros. Lo comparten la lista y la pantalla de filtros (mismo back stack entry). */
class AdminListingsViewModel : ViewModel() {
    private val repo = Repositories.anuncios
    private val query = MutableStateFlow("")
    private val filters = MutableStateFlow(ListingFilters.Queue)
    private val loading = MutableStateFlow(true)
    private val loadError = MutableStateFlow(false)

    val uiState: StateFlow<AdminListingsUiState> =
        combine(repo.listings, query, filters, loading, loadError) { all, q, f, isLoading, error ->
            AdminListingsUiState(
                query = q,
                filters = f,
                results = applyListingFilters(all, q, f),
                pendingCount = all.count { it.status == ListingStatus.PENDIENTE },
                statusCounts = ListingStatus.entries.associateWith { s -> all.count { it.status == s } },
                modalityCounts = ListingType.entries.associateWith { m -> all.count { it.listingType == m } },
                typeCounts = PropertyType.entries.associateWith { t -> all.count { it.type == t } },
                isLoading = isLoading,
                loadError = error
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, AdminListingsUiState())

    /** Vuelve a pedir los anuncios al API (al abrir la pantalla, al regresar a ella y al reintentar). */
    fun reload() {
        viewModelScope.launch {
            loading.value = true
            loadError.value = false
            repo.refresh().onFailure { loadError.value = true }
            loading.value = false
        }
    }

    fun onQueryChange(value: String) = query.update { value }

    fun applyFilters(newFilters: ListingFilters) = filters.update { newFilters }

    /** Regresa a la cola de pendientes. */
    fun clearAll() {
        query.value = ""
        filters.value = ListingFilters.Queue
    }
}

class AdminListingDetailViewModel(listingId: String) : ViewModel() {
    private val repo = Repositories.anuncios

    /** null si el anuncio no está en la lista cargada. */
    val listing: StateFlow<AdminListing?> = repo.listings
        .map { list -> list.firstOrNull { it.id == listingId } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, repo.getById(listingId))
}