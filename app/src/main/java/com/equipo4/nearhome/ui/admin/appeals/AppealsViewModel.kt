package com.equipo4.nearhome.ui.admin.appeals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.repository.Repositories
import com.equipo4.nearhome.domain.model.Appeal
import com.equipo4.nearhome.domain.model.AppealStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Lista de apelaciones del panel: igual que la de reportes (pendientes y resueltas). */
class AppealsViewModel : ViewModel() {
    private val repository = Repositories.apelaciones

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // Al abrir la lista se piden las apelaciones al API. Si falla, se muestra lo que ya estaba cargado.
        viewModelScope.launch {
            try {
                repository.refresh()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private val appeals: StateFlow<List<Appeal>> = repository.observeAppeals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingAppeals: StateFlow<List<Appeal>> = appeals
        .map { list -> list.filter { it.status == AppealStatus.PENDIENTE } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val resolvedAppeals: StateFlow<List<Appeal>> = appeals
        .map { list -> list.filter { it.status != AppealStatus.PENDIENTE } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
