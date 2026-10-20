package com.equipo4.nearhome.ui.profile.seller

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.R
import com.equipo4.nearhome.ui.profile.UserRole
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class EditProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val nameRegex = Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$")

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    var tempCameraUri: Uri? = null
        private set

    init {
        loadMockFrontendData()
    }

    private fun loadMockFrontendData() {
        val firstName = "Anthony Edward"
        val firstLastName = "Stark"
        val secondLastName = ""

        _uiState.update {
            it.copy(
                initialFirstName = firstName,
                initialFirstLastName = firstLastName,
                initialSecondLastName = secondLastName,
                initialPhotoUri = null,
                firstName = firstName,
                firstLastName = firstLastName,
                secondLastName = secondLastName,
                photoUri = null,
                role = UserRole.VENDEDOR
            )
        }
    }

    fun onFirstNameChange(value: String) {
        if (value.length > 50) return
        val error = validateNameField(value, isRequired = true)
        _uiState.update {
            it.copy(firstName = value, firstNameError = error)
        }
    }

    fun onFirstLastNameChange(value: String) {
        if (value.length > 50) return
        val error = validateNameField(value, isRequired = true)
        _uiState.update {
            it.copy(firstLastName = value, firstLastNameError = error)
        }
    }

    fun onSecondLastNameChange(value: String) {
        if (value.length > 50) return
        val error = validateNameField(value, isRequired = false)
        _uiState.update {
            it.copy(secondLastName = value, secondLastNameError = error)
        }
    }

    private fun validateNameField(text: String, isRequired: Boolean): String? {
        val trimmed = text.trim()
        if (isRequired && trimmed.isEmpty()) {
            return getApplication<Application>().getString(R.string.edit_profile_error_required)
        }
        if (text.isNotEmpty() && !nameRegex.matches(text)) {
            return getApplication<Application>().getString(R.string.edit_profile_error_only_letters)
        }
        return null
    }

    fun openBottomSheet() {
        _uiState.update { it.copy(isBottomSheetOpen = true) }
    }

    fun closeBottomSheet() {
        _uiState.update { it.copy(isBottomSheetOpen = false) }
    }

    fun createCameraImageUri(context: Context): Uri {
        val imageFile = File(
            context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES),
            "profile_temp_${System.currentTimeMillis()}.jpg"
        )
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, imageFile)
        tempCameraUri = uri
        return uri
    }

    fun onCameraImageCaptured(success: Boolean) {
        if (success && tempCameraUri != null) {
            _uiState.update { it.copy(photoUri = tempCameraUri, isBottomSheetOpen = false) }
        }
    }

    fun onGalleryImagePicked(context: Context, uri: Uri?) {
        uri ?: return
        viewModelScope.launch {
            val localFile = File(context.filesDir, "profile_photo_${System.currentTimeMillis()}.jpg")
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    localFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                val localUri = Uri.fromFile(localFile)
                _uiState.update { it.copy(photoUri = localUri, isBottomSheetOpen = false) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onRemovePhoto() {
        _uiState.update { it.copy(photoUri = null, isBottomSheetOpen = false) }
    }

    fun setShowCameraPermissionRationale(show: Boolean) {
        _uiState.update { it.copy(showCameraPermissionRationale = show) }
    }

    fun saveProfile() {
        val currentState = _uiState.value
        if (!currentState.isSaveEnabled) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            delay(200)
            _uiState.update { it.copy(isSaving = false, isSaveSuccess = true) }
        }
    }

    fun onAttemptExit(onConfirmExit: () -> Unit) {
        if (_uiState.value.hasChanges) {
            _uiState.update { it.copy(showDiscardDialog = true) }
        } else {
            onConfirmExit()
        }
    }

    fun dismissDiscardDialog() {
        _uiState.update { it.copy(showDiscardDialog = false) }
    }
}