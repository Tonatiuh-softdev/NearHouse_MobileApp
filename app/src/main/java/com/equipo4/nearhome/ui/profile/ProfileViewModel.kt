package com.equipo4.nearhome.ui.profile

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Datos de usuario de prueba (sin backend). Idioma y modo oscuro solo cambian el estado de esta
 * pantalla por ahora; aplicarlos a toda la app queda pendiente.
 */
class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun onLanguageSelected(language: String) = _uiState.update { it.copy(language = language) }

    fun onDarkModeSelected(enabled: Boolean) = _uiState.update { it.copy(darkModeEnabled = enabled) }

    fun onRoleSelected(role: UserRole) = _uiState.update { it.copy(role = role) }
}
