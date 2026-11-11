package com.equipo4.nearhome.ui.profile.seller

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.repository.MyListingsRepository
import com.equipo4.nearhome.data.session.CurrentUserStore
import com.equipo4.nearhome.di.NetworkModule
import com.equipo4.nearhome.ui.profile.UserRole
import com.equipo4.nearhome.util.AppStrings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.equipo4.nearhome.data.repository.Repositories

class MyListingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MyListingsRepository(application)
    private val session = SessionManager(application)

    private val _uiState = MutableStateFlow(
        MyListingsUiState(
            role = UserRole.fromDb(session.getUserRole())
        )
    )
    val uiState: StateFlow<MyListingsUiState> = _uiState.asStateFlow()

    /** Aviso para mostrar en un snackbar (anuncio eliminado o el error si no se pudo). */
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        viewModelScope.launch {
            CurrentUserStore.user.collect { user ->
                _uiState.update {
                    it.copy(
                        userName = user?.let { u ->
                            listOfNotNull(u.nombre, u.primerApellido, u.segundoApellido).joinToString(" ")
                        }.orEmpty(),
                        userEmail = user?.correo.orEmpty(),
                        userPhone = user?.telefono?.takeIf { it.isNotBlank() } ?: "",
                        userPhotoUrl = user?.takeIf { it.tieneFoto && it.fotoVersion != null }
                            ?.let { NetworkModule.fotoPerfilUrl(it.fotoVersion!!) },
                        role = UserRole.fromDb(user?.rol ?: session.getUserRole())
                    )
                }
            }
        }
    }

    /**
     * Pide los anuncios al API. La pantalla lo llama cada vez que se muestra, así un anuncio
     * recién creado, aprobado o rechazado aparece con su estado actual.
     */
    fun loadMyListings() {
        viewModelScope.launch {
            // Si se reintenta desde "sin conexión", la pantalla se queda igual hasta tener resultado
            val retrying = _uiState.value.loadError && _uiState.value.listings.isEmpty()
            _uiState.update { it.copy(isLoading = !retrying, isRetrying = retrying, loadError = retrying) }
            repository.load()
                .onSuccess { items ->
                    _uiState.update { it.copy(listings = items, isLoading = false, isRetrying = false, loadError = false) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false, isRetrying = false, loadError = true) }
                }
        }
    }

    /** Pausa (activa=false) o reactiva (activa=true) un anuncio publicado. */
    fun setListingActive(id: String, activa: Boolean) {
        viewModelScope.launch {
            repository.setActiva(id, activa)
                .onSuccess { updated ->
                    Repositories.propiedades.refresh()
                    _uiState.update { state ->
                        state.copy(
                            listings = state.listings.map {
                                if (it.property.id == id) updated else it
                            }
                        )
                    }
                    _message.value = AppStrings.get(
                        if (activa) R.string.manage_listing_resumed
                        else R.string.manage_listing_paused
                    )
                }
                .onFailure {
                    _message.value = it.message
                        ?: AppStrings.get(R.string.create_listing_error_generic)
                }
        }
    }

    /** Elimina el anuncio en el API y, si salió bien, lo quita de la lista. */
    fun deleteListing(id: String) {
        viewModelScope.launch {
            repository.delete(id)
                .onSuccess {
                    Repositories.propiedades.refresh()
                    _uiState.update { state ->
                        state.copy(listings = state.listings.filterNot { it.property.id == id })
                    }
                    _message.value = AppStrings.get(R.string.manage_listing_deleted)
                }
                .onFailure {
                    _message.value = it.message
                        ?: AppStrings.get(R.string.create_listing_error_generic)
                }
        }
    }

    fun messageShown() {
        _message.value = null
    }
}