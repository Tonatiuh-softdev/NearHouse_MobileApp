package com.equipo4.nearhome.ui.publicacion.report

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.repository.ReporteException
import com.equipo4.nearhome.data.repository.ReporteRepository
import com.equipo4.nearhome.util.AppStrings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Mínimo de palabras en la descripción del reporte. */
const val MIN_PALABRAS_DESCRIPCION = 10

data class ReportUiState(
    val publicacionId: String = "",
    val selectedCategory: ReportCategory? = null,
    val selectedReason: ReportReason? = null,
    val description: String = "",
    val evidenceFiles: List<EvidenceFile> = emptyList(),
    val errorMessage: String? = null,
    /** True mientras se envía. Desactiva el botón para que un doble toque no mande dos reportes. */
    val isSending: Boolean = false,
    /** True si el API dice que la publicación ya no existe: la pantalla regresa al detalle. */
    val publicacionNoDisponible: Boolean = false,
    val isSubmitted: Boolean = false
) {
    /** Cuenta palabras reales (separadas por espacios), no caracteres. */
    val palabrasDescripcion: Int
        get() = description.trim().split(Regex("\\s+")).count { it.isNotBlank() }

    val isSendEnabled: Boolean
        get() = selectedReason != null && palabrasDescripcion >= MIN_PALABRAS_DESCRIPCION

    val hasUnsavedChanges: Boolean
        get() = description.isNotEmpty() || evidenceFiles.isNotEmpty()
}

class ReportViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    fun initPublicacion(id: String) {
        _uiState.update { it.copy(publicacionId = id) }
    }

    fun selectCategory(category: ReportCategory) {
        _uiState.update { state ->
            if (state.selectedCategory != category) {
                state.copy(selectedCategory = category, selectedReason = null)
            } else {
                state
            }
        }
    }

    fun selectReason(reason: ReportReason) {
        _uiState.update { it.copy(selectedReason = reason) }
    }

    fun onDescriptionChange(newText: String) {
        if (newText.length <= 200) {
            _uiState.update { it.copy(description = newText) }
        }
    }

    fun addEvidence(file: EvidenceFile) {
        if (_uiState.value.evidenceFiles.size < 5) {
            _uiState.update { it.copy(evidenceFiles = it.evidenceFiles + file) }
        }
    }

    fun removeEvidence(file: EvidenceFile) {
        _uiState.update { it.copy(evidenceFiles = it.evidenceFiles - file) }
    }

    fun setErrorMessage(message: String?) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null, publicacionNoDisponible = false) }
    }

    /**
     * Manda el reporte al API. Si falla, no se borra nada de lo que capturó el usuario,
     * así puede reintentar cuando vuelva la conexión.
     */
    fun submitReport(context: Context) {
        val state = _uiState.value
        if (state.isSending || state.isSubmitted || !state.isSendEnabled) return
        val categoria = state.selectedCategory ?: return
        val motivo = state.selectedReason ?: return
        val publicacionId = state.publicacionId.toIntOrNull()
        if (publicacionId == null) {
            setErrorMessage(AppStrings.get(R.string.create_listing_error_generic))
            return
        }

        // Se marca antes de lanzar la corrutina para que un segundo toque ya vea isSending = true
        _uiState.update { it.copy(isSending = true, errorMessage = null) }

        viewModelScope.launch {
            ReporteRepository(context).reportar(
                publicacionId = publicacionId,
                categoria = categoria.name,
                motivo = motivo.codigo,
                descripcion = state.description,
                fotos = state.evidenceFiles.map { it.uri }
            ).onSuccess {
                _uiState.update { it.copy(isSending = false, isSubmitted = true) }
            }.onFailure { error ->
                val codigo = (error as? ReporteException)?.codigoHttp
                _uiState.update {
                    it.copy(
                        isSending = false,
                        errorMessage = error.message,
                        publicacionNoDisponible = codigo == 404
                    )
                }
            }
        }
    }

    fun dismissSuccessDialog() {
        _uiState.update { it.copy(isSubmitted = false) }
    }
}