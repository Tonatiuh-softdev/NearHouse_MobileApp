package com.equipo4.nearhome.domain.repository

import com.equipo4.nearhome.domain.model.Appeal
import kotlinx.coroutines.flow.Flow

interface AppealRepository {
    /** Apelaciones ya cargadas: pendientes primero, después las resueltas. */
    fun observeAppeals(): Flow<List<Appeal>>

    /** Vuelve a pedir las apelaciones al API. */
    suspend fun refresh(): Result<Unit>

    /** Detalle completo de una apelación. Null si no existe. */
    suspend fun getAppealById(appealId: String): Appeal?

    /** Revierte la decisión (ACEPTADA) o la mantiene (RECHAZADA), con la respuesta para quien apeló. */
    suspend fun resolver(appealId: String, estado: String, nota: String): Result<Unit>
}
