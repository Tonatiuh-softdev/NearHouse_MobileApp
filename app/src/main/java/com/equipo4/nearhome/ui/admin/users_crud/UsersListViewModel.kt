package com.equipo4.nearhome.ui.admin.users_crud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.repository.Repositories
import com.equipo4.nearhome.domain.model.User
import com.equipo4.nearhome.domain.model.UserAccountRole
import com.equipo4.nearhome.domain.model.UserAccountStatus
import kotlinx.coroutines.flow.*

class UsersListViewModel : ViewModel() {
    private val userRepository = Repositories.users

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedRole = MutableStateFlow<UserAccountRole?>(null)
    val selectedRole: StateFlow<UserAccountRole?> = _selectedRole.asStateFlow()

    private val _selectedStatus = MutableStateFlow<UserAccountStatus?>(null)
    val selectedStatus: StateFlow<UserAccountStatus?> = _selectedStatus.asStateFlow()

    private val _selectedRolesFilter = MutableStateFlow<Set<UserAccountRole>>(emptySet())
    val selectedRolesFilter: StateFlow<Set<UserAccountRole>> = _selectedRolesFilter.asStateFlow()

    private val _selectedStatusesFilter = MutableStateFlow<Set<UserAccountStatus>>(emptySet())
    val selectedStatusesFilter: StateFlow<Set<UserAccountStatus>> = _selectedStatusesFilter.asStateFlow()

    val users: StateFlow<List<User>> = userRepository.observeUsers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalCount: StateFlow<Int> = users.map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val activeFiltersCount: StateFlow<Int> = combine(
        _selectedRole,
        _selectedStatus,
        _selectedRolesFilter,
        _selectedStatusesFilter
    ) { r1, s1, rSet, sSet ->
        (if (r1 != null) 1 else 0) + (if (s1 != null) 1 else 0) + rSet.size + sSet.size
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val filteredUsers: StateFlow<List<User>> = combine(
        users,
        _searchQuery,
        _selectedRole,
        _selectedStatus,
        combine(_selectedRolesFilter, _selectedStatusesFilter) { r, s -> Pair(r, s) }
    ) { allUsers, query, role, status, filtersPair ->
        val rolesFilter = filtersPair.first
        val statusesFilter = filtersPair.second

        allUsers.filter { user ->
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                val cleanQuery = query.trim().lowercase()
                val cleanName = user.name.lowercase()
                val cleanEmail = user.email.lowercase()
                val cleanPhone = user.phone.replace(" ", "").lowercase()
                val queryWithoutSpaces = cleanQuery.replace(" ", "")

                cleanName.contains(cleanQuery) ||
                        cleanEmail.contains(cleanQuery) ||
                        cleanPhone.contains(queryWithoutSpaces)
            }

            val matchesRole = (role == null || user.role == role) &&
                    (rolesFilter.isEmpty() || user.role in rolesFilter)
            val matchesStatus = (status == null || user.status == status) &&
                    (statusesFilter.isEmpty() || user.status in statusesFilter)

            matchesQuery && matchesRole && matchesStatus
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onRoleSelected(role: UserAccountRole?) {
        _selectedRole.value = role
    }

    fun onStatusSelected(status: UserAccountStatus?) {
        _selectedStatus.value = status
    }

    fun applyFilters(roles: Set<UserAccountRole>, statuses: Set<UserAccountStatus>) {
        _selectedRolesFilter.value = roles
        _selectedStatusesFilter.value = statuses
    }

    fun clearSearchAndFilters() {
        _searchQuery.value = ""
        _selectedRole.value = null
        _selectedStatus.value = null
        _selectedRolesFilter.value = emptySet()
        _selectedStatusesFilter.value = emptySet()
    }
}
