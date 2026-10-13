package com.equipo4.nearhome.domain.repository

import com.equipo4.nearhome.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeUsers(): Flow<List<User>>
    suspend fun getUserById(userId: String): User?
    suspend fun updateUser(user: User)
    suspend fun deleteUser(userId: String)
    suspend fun blockUser(userId: String, reason: String?)
    suspend fun unblockUser(userId: String)
}
