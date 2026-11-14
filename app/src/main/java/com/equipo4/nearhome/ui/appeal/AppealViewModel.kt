package com.equipo4.nearhome.ui.appeal

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.data.repository.ApelacionException
import com.equipo4.nearhome.data.repository.ApelacionRepository
import com.equipo4.nearhome.ui.publicacion.report.EvidenceFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Mínimo de palabras de la explicación (igual que en los reportes). */
const val MIN_PALABRAS_APELACION = 10

/** Máximo de caracteres de la explicación (el API acepta de 10 a 500). */
const val MAX_CARACTERES_APELACION = 500

data class AppealUiState(
    val objetivo: ObjetivoApelacion? = null,
    val description: String = "",
    val evidenceFiles: List<EvidenceFile> = emptyList(),
    val errorMessage: String? = null,
    /** True mientras se envía. Desactiva el botón para que un doble toque no mande dos apelaciones. */
    val isSending: Boolean = false,
    /** True si ya no se puede apelar (ya apeló, venció el comprobante...): al cerrar el aviso se regresa. */
    val salirAlCerrarError: Boolean = false,
    val isSubmitted: Boolean = false
) {
    val palabrasDescripcion: Int
        get() = description.trim().split(Regex("\\s+")).count { it.isNotBlank() }

    val isSendEnabled: Boolean
        get() = objetivo != null && palabrasDescripcion >= MIN_PALABRAS_APELACION

    val hasUnsavedChanges: Boolean
        get() = description.isNotEmpty() || evidenceFiles.isNotEmpty()
}

class AppealViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AppealUiState(objetivo = ApelacionEnCurso.objetivo))
    val uiState: StateFlow<AppealUiState> = _uiState.asStateFlow()

    fun onDescriptionChange(newText: String) {
        if (newText.length <= MAX_CARACTERES_APELACION) {
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

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null, salirAlCerrarError = false) }
    }

    /** Manda la apelación. Si falla, no se borra lo que escribió, así puede reintentar. */
    fun submit(context: Context) {
        val state = _uiState.value
        if (state.isSending || state.isSubmitted || !state.isSendEnabled) return
        val objetivo = state.objetivo ?: return

        // Se marca antes de lanzar la corrutina para que un segundo toque ya vea isSending = true
        _uiState.update { it.copy(isSending = true, errorMessage = null) }

        viewModelScope.launch {
            val repositorio = ApelacionRepository(context)
            val fotos = state.evidenceFiles.map { it.uri }
            val resultado = when (objetivo) {
                is ObjetivoApelacion.Publicacion ->
                    repositorio.apelarPublicacion(objetivo.id, state.description, fotos)
                is ObjetivoApelacion.Suspension ->
                    repositorio.apelarSuspension(objetivo.token, state.description, fotos)
            }
            resultado.onSuccess {
                ApelacionEnCurso.limpiar()
                _uiState.update { it.copy(isSending = false, isSubmitted = true) }
            }.onFailure { error ->
                val codigo = (error as? ApelacionException)?.codigoHttp
                _uiState.update {
                    it.copy(
                        isSending = false,
                        errorMessage = error.message,
                        // Ya apeló, ya no se puede apelar o venció el comprobante: no tiene caso reintentar
                        salirAlCerrarError = codigo == 401 || codigo == 404 || codigo == 409
                    )
                }
            }
        }
    }
}
