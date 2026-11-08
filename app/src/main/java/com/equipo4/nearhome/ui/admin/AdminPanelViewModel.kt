package com.equipo4.nearhome.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.repository.Repositories
import com.equipo4.nearhome.domain.model.ReportStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AdminPanelViewModel : ViewModel() {
    val pendingCount: StateFlow<Int> = Repositories.reportes.observeReports()
        .map { list -> list.count { it.status == ReportStatus.PENDIENTE } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
}
