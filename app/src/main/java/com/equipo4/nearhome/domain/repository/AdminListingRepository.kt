package com.equipo4.nearhome.domain.repository

import com.equipo4.nearhome.domain.model.AdminListing
import kotlinx.coroutines.flow.StateFlow

interface AdminListingRepository {
    val listings: StateFlow<List<AdminListing>>

    fun getById(id: String): AdminListing?

    fun approve(id: String, actor: String)

    fun reject(id: String, reason: String, actor: String)

    fun deactivate(id: String, cause: String, actor: String)

    fun reactivate(id: String, actor: String)

    fun delete(id: String)
}