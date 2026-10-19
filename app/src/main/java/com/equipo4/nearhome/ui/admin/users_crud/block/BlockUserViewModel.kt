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

import com.equipo4.nearhome.util.AppStrings
import com.equipo4.nearhome.R

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
            onSuccess(AppStrings.get(R.string.block_user_msg_reactivated))
        } else {
            userRepository.blockUser(userId, reason.value.takeIf { it.isNotBlank() })
            onSuccess(AppStrings.get(R.string.block_user_msg_blocked))
        }
    }
}

class BlockUserViewModelFactory(private val userId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BlockUserViewModel(userId) as T
    }
}