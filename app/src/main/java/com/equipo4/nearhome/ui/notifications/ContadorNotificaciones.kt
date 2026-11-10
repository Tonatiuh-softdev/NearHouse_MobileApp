package com.equipo4.nearhome.ui.notifications

import android.content.Context
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.repository.Repositories
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * Número de notificaciones sin leer (el circulito rojo de la campana).
 * Es uno solo para toda la app: la barra inferior de cualquier pantalla lo lee de aquí.
 *
 * Se actualiza al volver a la app, al llegar a Home, cuando llega un push con la app abierta
 * y cuando se leen o borran notificaciones.
 */
object ContadorNotificaciones {

    private val _noLeidas = MutableStateFlow(0)
    val noLeidas: StateFlow<Int> = _noLeidas.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var consulta: Job? = null

    /** Pone el número directamente (por ejemplo, después de marcar todas como leídas). */
    fun actualizar(total: Int) {
        _noLeidas.value = total.coerceAtLeast(0)
    }

    /** Vuelve a pedir el total al servidor. Sin sesión, el contador queda en 0. */
    fun refrescar(context: Context) {
        val appContext = context.applicationContext
        if (!SessionManager(appContext).isLoggedIn()) {
            limpiar()
            return
        }
        consulta?.cancel()
        consulta = scope.launch {
            try {
                // Por si Firebase abrió la app en segundo plano y MainActivity todavía no corre
                Repositories.init(appContext)
                Repositories.notificaciones.totalNoLeidas().onSuccess { actualizar(it) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Sin internet o error del servidor: se queda el último número conocido
            }
        }
    }

    /** Al cerrar sesión. */
    fun limpiar() {
        consulta?.cancel()
        _noLeidas.value = 0
    }
}
