package com.equipo4.nearhome.data.repository

import com.equipo4.nearhome.domain.model.Notificacion

interface NotificacionesRepository {
    suspend fun listar(pagina: Int = 1): Result<List<Notificacion>>
    suspend fun totalNoLeidas(): Result<Int>
    suspend fun marcarLeida(id: String): Result<Unit>
    suspend fun marcarTodasLeidas(): Result<Unit>
    suspend fun borrar(id: String): Result<Unit>
    suspend fun borrarTodas(): Result<Unit>
    suspend fun registrarDispositivo(token: String, idioma: String): Result<Unit>
    suspend fun bajaDispositivo(token: String): Result<Unit>
}