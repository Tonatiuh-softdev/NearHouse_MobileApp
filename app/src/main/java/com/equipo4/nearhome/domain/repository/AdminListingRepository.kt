package com.equipo4.nearhome.domain.repository

import com.equipo4.nearhome.domain.model.AdminListing
import kotlinx.coroutines.flow.StateFlow

interface AdminListingRepository {
    val listings: StateFlow<List<AdminListing>>

    fun getById(id: String): AdminListing?

    suspend fun refresh(): Result<Unit>

    suspend fun approve(id: String, actor: String): Result<Unit>

    suspend fun reject(id: String, reason: String, actor: String): Result<Unit>

    suspend fun desactivar(id: String, motivo: String): Result<Unit>

    suspend fun reactivar(id: String): Result<Unit>

    suspend fun eliminar(id: String): Result<Unit>
}