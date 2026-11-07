package com.equipo4.nearhome.ui.admin.users_crud.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.repository.Repositories
import com.equipo4.nearhome.domain.model.UserDetail
import com.equipo4.nearhome.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface UserDetailUiState {
    data object Loading : UserDetailUiState
    data class Error(val mensaje: String) : UserDetailUiState
    data class Success(val user: UserDetail) : UserDetailUiState
}

class UserDetailViewModel(
    private val userId: String,
    private val repository: UserRepository = Repositories.users
) : ViewModel() {

    private val _state = MutableStateFlow<UserDetailUiState>(UserDetailUiState.Loading)
    val state: StateFlow<UserDetailUiState> = _state.asStateFlow()

    init {
        cargar()
    }

    private val _actionBusy = MutableStateFlow(false)
    val actionBusy: StateFlow<Boolean> = _actionBusy.asStateFlow()

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    /** Ejecuta una acción sobre la cuenta. Si sale bien, recarga el detalle y llama a onDone. */
    fun ejecutar(accion: suspend () -> Result<Unit>, onDone: () -> Unit) {
        viewModelScope.launch {
            _actionBusy.value = true
            _actionError.value = null
            accion()
                .onSuccess {
                    _actionBusy.value = false
                    cargar()
                    onDone()
                }
                .onFailure {
                    _actionBusy.value = false
                    _actionError.value = it.message.orEmpty()
                }
        }
    }

    fun suspender(dias: Int?, motivo: String, onDone: () -> Unit) =
        ejecutar({ repository.suspender(userId, dias, motivo) }, onDone)

    fun levantarSuspension(onDone: () -> Unit) =
        ejecutar({ repository.levantarSuspension(userId) }, onDone)

    fun eliminar(onDone: () -> Unit) =
        ejecutar({ repository.eliminar(userId) }, onDone)

    fun limpiarErrorAccion() {
        _actionError.value = null
    }

    fun cargar() {
        viewModelScope.launch {
            _state.value = UserDetailUiState.Loading
            repository.detalle(userId)
                .onSuccess { _state.value = UserDetailUiState.Success(it) }
                .onFailure { _state.value = UserDetailUiState.Error(it.message.orEmpty()) }
        }
    }
}

class UserDetailViewModelFactory(private val userId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        UserDetailViewModel(userId) as T
}