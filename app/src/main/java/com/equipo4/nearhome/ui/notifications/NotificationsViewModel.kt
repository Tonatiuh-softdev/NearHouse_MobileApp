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

    // Contador para la campana
    val unreadCount: StateFlow<Int> = ContadorNotificaciones.noLeidas

    // Se llama cuando el ViewModel se crea (al entrar a la pantalla)
    init {
        cargarNotificacionesInicial()
        refreshUnreadCount()
    }

    private fun cargarNotificacionesInicial() {
        viewModelScope.launch {
            _uiState.value = NotificationsUiState.Loading // Siempre muestra el spinner para forzar la carga
            repository.listar().onSuccess { list ->
                _uiState.value = if (list.isEmpty()) NotificationsUiState.Empty else NotificationsUiState.Success(list)
            }.onFailure { e ->
                _uiState.value = NotificationsUiState.Error(e.message)
            }
        }
    }

    fun loadNotifications() {
        cargarNotificacionesInicial()
    }

    fun refreshUnreadCount() {
        viewModelScope.launch {
            repository.totalNoLeidas().onSuccess { count ->
                ContadorNotificaciones.actualizar(count)
            }
        }
    }

    fun recargar(onFinished: (Boolean) -> Unit = {}) {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            var success = true
            repository.listar().onSuccess { list ->
                _uiState.value = if (list.isEmpty()) NotificationsUiState.Empty else NotificationsUiState.Success(list)
            }.onFailure {
                success = false
            }
            repository.totalNoLeidas().onSuccess { count ->
                ContadorNotificaciones.actualizar(count)
            }
            _isRefreshing.value = false
            onFinished(success)
        }
    }

    fun refresh() {
        recargar()
    }

    fun marcarTodasLeidas() {
        viewModelScope.launch {
            repository.marcarTodasLeidas().onSuccess {
                ContadorNotificaciones.actualizar(0)
                cargarNotificacionesInicial()
            }
        }
    }

    fun borrarTodas() {
        viewModelScope.launch {
            repository.borrarTodas().onSuccess {
                ContadorNotificaciones.actualizar(0)
                cargarNotificacionesInicial()
            }
        }
    }

    fun borrarUna(id: String) {
        viewModelScope.launch {
            repository.borrar(id).onSuccess {
                cargarNotificacionesInicial()
                refreshUnreadCount()
            }
        }
    }

    fun onNotificationClicked(notificacion: Notificacion, onNavigate: (String) -> Unit) {
        // 1. Navegar INMEDIATAMENTE (para que no se sienta lento)
        val route = when (notificacion.tipo) {
            TipoNotificacion.BIENVENIDA -> "home"
            TipoNotificacion.PLAN_CONTRATADO -> "account_admin"
            TipoNotificacion.PUBLICACION_EN_REVISION,
            TipoNotificacion.PUBLICACION_RECHAZADA,
            TipoNotificacion.PUBLICACION_RETIRADA -> "my_listings"
            TipoNotificacion.PUBLICACION_APROBADA,
            TipoNotificacion.REPORTE_ENVIADO,
            TipoNotificacion.REPORTE_ATENDIDO,
            TipoNotificacion.REPORTE_DESCARTADO -> when {
                notificacion.publicacionId != null -> "publication_detail/${notificacion.publicacionId}"
                // Reporte a un usuario: se abre su perfil
                notificacion.usuarioReportadoId != null -> "user_profile/${notificacion.usuarioReportadoId}"
                else -> "home"
            }
            // Apelación de una publicación: "Mis anuncios"; de la cuenta: Inicio
            TipoNotificacion.APELACION_ENVIADA,
            TipoNotificacion.APELACION_ACEPTADA,
            TipoNotificacion.APELACION_RECHAZADA ->
                if (notificacion.esApelacionDeCuenta) "home" else "my_listings"
        }
        onNavigate(route)

        // 2. Mandar al servidor en segundo plano (sin bloquear la UI)
        if (!notificacion.leida) {
            viewModelScope.launch {
                repository.marcarLeida(notificacion.id).onSuccess {
                    refreshUnreadCount()
                }
            }
        }
    }
}