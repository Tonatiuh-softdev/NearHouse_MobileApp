package com.equipo4.nearhome.ui.profile.publicprofile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.repository.Repositories
import com.equipo4.nearhome.data.repository.fotoUsuarioAdminUrl
import com.equipo4.nearhome.domain.model.Property
import com.equipo4.nearhome.domain.model.UserAccountRole
import com.equipo4.nearhome.domain.model.UserDetail
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserProfileUiState(
    val isLoading: Boolean = true,
    val name: String? = null,
    val photoUrl: String? = null,
    val email: String? = null,
    val phone: String? = null,
    /** Solo anuncios PUBLICADA y activos (los mismos que ve cualquiera en el home): nunca pausados, en revisión ni rechazados. */
    val listings: List<Property> = emptyList(),
    /** Quien mira es administrador: se muestran los datos de la cuenta y el botón para administrarla. */
    val viewerIsAdmin: Boolean = false,
    /** El perfil es de quien tiene la sesión: no se muestra el botón de reportar. */
    val isOwnProfile: Boolean = false,
    /** Datos de la cuenta. Solo se piden si quien mira es administrador. */
    val account: UserDetail? = null
)

/**
 * Perfil de un usuario visto por otra persona.
 * Los anuncios salen de la lista pública de publicaciones (filtrada por vendedor), así no hace falta
 * un endpoint nuevo. Si quien mira es administrador, además se piden los datos de la cuenta.
 */
class UserProfileViewModel(
    application: Application,
    private val userId: Int
) : AndroidViewModel(application) {

    private val session = SessionManager(application)
    private val propiedades = Repositories.propiedades

    private val _uiState = MutableStateFlow(
        UserProfileUiState(
            isOwnProfile = session.getUserId() == userId,
            viewerIsAdmin = session.getUserRole() in setOf(
                UserAccountRole.ADMINISTRADOR.apiName,
                UserAccountRole.SUPER_ADMIN.apiName
            )
        )
    )
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    private var listingsLoaded = false
    private var accountLoaded = !_uiState.value.viewerIsAdmin

    init {
        viewModelScope.launch {
            propiedades.observeAll().collect { all ->
                val mine = all.filter { it.sellerId == userId }
                val first = mine.firstOrNull()
                // La primera emisión puede llegar vacía mientras se pide la lista al API
                if (all.isEmpty() && !listingsLoaded) return@collect
                listingsLoaded = true
                _uiState.update { s ->
                    s.copy(
                        listings = mine,
                        // Los datos de la cuenta (admin) tienen prioridad sobre los del anuncio
                        name = s.account?.let { fullName(it) } ?: first?.sellerName ?: s.name,
                        photoUrl = if (s.account != null) s.photoUrl else first?.sellerPhotoUrl ?: s.photoUrl,
                        email = s.account?.email ?: first?.sellerEmail ?: s.email,
                        phone = s.account?.phone ?: first?.sellerPhone ?: s.phone,
                        isLoading = !accountLoaded
                    )
                }
            }
        }

        // Si el API no regresa ningún anuncio la lista nunca cambia: no se queda cargando para siempre
        viewModelScope.launch {
            delay(4_000)
            if (!listingsLoaded) {
                listingsLoaded = true
                _uiState.update { it.copy(isLoading = !accountLoaded) }
            }
        }

        if (_uiState.value.viewerIsAdmin) {
            viewModelScope.launch {
                val account = Repositories.users.detalle(userId.toString()).getOrNull()
                accountLoaded = true
                _uiState.update { s ->
                    if (account == null) {
                        s.copy(isLoading = !listingsLoaded)
                    } else {
                        s.copy(
                            account = account,
                            name = fullName(account),
                            photoUrl = fotoUsuarioAdminUrl(account.id),
                            email = account.email,
                            phone = account.phone,
                            isLoading = !listingsLoaded
                        )
                    }
                }
            }
        }
    }

    fun toggleSaved(property: Property) {
        viewModelScope.launch { propiedades.setSaved(property.id, !property.isSaved) }
    }

    private fun fullName(u: UserDetail): String =
        listOfNotNull(u.name, u.firstSurname, u.secondSurname?.takeIf { it.isNotBlank() }).joinToString(" ")

    class Factory(
        private val application: Application,
        private val userId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            UserProfileViewModel(application, userId) as T
    }
}
