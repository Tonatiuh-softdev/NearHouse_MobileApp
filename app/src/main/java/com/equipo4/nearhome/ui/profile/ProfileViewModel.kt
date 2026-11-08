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
            isLoading = true,
            language = run {
                val tag = AppCompatDelegate.getApplicationLocales().get(0)?.language
                when (tag) {
                    "en" -> "English"
                    "es" -> "Español"
                    else -> settings.getLanguage().takeIf { it in LanguageOptions } ?: "Español"
                }
            },
            darkMode = settings.getDarkMode()
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
                        isLoading = user == null,
                        userName = user?.let { u ->
                            listOfNotNull(u.nombre, u.primerApellido, u.segundoApellido).joinToString(" ")
                        }.orEmpty(),
                        userPhotoUrl = user?.takeIf { it.tieneFoto && it.fotoVersion != null }
                            ?.let { NetworkModule.fotoPerfilUrl(it.fotoVersion!!) },
                        role = UserRole.fromDb(user?.rol)
                    )
                }
            }
        }
        viewModelScope.launch {
            SettingsManager.darkModeState.collect { dark ->
                _uiState.update { it.copy(darkMode = dark) }
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
                // Sin internet o servidor caído: desactiva el esqueleto de todas formas
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onLanguageSelected(language: String) {
        settings.saveLanguage(language)
        val tag = if (language == "English") "en" else "es"
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
        _uiState.update { it.copy(language = language) }
    }

    /** mode: true = oscuro, false = claro, null = tema del sistema */
    fun onDarkModeSelected(mode: Boolean?) {
        settings.saveDarkMode(mode)
        _uiState.update { it.copy(darkMode = mode) }
    }
}