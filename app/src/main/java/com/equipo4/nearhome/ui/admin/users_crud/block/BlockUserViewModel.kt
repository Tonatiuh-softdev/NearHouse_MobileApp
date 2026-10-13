package com.equipo4.nearhome.ui.admin.users_crud.block

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.repository.Repositories
import com.equipo4.nearhome.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BlockUserViewModel(val userId: String) : ViewModel() {
    private val userRepository = Repositories.users

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    val reason = MutableStateFlow("")

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            _user.value = userRepository.getUserById(userId)
        }
    }

    suspend fun toggleBlockStatus(onSuccess: (String) -> Unit) {
        val u = _user.value ?: return
        if (u.status == com.equipo4.nearhome.domain.model.UserAccountStatus.BLOQUEADO) {
            userRepository.unblockUser(userId)
            onSuccess("Cuenta reactivada")
        } else {
            userRepository.blockUser(userId, reason.value.takeIf { it.isNotBlank() })
            onSuccess("Cuenta bloqueada")
        }
    }
}

class BlockUserViewModelFactory(private val userId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BlockUserViewModel(userId) as T
    }
}
