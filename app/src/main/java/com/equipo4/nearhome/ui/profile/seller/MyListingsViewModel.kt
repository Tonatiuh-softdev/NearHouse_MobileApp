package com.equipo4.nearhome.ui.profile.seller

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.session.CurrentUserStore
import com.equipo4.nearhome.ui.profile.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.equipo4.nearhome.di.NetworkModule

class MyListingsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MyListingsUiState())
    val uiState: StateFlow<MyListingsUiState> = _uiState.asStateFlow()

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
                        role = UserRole.fromDb(user?.rol)
                    )
                }
            }
        }
        loadMyListings()
    }

    fun loadMyListings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // AQUÍ: conectar con los endpoints de publicaciones cuando Nahum los termine
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}