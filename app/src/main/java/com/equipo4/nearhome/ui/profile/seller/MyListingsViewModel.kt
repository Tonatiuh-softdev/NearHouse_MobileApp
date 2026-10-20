package com.equipo4.nearhome.ui.profile.seller

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.ui.profile.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MyListingsViewModel(application: Application) : AndroidViewModel(application) {

    private val session = SessionManager(application)

    private val _uiState = MutableStateFlow(
        MyListingsUiState(
            userName = session.getUserName().orEmpty(),
            userEmail = session.getUserEmail().orEmpty(),
            userPhone = session.getUserPhone().orEmpty().ifBlank { "+52 314 123 4567" },
            userPhotoUrl = null,
            role = UserRole.fromDb(session.getUserRole())
        )
    )
    val uiState: StateFlow<MyListingsUiState> = _uiState.asStateFlow()

    init {
        loadMyListings()
    }

    fun loadMyListings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // AQUÍ: Llama a tu repositorio cuando se conecte con el backend
                // val userProperties = repository.getMyProperties()
                // _uiState.update { it.copy(listings = userProperties, isLoading = false) }
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}