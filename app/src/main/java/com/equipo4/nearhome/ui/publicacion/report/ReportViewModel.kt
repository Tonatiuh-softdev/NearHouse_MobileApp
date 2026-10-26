package com.equipo4.nearhome.ui.publicacion.report

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ReportUiState(
    val publicacionId: String = "",
    val selectedCategory: ReportCategory? = null,
    val selectedReason: ReportReason? = null,
    val description: String = "",
    val evidenceFiles: List<EvidenceFile> = emptyList(),
    val errorMessage: String? = null,
    val isSubmitted: Boolean = false
) {
    val isSendEnabled: Boolean
        get() = selectedReason != null && description.trim().length >= 10

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
        _uiState.update { currentState ->
            if (currentState.selectedCategory != category) {
                currentState.copy(
                    selectedCategory = category,
                    selectedReason = null
                )
            } else {
                currentState
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
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun submitReport() {
        _uiState.update { it.copy(isSubmitted = true) }
    }

    fun dismissSuccessDialog() {
        _uiState.update { it.copy(isSubmitted = false) }
    }
}