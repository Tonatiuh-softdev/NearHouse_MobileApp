package com.equipo4.nearhome.data.session

import com.equipo4.nearhome.network.AuthApi
import com.equipo4.nearhome.network.UsuarioMeDto
import com.equipo4.nearhome.ui.profile.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Response

object CurrentUserStore {
    private val _user = MutableStateFlow<UsuarioMeDto?>(null)
    val user: StateFlow<UsuarioMeDto?> = _user.asStateFlow()

    /** Null mientras no haya cargado /usuarios/me. */
    fun roleOrNull(): UserRole? = _user.value?.let { UserRole.fromDb(it.rol) }

    suspend fun refresh(api: AuthApi): Response<UsuarioMeDto> {
        val response = api.me()
        if (response.isSuccessful) response.body()?.let { _user.value = it }
        return response
    }

    fun clear() {
        _user.value = null
    }
}