package com.equipo4.nearhome.ui.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.repository.Repositories
import com.equipo4.nearhome.domain.repository.PropiedadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.equipo4.nearhome.util.AppStrings
import com.equipo4.nearhome.R

class SavedViewModel(
    private val repository: PropiedadRepository = Repositories.propiedades
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedUiState())
    val uiState: StateFlow<SavedUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // 1. Observar propiedades guardadas
            launch {
                repository.observeSaved()
                    .catch { _uiState.update { s -> s.copy(isLoading = false, errorMessage = AppStrings.get(R.string.saved_error_load)) } }
                    .collect { list ->
                        _uiState.update { it.copy(errorMessage = null, properties = list) }
                    }
            }

            // 2. Cargar / refrescar datos iniciales
            launch {
                try {
                    _uiState.update { it.copy(isLoading = true) }
                    repository.refresh()
                } finally {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun recargar(onFinished: (Boolean) -> Unit = {}) {
        if (_uiState.value.isRefreshing) return
        _uiState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            val success = repository.refresh()
            _uiState.update { it.copy(isRefreshing = false) }
            onFinished(success)
        }
    }

    fun onScreenResumed() {
        viewModelScope.launch {
            if (_uiState.value.properties.isEmpty()) {
                _uiState.update { it.copy(isLoading = true) }
            }
            repository.refresh()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /** Quita la propiedad de guardados; la lista se actualiza sola porque observa el repositorio. */
    fun onUnsave(propertyId: String) {
        viewModelScope.launch { repository.setSaved(propertyId, false) }
    }
}