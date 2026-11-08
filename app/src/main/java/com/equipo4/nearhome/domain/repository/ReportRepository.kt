package com.equipo4.nearhome.domain.repository

import com.equipo4.nearhome.domain.model.Report
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    /** Reportes ya cargados: pendientes primero, después atendidos. */
    fun observeReports(): Flow<List<Report>>

    /** Vuelve a pedir los reportes al API. */
    suspend fun refresh(): Result<Unit>

    /** Detalle completo de un reporte. Null si no existe. */
    suspend fun getReportById(reportId: String): Report?

    /** Procede (PROCEDENTE) o descarta (DESCARTADO) un reporte. */
    suspend fun resolver(
        reportId: String,
        estado: String,
        nota: String,
        retirarPublicacion: Boolean
    ): Result<Unit>
}