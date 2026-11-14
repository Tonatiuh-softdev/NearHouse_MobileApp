package com.equipo4.nearhome.ui.admin.appeals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.repository.Repositories
import com.equipo4.nearhome.domain.model.Appeal
import com.equipo4.nearhome.domain.model.Property
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppealDetailViewModel(val appealId: String) : ViewModel() {
    private val repository = Repositories.apelaciones
    private val propertyRepository = Repositories.propiedades

    private val _appeal = MutableStateFlow<Appeal?>(null)
    val appeal: StateFlow<Appeal?> = _appeal.asStateFlow()

    /** El anuncio apelado (solo en apelaciones de anuncio), para la tarjeta con foto y precio. */
    private val _targetProperty = MutableStateFlow<Property?>(null)
    val targetProperty: StateFlow<Property?> = _targetProperty.asStateFlow()

    /** True mientras se envía la resolución. Desactiva los botones. */
    private val _sending = MutableStateFlow(false)
    val sending: StateFlow<Boolean> = _sending.asStateFlow()

    /** Mensaje de error del API, para mostrarlo una vez. */
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val a = repository.getAppealById(appealId)
            _appeal.value = a
            a?.publicationId?.let { _targetProperty.value = propertyRepository.getById(it) }
        }
    }

    /**
     * estado: ACEPTADA (revertir) o RECHAZADA (mantener). Si sale bien, llama [onSuccess].
     * Si falla (por ejemplo, otro administrador ya la resolvió), muestra el error y la vuelve a cargar.
     */
    fun resolver(estado: String, nota: String, onSuccess: () -> Unit) {
        if (_sending.value) return
        viewModelScope.launch {
            _sending.value = true
            val resultado = repository.resolver(appealId, estado, nota)
            _sending.value = false
            resultado
                .onSuccess { onSuccess() }
                .onFailure {
                    _error.value = it.message
                    load()
                }
        }
    }

    fun clearError() {
        _error.value = null
    }
}

class AppealDetailViewModelFactory(private val appealId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AppealDetailViewModel(appealId) as T
}
