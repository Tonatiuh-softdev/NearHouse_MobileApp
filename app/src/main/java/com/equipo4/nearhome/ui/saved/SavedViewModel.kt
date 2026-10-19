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
            repository.observeSaved()
                .catch { _uiState.update { s -> s.copy(isLoading = false, errorMessage = AppStrings.get(R.string.saved_error_load)) } }
                .collect { list ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = null, properties = list) }
                }
        }
    }

    /** Quita la propiedad de guardados; la lista se actualiza sola porque observa el repositorio. */
    fun onUnsave(propertyId: String) {
        viewModelScope.launch { repository.setSaved(propertyId, false) }
    }
}