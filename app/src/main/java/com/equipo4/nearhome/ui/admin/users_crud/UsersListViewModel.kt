package com.equipo4.nearhome.ui.admin.users_crud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.repository.Repositories
import com.equipo4.nearhome.domain.model.User
import com.equipo4.nearhome.domain.model.UserAccountRole
import com.equipo4.nearhome.domain.model.UserAccountStatus
import com.equipo4.nearhome.domain.repository.UserFilters
import com.equipo4.nearhome.domain.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class UsersListUiState(
    val users: List<User> = emptyList(),
    val totalAccounts: Int = 0,
    val totalResults: Int = 0,
    val page: Int = 0,
    val hasMore: Boolean = false,
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null
)

class UsersListViewModel(
    private val repository: UserRepository = Repositories.users
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedRole = MutableStateFlow<UserAccountRole?>(null)
    val selectedRole: StateFlow<UserAccountRole?> = _selectedRole.asStateFlow()

    private val _selectedStatus = MutableStateFlow<UserAccountStatus?>(null)
    val selectedStatus: StateFlow<UserAccountStatus?> = _selectedStatus.asStateFlow()

    private val _uiState = MutableStateFlow(UsersListUiState())
    val uiState: StateFlow<UsersListUiState> = _uiState.asStateFlow()

    /** Filtros con los que se cargó la lista. Si cambian mientras llega una página, esa página se descarta. */
    private var filtrosActuales = UserFilters()

    init {
        viewModelScope.launch {
            combine(
                // La búsqueda espera 500 ms después de la última letra; rol y estatus cambian al instante
                _searchQuery.debounce(SEARCH_DEBOUNCE_MS).map { it.trim() }.distinctUntilChanged(),
                _selectedRole,
                _selectedStatus
            ) { texto, rol, estatus ->
                UserFilters(buscar = texto, rol = rol, estatus = estatus)
            }
                .distinctUntilChanged()
                .collectLatest { filtros -> cargarPrimeraPagina(filtros) }
        }
    }

    private suspend fun cargarPrimeraPagina(filtros: UserFilters) {
        filtrosActuales = filtros
        _uiState.update { it.copy(isLoading = true, isLoadingMore = false, errorMessage = null) }
        repository.listar(filtros, pagina = 1)
            .onSuccess { pagina ->
                _uiState.value = UsersListUiState(
                    users = pagina.users,
                    totalAccounts = pagina.totalAccounts,
                    totalResults = pagina.totalResults,
                    page = pagina.page,
                    hasMore = pagina.hasMore,
                    isLoading = false
                )
            }
            .onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message.orEmpty()) }
            }
    }

    /** Carga la siguiente página. La pantalla lo llama al acercarse al final de la lista. */
    fun cargarMas() {
        val estado = _uiState.value
        if (!estado.hasMore || estado.isLoading || estado.isLoadingMore) return

        val filtros = filtrosActuales
        val siguiente = estado.page + 1
        _uiState.update { it.copy(isLoadingMore = true) }

        viewModelScope.launch {
            repository.listar(filtros, pagina = siguiente)
                .onSuccess { pagina ->
                    _uiState.update { actual ->
                        if (filtros != filtrosActuales) {
                            // Cambiaron los filtros mientras llegaba la página: se descarta
                            actual.copy(isLoadingMore = false)
                        } else {
                            actual.copy(
                                users = actual.users + pagina.users,
                                page = siguiente,
                                hasMore = pagina.hasMore,
                                totalResults = pagina.totalResults,
                                isLoadingMore = false
                            )
                        }
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoadingMore = false) }
                }
        }
    }

    fun reintentar() {
        viewModelScope.launch { cargarPrimeraPagina(filtrosActuales) }
    }

    fun onSearchQueryChanged(texto: String) {
        _searchQuery.value = texto
    }

    fun onRoleSelected(rol: UserAccountRole?) {
        _selectedRole.value = rol
    }

    fun onStatusSelected(estatus: UserAccountStatus?) {
        _selectedStatus.value = estatus
    }

    fun clearSearchAndFilters() {
        _searchQuery.value = ""
        _selectedRole.value = null
        _selectedStatus.value = null
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 500L
    }
}