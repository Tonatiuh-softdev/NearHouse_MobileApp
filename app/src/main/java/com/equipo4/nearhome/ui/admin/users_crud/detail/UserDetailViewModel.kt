package com.equipo4.nearhome.ui.admin.users_crud.detail

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

class UserDetailViewModel(val userId: String) : ViewModel() {
    private val userRepository = Repositories.users

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    // Form fields
    val nombre = MutableStateFlow("")
    val primerApellido = MutableStateFlow("")
    val segundoApellido = MutableStateFlow("")
    val correo = MutableStateFlow("")
    val telefono = MutableStateFlow("")

    // Errors
    val nombreError = MutableStateFlow<String?>(null)
    val apellidoError = MutableStateFlow<String?>(null)
    val correoError = MutableStateFlow<String?>(null)
    val telefonoError = MutableStateFlow<String?>(null)

    val hasValidationErrors = MutableStateFlow(false)
    val showGlobalError = MutableStateFlow(false)
    val isSavedSuccessfully = MutableStateFlow(false)

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            val u = userRepository.getUserById(userId)
            if (u != null) {
                _user.value = u
                // Parse name into parts
                val nameParts = u.name.split(" ")
                if (nameParts.size >= 2) {
                    nombre.value = nameParts.dropLast(2).joinToString(" ").ifBlank { nameParts.first() }
                    primerApellido.value = nameParts.takeLast(2).firstOrNull() ?: ""
                    segundoApellido.value = if (nameParts.size > 2) nameParts.last() else ""
                } else {
                    nombre.value = u.name
                    primerApellido.value = ""
                    segundoApellido.value = ""
                }
                correo.value = u.email
                telefono.value = u.phone
            }
        }
    }

    fun saveChanges(onSuccess: () -> Unit) {
        val n = nombre.value.trim()
        val pAp = primerApellido.value.trim()
        val c = correo.value.trim()
        val t = telefono.value.trim()

        var hasError = false

        if (n.isBlank()) {
            nombreError.value = AppStrings.get(R.string.field_required)
            hasError = true
        } else {
            nombreError.value = null
        }

        if (pAp.isBlank()) {
            apellidoError.value = AppStrings.get(R.string.field_required)
            hasError = true
        } else {
            apellidoError.value = null
        }

        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        if (c.isBlank() || !emailRegex.matches(c)) {
            correoError.value = AppStrings.get(R.string.user_detail_email_error)
            hasError = true
        } else {
            correoError.value = null
        }

        val cleanPhone = t.replace(Regex("[^0-9]"), "")
        if (cleanPhone.length < 10) {
            telefonoError.value = AppStrings.get(R.string.user_detail_phone_error)
            hasError = true
        } else {
            telefonoError.value = null
        }

        if (hasError) {
            hasValidationErrors.value = true
            showGlobalError.value = true
            return
        }

        hasValidationErrors.value = false
        showGlobalError.value = false

        viewModelScope.launch {
            val current = _user.value ?: return@launch
            val fullName = listOf(n, pAp, segundoApellido.value.trim()).filter { it.isNotBlank() }.joinToString(" ")
            val updated = current.copy(
                name = fullName,
                email = c,
                phone = t
            )
            userRepository.updateUser(updated)
            _user.value = updated
            isSavedSuccessfully.value = true
            onSuccess()
        }
    }
}

class UserDetailViewModelFactory(private val userId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UserDetailViewModel(userId) as T
    }
}