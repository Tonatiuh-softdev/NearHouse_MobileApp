package com.equipo4.nearhome.ui.profile

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.local.SettingsManager
import com.equipo4.nearhome.data.session.CurrentUserStore
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

    private val settings = SettingsManager(application)
    private val authApi: AuthApi = NetworkModule.provideRetrofit(
        NetworkModule.provideOkHttpClient(SessionManager(application))
    ).create(AuthApi::class.java)

    private val _uiState = MutableStateFlow(
        ProfileUiState(
            language = run {
                val tag = AppCompatDelegate.getApplicationLocales().get(0)?.language
                when (tag) {
                    "en" -> "English"
                    "es" -> "Español"
                    else -> settings.getLanguage().takeIf { it in LanguageOptions } ?: "Español"
                }
            },
            darkModeEnabled = settings.getDarkMode() == true
        )
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _sessionExpired = Channel<Unit>(Channel.BUFFERED)
    val sessionExpired = _sessionExpired.receiveAsFlow()

    init {
        viewModelScope.launch {
            CurrentUserStore.user.collect { user ->
                _uiState.update {
                    it.copy(
                        userName = user?.let { u ->
                            listOfNotNull(u.nombre, u.primerApellido, u.segundoApellido).joinToString(" ")
                        }.orEmpty(),
                        role = UserRole.fromDb(user?.rol)
                    )
                }
            }
        }
        viewModelScope.launch {
            SettingsManager.darkModeState.collect { dark ->
                _uiState.update { it.copy(darkModeEnabled = dark == true) }
            }
        }
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            try {
                val response = CurrentUserStore.refresh(authApi)
                if (response.code() == 401) _sessionExpired.send(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Sin internet o servidor caído: no cerrar sesión
            }
        }
    }

    fun onLanguageSelected(language: String) {
        settings.saveLanguage(language)
        val tag = if (language == "English") "en" else "es"
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
        _uiState.update { it.copy(language = language) }
    }

    fun onDarkModeSelected(enabled: Boolean) {
        settings.saveDarkMode(enabled)
        _uiState.update { it.copy(darkModeEnabled = enabled) }
    }
}