package com.equipo4.nearhome.data.repository

import com.equipo4.nearhome.domain.model.Notificacion
import kotlinx.coroutines.flow.Flow

interface NotificacionesRepository {
    fun getNotificaciones(): Flow<List<Notificacion>>
    suspend fun marcarComoLeida(id: String)
    suspend fun alternarEstadoVacio(forceEmpty: Boolean)
}