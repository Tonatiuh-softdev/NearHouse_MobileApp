package com.equipo4.nearhome.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.repository.NotificacionesRepository
import com.equipo4.nearhome.domain.model.Notificacion
import com.equipo4.nearhome.domain.model.TipoNotificacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface NotificationsUiState {
    object Loading : NotificationsUiState
    object Empty : NotificationsUiState
    data class Success(val list: List<Notificacion>) : NotificationsUiState
    data class Error(val message: String? = null) : NotificationsUiState
}

class NotificationsViewModel(
    private val repository: NotificacionesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationsUiState>(NotificationsUiState.Loading)
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Contador para la campana (Barra inferior)
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    init {
        loadNotifications()
        refreshUnreadCount()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = NotificationsUiState.Loading
            repository.listar().onSuccess { list ->
                _uiState.value = if (list.isEmpty()) {
                    NotificationsUiState.Empty
                } else {
                    NotificationsUiState.Success(list)
                }
            }.onFailure { e ->
                _uiState.value = NotificationsUiState.Error(e.message)
            }
        }
    }

    fun refreshUnreadCount() {
        viewModelScope.launch {
            repository.totalNoLeidas().onSuccess { count ->
                _unreadCount.value = count
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadNotifications()
            refreshUnreadCount()
            _isRefreshing.value = false
        }
    }

    fun marcarTodasLeidas() {
        viewModelScope.launch {
            repository.marcarTodasLeidas().onSuccess {
                _unreadCount.value = 0
                loadNotifications()
            }
        }
    }

    fun borrarTodas() {
        viewModelScope.launch {
            repository.borrarTodas().onSuccess {
                _unreadCount.value = 0
                loadNotifications()
            }
        }
    }

    fun borrarUna(id: String) {
        viewModelScope.launch {
            repository.borrar(id).onSuccess {
                loadNotifications()
                refreshUnreadCount()
            }
        }
    }

    fun onNotificationClicked(notificacion: Notificacion, onNavigate: (String) -> Unit) {
        viewModelScope.launch {
            // 1. Marcar como leída si no lo está
            if (!notificacion.leida) {
                repository.marcarLeida(notificacion.id).onSuccess {
                    refreshUnreadCount()
                }
            }

            // 2. Navegar según el tipo
            val route = when (notificacion.tipo) {
                TipoNotificacion.BIENVENIDA -> "home"
                TipoNotificacion.PLAN_CONTRATADO -> "account_admin"

                TipoNotificacion.PUBLICACION_EN_REVISION,
                TipoNotificacion.PUBLICACION_RECHAZADA,
                TipoNotificacion.PUBLICACION_RETIRADA -> "my_listings"

                TipoNotificacion.PUBLICACION_APROBADA,
                TipoNotificacion.REPORTE_ENVIADO,
                TipoNotificacion.REPORTE_ATENDIDO,
                TipoNotificacion.REPORTE_DESCARTADO ->
                    if (notificacion.publicacionId != null) "publication_detail/${notificacion.publicacionId}" else "home"
            }
            onNavigate(route)
        }
    }
}