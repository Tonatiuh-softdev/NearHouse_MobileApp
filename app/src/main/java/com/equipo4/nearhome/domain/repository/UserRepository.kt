package com.equipo4.nearhome.domain.repository

import com.equipo4.nearhome.domain.model.User
import com.equipo4.nearhome.domain.model.UserAccountRole
import com.equipo4.nearhome.domain.model.UserAccountStatus
import com.equipo4.nearhome.domain.model.UserDetail

data class UserFilters(
    val buscar: String = "",
    val rol: UserAccountRole? = null,
    val estatus: UserAccountStatus? = null
)

data class UserPage(
    val totalAccounts: Int,
    val totalResults: Int,
    val page: Int,
    val hasMore: Boolean,
    val users: List<User>
)


/** Panel de usuarios de administración. Los errores llegan como Result con mensaje listo para mostrar. */
interface UserRepository {
    suspend fun listar(filtros: UserFilters, pagina: Int): Result<UserPage>
    suspend fun detalle(userId: String): Result<UserDetail>
    suspend fun suspender(userId: String, dias: Int?, motivo: String): Result<Unit>
    suspend fun levantarSuspension(userId: String): Result<Unit>
    suspend fun eliminar(userId: String): Result<Unit>
}