package com.equipo4.nearhome.ui.subscription

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.session.CurrentUserStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChoosePlanUiState(
    val planes: List<SubscriptionPlan> = emptyList(),
    val cargando: Boolean = true,
    val errorCarga: Boolean = false,
    val pruebaDisponible: Boolean = false,
    val planActualId: Int? = null,
    val guardando: Boolean = false,
    val errorAccion: String? = null
)

class ChoosePlanViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = SubscriptionRepository(application)

    private val _uiState = MutableStateFlow(ChoosePlanUiState())
    val uiState: StateFlow<ChoosePlanUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            CurrentUserStore.user.collect { u ->
                _uiState.update {
                    it.copy(
                        pruebaDisponible = u?.pruebaDisponible == true,
                        planActualId = u?.plan?.takeIf { p -> p.vigente }?.id
                    )
                }
            }
        }
        cargarPlanes()
    }

    fun cargarPlanes() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, errorCarga = false) }
            when (val r = repo.obtenerPlanes()) {
                is Resultado.Ok -> {
                    val planes = r.valor.map { it.toModel() }
                    SubscriptionPlanCache.plans = planes
                    _uiState.update { it.copy(planes = planes, cargando = false) }
                }
                is Resultado.Error -> _uiState.update { it.copy(cargando = false, errorCarga = true) }
            }
        }
    }

    fun contratar(plan: SubscriptionPlan, onDone: () -> Unit) {
        if (_uiState.value.guardando) return
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, errorAccion = null) }
            when (val r = repo.contratar(plan.id)) {
                is Resultado.Ok -> {
                    _uiState.update { it.copy(guardando = false) }
                    onDone()
                }
                is Resultado.Error -> _uiState.update { it.copy(guardando = false, errorAccion = r.mensaje) }
            }
        }
    }
}