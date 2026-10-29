package com.equipo4.nearhome.ui.profile.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.session.CurrentUserStore
import com.equipo4.nearhome.di.NetworkModule
import com.equipo4.nearhome.network.AuthApi
import com.equipo4.nearhome.network.DesactivarCuentaRequest
import com.equipo4.nearhome.network.UsuarioMeDto
import com.equipo4.nearhome.ui.profile.UserRole
import com.equipo4.nearhome.ui.subscription.PlanDates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException

class AccountAdminViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val authApi: AuthApi = NetworkModule.provideRetrofit(
        NetworkModule.provideOkHttpClient(sessionManager)
    ).create(AuthApi::class.java)

    private val _uiState = MutableStateFlow(AccountAdminUiState(isLoading = true))
    val uiState: StateFlow<AccountAdminUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            CurrentUserStore.user.collect { user ->
                _uiState.update {
                    it.copy(isLoading = false, userData = user?.toAccountData())
                }
            }
        }
    }

    private fun UsuarioMeDto.toAccountData(): UserAccountData {
        val res = getApplication<Application>().resources
        val locale = res.configuration.locales[0]
        val p = plan
        val vigente = p?.vigente == true
        return UserAccountData(
            email = correo,
            phone = telefono,
            role = UserRole.fromDb(rol),
            planName = p?.let { res.getString(R.string.plan_full_name_format, it.nombre) },
            planStatus = p?.let { res.getString(if (vigente) R.string.plan_status_active else R.string.plan_status_expired) },
            planExpired = p != null && !vigente,
            renewalDate = p?.let { PlanDates.formatear(it.fechaVencimiento, locale) },
            isGoogleUser = false
        )
    }

    fun onEvent(event: AccountAdminUiEvent) {
        when (event) {
            AccountAdminUiEvent.OnOptionClick -> {
                _uiState.update { it.copy(userMessage = R.string.coming_soon) }
            }
            AccountAdminUiEvent.OnDeactivateRequested -> {
                _uiState.update {
                    it.copy(
                        showDeactivateDialog = true,
                        deactivatePasswordInput = "",
                        deactivateError = null
                    )
                }
            }
            is AccountAdminUiEvent.OnDeactivatePasswordChanged -> {
                _uiState.update {
                    it.copy(deactivatePasswordInput = event.text, deactivateError = null)
                }
            }
            AccountAdminUiEvent.OnDeactivateDismissed -> {
                _uiState.update {
                    it.copy(
                        showDeactivateDialog = false,
                        deactivatePasswordInput = "",
                        deactivateError = null
                    )
                }
            }
            is AccountAdminUiEvent.OnDeactivateConfirmed -> {
                desactivarCuenta(event.googleToken)
            }
            AccountAdminUiEvent.OnDeleteRequested -> {
                _uiState.update { it.copy(showDeleteDialog = true, deleteInputText = "") }
            }
            AccountAdminUiEvent.OnDeleteDismissed -> {
                _uiState.update { it.copy(showDeleteDialog = false, deleteInputText = "") }
            }
            is AccountAdminUiEvent.OnDeleteInputChanged -> {
                _uiState.update { it.copy(deleteInputText = event.text) }
            }
            AccountAdminUiEvent.OnDeleteConfirmed -> {
                _uiState.update {
                    it.copy(showDeleteDialog = false, deleteInputText = "", userMessage = R.string.coming_soon)
                }
            }
            AccountAdminUiEvent.OnUserMessageConsumed -> {
                _uiState.update { it.copy(userMessage = null) }
            }
        }
    }

    private fun desactivarCuenta(googleToken: String?) {
        val currentState = _uiState.value
        if (currentState.isDeactivating) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeactivating = true, deactivateError = null) }
            try {
                val request = DesactivarCuentaRequest(
                    contrasena = currentState.deactivatePasswordInput.takeIf { it.isNotBlank() },
                    googleToken = googleToken
                )
                val response = authApi.desactivarCuenta(request)
                if (response.isSuccessful || response.code() == 204) {
                    sessionManager.clearSession()
                    _uiState.update {
                        it.copy(
                            isDeactivating = false,
                            showDeactivateDialog = false,
                            isDeactivatedSuccess = true
                        )
                    }
                } else {
                    val rawError = response.errorBody()?.string()
                    val errorMsg = parseErrorMessage(rawError)
                    _uiState.update {
                        it.copy(isDeactivating = false, deactivateError = errorMsg)
                    }
                }
            } catch (_: IOException) {
                _uiState.update {
                    it.copy(
                        isDeactivating = false,
                        deactivateError = "No se pudo conectar con el servidor. Revisa tu conexión a internet e inténtalo de nuevo"
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isDeactivating = false,
                        deactivateError = "Ocurrió un error inesperado. Inténtalo de nuevo"
                    )
                }
            }
        }
    }

    private fun parseErrorMessage(rawError: String?): String {
        if (rawError.isNullOrBlank()) return "La contraseña actual es incorrecta"
        return try {
            val json = JSONObject(rawError)
            json.optString("error", "La contraseña actual es incorrecta")
        } catch (_: Exception) {
            "La contraseña actual es incorrecta"
        }
    }
}
