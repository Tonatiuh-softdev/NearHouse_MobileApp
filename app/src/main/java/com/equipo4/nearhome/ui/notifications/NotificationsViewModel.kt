package com.equipo4.nearhome.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.repository.NotificacionesRepository
import com.equipo4.nearhome.domain.model.Notificacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = NotificationsUiState.Loading
            repository.getNotificaciones()
                .catch { e -> _uiState.value = NotificationsUiState.Error(e.message) }
                .collect { list ->
                    val sortedList = list.sortedByDescending { it.timestampMs }
                    _uiState.value = if (sortedList.isEmpty()) {
                        NotificationsUiState.Empty
                    } else {
                        NotificationsUiState.Success(sortedList)
                    }
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadNotifications()
            _isRefreshing.value = false
        }
    }

    fun onNotificationClicked(notificacion: Notificacion, onNavigate: (String) -> Unit) {
        viewModelScope.launch {
            if (!notificacion.leida) {
                repository.marcarComoLeida(notificacion.id)
            }
            // Rutas de navegación según el requerimiento
            val route = when (notificacion.tipo) {
                com.equipo4.nearhome.domain.model.TipoNotificacion.EN_REVISION,
                com.equipo4.nearhome.domain.model.TipoNotificacion.RECHAZADA -> "my_listings"

                com.equipo4.nearhome.domain.model.TipoNotificacion.APROBADA,
                com.equipo4.nearhome.domain.model.TipoNotificacion.NOVEDADES_BUSQUEDA,
                com.equipo4.nearhome.domain.model.TipoNotificacion.ENCONTRAMOS_ALGO,
                com.equipo4.nearhome.domain.model.TipoNotificacion.RECIEN_PUBLICADAS -> "publication_detail/${notificacion.publicacionId ?: ""}"

                com.equipo4.nearhome.domain.model.TipoNotificacion.PAGO,
                com.equipo4.nearhome.domain.model.TipoNotificacion.BIENVENIDA -> "account_management/current_plan"
            }
            onNavigate(route)
        }
    }
}