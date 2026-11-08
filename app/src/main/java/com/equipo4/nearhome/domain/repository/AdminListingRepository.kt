package com.equipo4.nearhome.domain.repository

import com.equipo4.nearhome.domain.model.AdminListing
import kotlinx.coroutines.flow.StateFlow

/**
 * Anuncios para el administrador. [listings] guarda lo último que se cargó;
 * [refresh], [approve] y [reject] hablan con el API y regresan un error con mensaje listo para mostrar.
 */
interface AdminListingRepository {
    val listings: StateFlow<List<AdminListing>>

    fun getById(id: String): AdminListing?

    /** Vuelve a pedir todos los anuncios al API. */
    suspend fun refresh(): Result<Unit>

    /** EN_REVISION -> PUBLICADA. El administrador se toma del token, [actor] solo se conserva por compatibilidad. */
    suspend fun approve(id: String, actor: String): Result<Unit>

    /** EN_REVISION -> RECHAZADA con el motivo que verá el vendedor. */
    suspend fun reject(id: String, reason: String, actor: String): Result<Unit>
}