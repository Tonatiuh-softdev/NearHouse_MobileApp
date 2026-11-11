package com.equipo4.nearhome.ui.admin.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.repository.Repositories
import com.equipo4.nearhome.domain.model.Report
import com.equipo4.nearhome.domain.model.ReportStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReportsViewModel : ViewModel() {
    private val reportRepository = Repositories.reportes

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // Al abrir la lista se piden los reportes al API. Si falla, se muestra lo que ya estaba cargado.
        viewModelScope.launch {
            try {
                reportRepository.refresh()
            } finally {
                _isLoading.value = false
            }
        }
    }

    val reports: StateFlow<List<Report>> = reportRepository.observeReports()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val pendingReports: StateFlow<List<Report>> = reports.map { list ->
        list.filter { it.status == ReportStatus.PENDIENTE }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val attendedReports: StateFlow<List<Report>> = reports.map { list ->
        list.filter { it.status == ReportStatus.ATENDIDO }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val pendingCount: StateFlow<Int> = pendingReports.map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
}