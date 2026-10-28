package com.equipo4.nearhome.ui.profile.seller

import android.net.Uri
import com.equipo4.nearhome.ui.profile.UserRole

data class EditProfileUiState(
    // Estado inicial de referencia
    val initialFirstName: String = "",
    val initialFirstLastName: String = "",
    val initialSecondLastName: String = "",
    val initialPhotoUri: Uri? = null,

    // Estado actual del formulario
    val firstName: String = "",
    val firstLastName: String = "",
    val secondLastName: String = "",
    val photoUri: Uri? = null,

    // Errores de validación
    val firstNameError: String? = null,
    val firstLastNameError: String? = null,
    val secondLastNameError: String? = null,

    // Estados de UI
    val role: UserRole = UserRole.VENDEDOR,
    val isBottomSheetOpen: Boolean = false,
    val showDiscardDialog: Boolean = false,
    val showCameraPermissionRationale: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val isSaveSuccess: Boolean = false
) {
    val hasChanges: Boolean
        get() = firstName != initialFirstName ||
                firstLastName != initialFirstLastName ||
                secondLastName != initialSecondLastName ||
                photoUri != initialPhotoUri

    val isValid: Boolean
        get() = firstName.trim().isNotEmpty() &&
                firstLastName.trim().isNotEmpty() &&
                firstNameError == null &&
                firstLastNameError == null &&
                secondLastNameError == null

    val isSaveEnabled: Boolean
        get() = hasChanges && isValid && !isSaving
}