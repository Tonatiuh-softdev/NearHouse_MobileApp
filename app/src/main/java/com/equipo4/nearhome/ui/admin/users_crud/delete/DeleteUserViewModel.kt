package com.equipo4.nearhome.ui.admin.users_crud.delete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.repository.Repositories
import com.equipo4.nearhome.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DeleteUserViewModel(val userId: String) : ViewModel() {
    private val userRepository = Repositories.users

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            _user.value = userRepository.getUserById(userId)
        }
    }

    suspend fun deleteUser(onSuccess: (String) -> Unit) {
        val u = _user.value ?: return
        userRepository.deleteUser(userId)
        onSuccess("Se eliminó la cuenta de ${u.name}")
    }
}

class DeleteUserViewModelFactory(private val userId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DeleteUserViewModel(userId) as T
    }
}
