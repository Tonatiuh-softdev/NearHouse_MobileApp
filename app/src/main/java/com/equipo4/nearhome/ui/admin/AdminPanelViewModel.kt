package com.equipo4.nearhome.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.repository.Repositories
import com.equipo4.nearhome.domain.model.ListingStatus
import com.equipo4.nearhome.domain.model.ReportStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminPanelViewModel : ViewModel() {

    init {
        refresh()
    }

    /** Pide reportes y anuncios al servidor para que los números rojos de pendientes estén al día. */
    fun refresh() {
        viewModelScope.launch { Repositories.reportes.refresh() }
        viewModelScope.launch { Repositories.anuncios.refresh() }
    }

    /** Anuncios pendientes de revisión (la cola del administrador). */
    val pendingListingsCount: StateFlow<Int> = Repositories.anuncios.listings
        .map { list -> list.count { it.status == ListingStatus.PENDIENTE } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val pendingCount: StateFlow<Int> = Repositories.reportes.observeReports()
        .map { list -> list.count { it.status == ReportStatus.PENDIENTE } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
}
