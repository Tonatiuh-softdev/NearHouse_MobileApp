package com.equipo4.nearhome.domain.repository

import com.equipo4.nearhome.domain.model.Report
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    fun observeReports(): Flow<List<Report>>
    suspend fun getReportById(reportId: String): Report?
    suspend fun applyMeasure(reportId: String, measureResult: String, attendedDate: String)
    suspend fun closeWithoutMeasure(reportId: String, attendedDate: String)
}
