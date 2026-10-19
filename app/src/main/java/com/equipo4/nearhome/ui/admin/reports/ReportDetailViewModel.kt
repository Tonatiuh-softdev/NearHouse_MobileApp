package com.equipo4.nearhome.ui.admin.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.repository.Repositories
import com.equipo4.nearhome.domain.model.Property
import com.equipo4.nearhome.domain.model.Report
import com.equipo4.nearhome.domain.model.ReportType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

import com.equipo4.nearhome.util.AppStrings
import com.equipo4.nearhome.R

class ReportDetailViewModel(val reportId: String) : ViewModel() {
    private val reportRepository = Repositories.reportes
    private val propertyRepository = Repositories.propiedades

    private val _report = MutableStateFlow<Report?>(null)
    val report: StateFlow<Report?> = _report.asStateFlow()

    private val _targetProperty = MutableStateFlow<Property?>(null)
    val targetProperty: StateFlow<Property?> = _targetProperty.asStateFlow()

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
        }
    }

    suspend fun applyMeasure(onSuccess: (String) -> Unit) {
        val r = _report.value ?: return
        val measureResult = if (r.type == ReportType.PUBLICACION) {
            AppStrings.get(R.string.report_measure_listing_deactivated)
        } else {
            AppStrings.get(R.string.report_measure_user_suspended)
        }
        val dateStr = getCurrentDateFormatted()
        reportRepository.applyMeasure(reportId, measureResult, dateStr)
        onSuccess(measureResult)
    }

    suspend fun closeWithoutMeasure(onSuccess: () -> Unit) {
        val dateStr = getCurrentDateFormatted()
        reportRepository.closeWithoutMeasure(reportId, dateStr)
        onSuccess()
    }

    private fun getCurrentDateFormatted(): String {
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = java.text.DateFormatSymbols(AppStrings.locale()).shortMonths[calendar.get(Calendar.MONTH)].replace(".", "")
        return AppStrings.get(R.string.attended_date_prefix, "$day $month")
    }
}

class ReportDetailViewModelFactory(private val reportId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReportDetailViewModel(reportId) as T
    }
}