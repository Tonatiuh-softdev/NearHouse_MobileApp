package com.equipo4.nearhome.ui.admin.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.repository.Repositories
import com.equipo4.nearhome.domain.model.Property
import com.equipo4.nearhome.domain.model.Report
import com.equipo4.nearhome.domain.model.ReportType
import com.equipo4.nearhome.domain.repository.UserFilters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReportDetailViewModel(val reportId: String) : ViewModel() {
    private val reportRepository = Repositories.reportes
    private val propertyRepository = Repositories.propiedades

    private val _report = MutableStateFlow<Report?>(null)
    val report: StateFlow<Report?> = _report.asStateFlow()

    private val _targetProperty = MutableStateFlow<Property?>(null)
    val targetProperty: StateFlow<Property?> = _targetProperty.asStateFlow()

    /** Id de la cuenta que reportó, para abrir su perfil. Null si no se encontró o ya no existe. */
    private val _reporterId = MutableStateFlow<Int?>(null)
    val reporterId: StateFlow<Int?> = _reporterId.asStateFlow()

    /** True mientras se envía la resolución. Desactiva los botones. */
    private val _sending = MutableStateFlow(false)
    val sending: StateFlow<Boolean> = _sending.asStateFlow()

    /** Mensaje de error del API, para mostrarlo una vez. */
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadReport()
    }

    private fun loadReport() {
        viewModelScope.launch {
            val r = reportRepository.getReportById(reportId)
            _report.value = r
            if (r != null && r.type == ReportType.PUBLICACION && !r.targetId.isNullOrEmpty()) {
                _targetProperty.value = propertyRepository.getById(r.targetId)
            }
            // El API ya manda el id de quien reportó; si no viene (versión anterior), se busca por correo
            r?.reportedById?.let { _reporterId.value = it }
            r?.reportedByEmail?.let { buscarReportante(it) }
        }
    }

    /** El detalle del reporte no trae el id de quien reportó: se busca su cuenta por correo en el panel de usuarios. */
    private suspend fun buscarReportante(correo: String) {
        if (_reporterId.value != null) return
        Repositories.users.listar(UserFilters(buscar = correo), pagina = 1).onSuccess { page ->
            _reporterId.value = page.users
                .firstOrNull { it.email.equals(correo, ignoreCase = true) }
                ?.id?.toIntOrNull()
        }
    }

    /**
     * estado: PROCEDENTE o DESCARTADO. Si sale bien, llama [onSuccess].
     * Si falla (por ejemplo, ya fue resuelto por otro admin), muestra el error y vuelve a cargar el reporte.
     */
    fun resolver(estado: String, nota: String, retirarPublicacion: Boolean, onSuccess: () -> Unit) {
        if (_sending.value) return
        viewModelScope.launch {
            _sending.value = true
            val resultado = reportRepository.resolver(reportId, estado, nota, retirarPublicacion)
            _sending.value = false
            resultado
                .onSuccess { onSuccess() }
                .onFailure {
                    _error.value = it.message
                    loadReport()
                }
        }
    }

    fun clearError() {
        _error.value = null
    }
}

class ReportDetailViewModelFactory(private val reportId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReportDetailViewModel(reportId) as T
    }
}