package com.equipo4.nearhome.ui.profile.seller

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.local.SessionManager
import com.equipo4.nearhome.data.session.CurrentUserStore
import com.equipo4.nearhome.di.NetworkModule
import com.equipo4.nearhome.network.AuthApi
import com.equipo4.nearhome.network.UpdateMeRequest
import com.equipo4.nearhome.ui.profile.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import com.equipo4.nearhome.network.UsuarioMeDto
import com.equipo4.nearhome.util.PhotoProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class EditProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val nameRegex = Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$")

    private val authApi: AuthApi = NetworkModule.provideRetrofit(
        NetworkModule.provideOkHttpClient(SessionManager(application))
    ).create(AuthApi::class.java)

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    var tempCameraUri: Uri? = null
        private set

    init {
        viewModelScope.launch {
            CurrentUserStore.user.collect { user ->
                if (user == null) return@collect
                val fotoRemota = fotoRemotaDe(user)
                _uiState.update { s ->
                    // Si el usuario ya cambió algo, no se pisa lo que está editando
                    val pristine = !s.hasChanges
                    s.copy(
                        initialFirstName = user.nombre,
                        initialFirstLastName = user.primerApellido,
                        initialSecondLastName = user.segundoApellido.orEmpty(),
                        initialPhotoUri = fotoRemota,
                        firstName = if (pristine) user.nombre else s.firstName,
                        firstLastName = if (pristine) user.primerApellido else s.firstLastName,
                        secondLastName = if (pristine) user.segundoApellido.orEmpty() else s.secondLastName,
                        photoUri = if (pristine) fotoRemota else s.photoUri,
                        firstNameError = if (pristine) null else s.firstNameError,
                        firstLastNameError = if (pristine) null else s.firstLastNameError,
                        secondLastNameError = if (pristine) null else s.secondLastNameError,
                        role = UserRole.fromDb(user.rol)
                    )
                }
            }
        }
    }

    private fun fotoRemotaDe(user: UsuarioMeDto): Uri? =
        if (user.tieneFoto && user.fotoVersion != null) {
            Uri.parse(NetworkModule.fotoPerfilUrl(user.fotoVersion))
        } else {
            null
        }

    fun onFirstNameChange(value: String) {
        if (value.length > 50) return
        val error = validateNameField(value, isRequired = true)
        _uiState.update { it.copy(firstName = value, firstNameError = error) }
    }

    fun onFirstLastNameChange(value: String) {
        if (value.length > 50) return
        val error = validateNameField(value, isRequired = true)
        _uiState.update { it.copy(firstLastName = value, firstLastNameError = error) }
    }

    fun onSecondLastNameChange(value: String) {
        if (value.length > 50) return
        val error = validateNameField(value, isRequired = false)
        _uiState.update { it.copy(secondLastName = value, secondLastNameError = error) }
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
                    localFile.outputStream().use { output -> input.copyTo(output) }
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
        val state = _uiState.value
        if (!state.isSaveEnabled) return
        val app = getApplication<Application>()

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }

            fun fallar(msg: String?) {
                _uiState.update {
                    it.copy(isSaving = false, saveError = msg ?: "No se pudo guardar. Inténtalo de nuevo.")
                }
            }

            try {
                // 1) Nombre y apellidos, solo si cambiaron
                val nombresCambiaron =
                    state.firstName.trim() != state.initialFirstName.trim() ||
                            state.firstLastName.trim() != state.initialFirstLastName.trim() ||
                            state.secondLastName.trim() != state.initialSecondLastName.trim()

                if (nombresCambiaron) {
                    val r = authApi.updateMe(
                        UpdateMeRequest(
                            nombre = state.firstName.trim(),
                            primerApellido = state.firstLastName.trim(),
                            segundoApellido = state.secondLastName.trim().ifBlank { null }
                        )
                    )
                    if (!r.isSuccessful) {
                        fallar(apiMessage(r.errorBody()?.string()))
                        return@launch
                    }
                }

                // 2) Foto: subir si es nueva, borrar si la quitaron
                val uri = state.photoUri
                if (uri != state.initialPhotoUri) {
                    val r = if (uri == null) {
                        authApi.borrarFoto()
                    } else {
                        val jpeg = withContext(Dispatchers.IO) { PhotoProcessor.comprimirAJpeg(app, uri) }
                        if (jpeg == null) {
                            fallar("No se pudo leer la foto elegida")
                            return@launch
                        }
                        authApi.subirFoto(
                            MultipartBody.Part.createFormData(
                                "foto",
                                "perfil.jpg",
                                jpeg.toRequestBody("image/jpeg".toMediaType())
                            )
                        )
                    }
                    if (!r.isSuccessful) {
                        fallar(apiMessage(r.errorBody()?.string()))
                        return@launch
                    }
                }

                // 3) Refrescar el estado compartido: Cuenta, Mis anuncios y Perfil se actualizan
                CurrentUserStore.refresh(authApi)
                _uiState.update { it.copy(isSaving = false, isSaveSuccess = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                fallar("No se pudo conectar con el servidor. Revisa tu conexión a internet e inténtalo de nuevo")
            } catch (e: Exception) {
                fallar("Ocurrió un error inesperado. Inténtalo de nuevo.")
            }
        }
    }

    /** Saca el mensaje que manda la API (campo "error", "message", "mensaje" o "detail"). */
    private fun apiMessage(raw: String?): String? = try {
        val json = JSONObject(raw ?: "")
        listOf("error", "message", "mensaje", "detail").firstNotNullOfOrNull { key ->
            json.optString(key, "").takeIf { it.isNotBlank() }
        }
    } catch (e: Exception) {
        null
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