package com.equipo4.nearhome.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.equipo4.nearhome.data.local.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Lee nombre y rol de la sesión guardada. Idioma y modo oscuro solo afectan esta pantalla por ahora. */
class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val session = SessionManager(application)

    private val _uiState = MutableStateFlow(
        ProfileUiState(
            userName = session.getUserName().orEmpty(),
            role = UserRole.fromDb(session.getUserRole())
        )
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun onLanguageSelected(language: String) = _uiState.update { it.copy(language = language) }

    /** Solo para pruebas: cambia el rol y lo guarda en la sesión para que persista. */
    fun onRoleSelected(role: UserRole) {
        session.saveUser(_uiState.value.userName, role.dbValue)
        _uiState.update { it.copy(role = role) }
    }

    fun onDarkModeSelected(enabled: Boolean) = _uiState.update { it.copy(darkModeEnabled = enabled) }
}