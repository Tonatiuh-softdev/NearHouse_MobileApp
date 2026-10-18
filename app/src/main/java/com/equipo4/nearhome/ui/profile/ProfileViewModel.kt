package com.equipo4.nearhome.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.local.SettingsManager
import com.equipo4.nearhome.di.NetworkModule
import com.equipo4.nearhome.network.AuthApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val session = SessionManager(application)
    private val settings = SettingsManager(application)
    private val authApi: AuthApi = NetworkModule.provideRetrofit(
        NetworkModule.provideOkHttpClient(session)
    ).create(AuthApi::class.java)

    private val _uiState = MutableStateFlow(
        ProfileUiState(
            userName = session.getUserName().orEmpty(),
            role = UserRole.fromDb(session.getUserRole()),
            // Idioma elegido antes (se guarda en el dispositivo); si no hay o no es válido, Español.
            language = settings.getLanguage().takeIf { it in LanguageOptions } ?: SettingsManager.DEFAULT_LANGUAGE
        )
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // Evento de una sola vez: la sesión expiró (401)
    private val _sessionExpired = Channel<Unit>(Channel.BUFFERED)
    val sessionExpired = _sessionExpired.receiveAsFlow()

    init {
        verifySession()
    }

    /** Petición con token. El interceptor ya borró la sesión si fue 401; aquí solo avisamos a la pantalla. */
    private fun verifySession() {
        viewModelScope.launch {
            try {
                val response = authApi.me()
                if (response.code() == 401) {
                    _sessionExpired.send(Unit)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Sin internet o servidor caído: NO cerrar sesión
            }
        }
    }

    fun onLanguageSelected(language: String) {
        settings.saveLanguage(language)
        _uiState.update { it.copy(language = language) }
    }

    /** Solo para pruebas: cambia el rol y lo guarda en la sesión para que persista. */
    fun onRoleSelected(role: UserRole) {
        session.saveUser(_uiState.value.userName, role.dbValue)
        _uiState.update { it.copy(role = role) }
    }

    fun onDarkModeSelected(enabled: Boolean) = _uiState.update { it.copy(darkModeEnabled = enabled) }
}