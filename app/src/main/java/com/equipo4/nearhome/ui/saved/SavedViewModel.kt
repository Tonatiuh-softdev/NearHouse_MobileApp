package com.equipo4.nearhome.ui.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.repository.Repositories
import com.equipo4.nearhome.domain.model.Property
import com.equipo4.nearhome.domain.repository.PropiedadRepository
import com.equipo4.nearhome.util.AppStrings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SavedViewModel(
    private val repository: PropiedadRepository = Repositories.propiedades
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedUiState())
    val uiState: StateFlow<SavedUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // 1. Observar propiedades guardadas en tiempo real desde el repositorio central
            launch {
                repository.observeSaved()
                    .catch { _uiState.update { s -> s.copy(isLoading = false, loadError = true, errorMessage = AppStrings.get(R.string.saved_error_load)) } }
                    .collect { list ->
                        _uiState.update { it.copy(errorMessage = null, properties = list) }
                    }
            }

            // 2. Cargar datos iniciales desde el API
            launch {
                try {
                    _uiState.update { it.copy(isLoading = true, loadError = false) }
                    val success = repository.refresh()
                    _uiState.update { it.copy(loadError = !success) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(loadError = true) }
                } finally {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun recargar(onFinished: (Boolean) -> Unit = {}) {
        if (_uiState.value.isRefreshing) return
        _uiState.update { it.copy(isRefreshing = true, loadError = false) }
        viewModelScope.launch {
            val success = repository.refresh()
            _uiState.update { it.copy(isRefreshing = false, loadError = !success) }
            onFinished(success)
        }
    }

    fun onScreenResumed() {
        viewModelScope.launch {
            if (_uiState.value.properties.isEmpty()) {
                _uiState.update { it.copy(isLoading = true, loadError = false) }
            }
            val success = repository.refresh()
            _uiState.update { it.copy(isLoading = false, loadError = !success) }
        }
    }

    /** Quita la propiedad de guardados; guarda copia para opción "Deshacer". */
    fun onUnsave(property: Property) {
        viewModelScope.launch {
            try {
                repository.setSaved(property.id, false)
                _uiState.update {
                    it.copy(
                        lastUnsavedProperty = property,
                        snackbarMessage = "Quitada de guardados"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = "No se pudo guardar. Revisa tu conexión.") }
            }
        }
    }

    /** Deshace la acción de quitar guardado (vuelve a enviar POST a la API). */
    fun undoUnsave() {
        val propertyToRestore = _uiState.value.lastUnsavedProperty ?: return
        viewModelScope.launch {
            try {
                repository.setSaved(propertyToRestore.id, true)
                _uiState.update { it.copy(lastUnsavedProperty = null, snackbarMessage = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = "No se pudo guardar. Revisa tu conexión.") }
            }
        }
    }

    fun onSnackbarConsumed() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
