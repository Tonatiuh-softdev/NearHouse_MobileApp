package com.equipo4.nearhome.ui.admin.users_crud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.repository.Repositories
import com.equipo4.nearhome.domain.model.User
import com.equipo4.nearhome.domain.model.UserAccountRole
import com.equipo4.nearhome.domain.model.UserAccountStatus
import kotlinx.coroutines.flow.*

class FilterUsersViewModel(
    initialRoles: Set<UserAccountRole> = emptySet(),
    initialStatuses: Set<UserAccountStatus> = emptySet()
) : ViewModel() {
    private val userRepository = Repositories.users

    private val _selectedRoles = MutableStateFlow<Set<UserAccountRole>>(initialRoles)
    val selectedRoles: StateFlow<Set<UserAccountRole>> = _selectedRoles.asStateFlow()

    private val _selectedStatuses = MutableStateFlow<Set<UserAccountStatus>>(initialStatuses)
    val selectedStatuses: StateFlow<Set<UserAccountStatus>> = _selectedStatuses.asStateFlow()

    val allUsers: StateFlow<List<User>> = userRepository.observeUsers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val selectedFiltersCount: StateFlow<Int> = combine(_selectedRoles, _selectedStatuses) { roles, statuses ->
        roles.size + statuses.size
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val liveFilteredCount: StateFlow<Int> = combine(
        allUsers,
        _selectedRoles,
        _selectedStatuses
    ) { users, roles, statuses ->
        users.filter { user ->
            val matchesRole = roles.isEmpty() || user.role in roles
            val matchesStatus = statuses.isEmpty() || user.status in statuses
            matchesRole && matchesStatus
        }.size
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    fun toggleRole(role: UserAccountRole) {
        _selectedRoles.update { current ->
            if (role in current) current - role else current + role
        }
    }

    fun toggleStatus(status: UserAccountStatus) {
        _selectedStatuses.update { current ->
            if (status in current) current - status else current + status
        }
    }

    fun clearAllFilters() {
        _selectedRoles.value = emptySet()
        _selectedStatuses.value = emptySet()
    }

    fun getRoleCount(role: UserAccountRole, users: List<User>): Int {
        return users.count { it.role == role }
    }

    fun getStatusCount(status: UserAccountStatus, users: List<User>): Int {
        return users.count { it.status == status }
    }
}

class FilterUsersViewModelFactory(
    private val initialRoles: Set<UserAccountRole>,
    private val initialStatuses: Set<UserAccountStatus>
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FilterUsersViewModel(initialRoles, initialStatuses) as T
    }
}
