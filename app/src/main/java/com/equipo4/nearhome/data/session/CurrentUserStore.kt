package com.equipo4.nearhome.data.session

import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.repository.PropiedadRepositoryApi
import com.equipo4.nearhome.data.repository.Repositories
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

    /**
     * Pide /usuarios/me y guarda el resultado en memoria.
     * Si se pasa [session], también guarda el rol en SessionManager,
     * para que getUserRole() no quede con un valor viejo.
     */
    suspend fun refresh(api: AuthApi, session: SessionManager? = null): Response<UsuarioMeDto> {
        val response = api.me()
        if (response.isSuccessful) {
            response.body()?.let { usuario ->
                _user.value = usuario
                session?.saveUser(name = usuario.nombre, role = usuario.rol)
            }
        }
        return response
    }

    fun clear() {
        _user.value = null
        (Repositories.propiedades as? PropiedadRepositoryApi)?.clearSavedMemory()
    }

    fun set(user: UsuarioMeDto) {
        _user.value = user
    }
}