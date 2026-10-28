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

class ListingActionViewModel(val listingId: String, val action: ListingAction) : ViewModel() {
    private val repo = Repositories.anuncios

    /** Foto fija del anuncio al abrir la pantalla: no cambia aunque el estado cambie al confirmar. */
    val listing: AdminListing? = repo.getById(listingId)

    private val _form = MutableStateFlow(ActionForm())
    val form: StateFlow<ActionForm> = _form.asStateFlow()

    private val _submitting = MutableStateFlow(false)
    val submitting: StateFlow<Boolean> = _submitting.asStateFlow()

    /** Mensaje del API si no se pudo aplicar el cambio (por ejemplo, "Este anuncio ya fue revisado"). */
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var done = false

    fun setCheck(index: Int, value: Boolean) = _form.update { f ->
        f.copy(checks = f.checks.mapIndexed { i, c -> if (i == index) value else c })
    }

    fun setText(value: String) = _form.update { it.copy(text = value) }

    fun setAcknowledged(value: Boolean) = _form.update { it.copy(acknowledged = value) }

    /**
     * Aplica el cambio en el API. [onSuccess] se llama una sola vez, cuando el API ya lo confirmó.
     * Solo se puede aprobar y rechazar; las demás acciones todavía no existen en la base de datos.
     */
    fun confirm(actor: String, onSuccess: () -> Unit) {
        val l = listing ?: return
        val form = _form.value
        if (done || _submitting.value || !isActionFormValid(action, form)) return
        viewModelScope.launch {
            _submitting.value = true
            _error.value = null
            val resultado = when (action) {
                ListingAction.APROBAR -> repo.approve(l.id, actor)
                ListingAction.RECHAZAR -> repo.reject(l.id, form.text, actor)
                else -> Result.failure(IllegalStateException(AppStrings.get(R.string.create_listing_error_generic)))
            }
            _submitting.value = false
            resultado
                .onSuccess {
                    done = true
                    onSuccess()
                }
                .onFailure { _error.value = it.message ?: AppStrings.get(R.string.create_listing_error_generic) }
        }
    }
}