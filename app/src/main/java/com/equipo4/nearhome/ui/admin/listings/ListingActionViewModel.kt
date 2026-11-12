package com.equipo4.nearhome.ui.admin.listings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo4.nearhome.R
import com.equipo4.nearhome.data.repository.Repositories
import com.equipo4.nearhome.domain.model.AdminListing
import com.equipo4.nearhome.domain.model.ListingAction
import com.equipo4.nearhome.util.AppStrings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Mínimo de caracteres para motivo de rechazo. */
const val MIN_REJECT_CHARS = 10

/**
 * Datos que escribe el administrador en la confirmación.
 * checks: checklist de aprobar. text: motivo (rechazar, desactivar, eliminar).
 * acknowledged: casilla de confirmación de eliminar.
 */
data class ActionForm(
    val checks: List<Boolean> = listOf(false, false, false),
    val text: String = "",
    val acknowledged: Boolean = false
)

class ListingActionViewModel(val listingId: String, val action: ListingAction) : ViewModel() {
    private val repo = Repositories.anuncios

    /** Foto fija del anuncio al abrir la pantalla: no cambia aunque el estado cambie al confirmar. */
    val listing: AdminListing? = repo.getById(listingId)

    private val _form = MutableStateFlow(ActionForm())
    val form: StateFlow<ActionForm> = _form.asStateFlow()

    private val _submitting = MutableStateFlow(false)
    val submitting: StateFlow<Boolean> = _submitting.asStateFlow()

    /** Mensaje del API si no se pudo aplicar el cambio (por ejemplo, "Este anuncio ya fue revisado"). */
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var done = false

    fun setCheck(index: Int, value: Boolean) = _form.update { f ->
        f.copy(checks = f.checks.mapIndexed { i, c -> if (i == index) value else c })
    }

    fun setText(value: String) = _form.update { it.copy(text = value) }

    fun setAcknowledged(value: Boolean) = _form.update { it.copy(acknowledged = value) }

    /**
     * Aplica el cambio en el API. [onSuccess] se llama una sola vez, cuando el API ya lo confirmó.
     */
    fun confirm(actor: String, onSuccess: () -> Unit) {
        val l = listing ?: return
        val form = _form.value
        if (done || _submitting.value || !formularioCompleto(action, form)) return

        viewModelScope.launch {
            _submitting.value = true
            _error.value = null
            val resultado = when (action) {
                ListingAction.APROBAR -> repo.approve(l.id, actor)
                ListingAction.RECHAZAR -> repo.reject(l.id, form.text, actor)
                ListingAction.DESACTIVAR -> repo.desactivar(l.id, form.text)
                ListingAction.REACTIVAR -> repo.reactivar(l.id)
                ListingAction.ELIMINAR -> repo.eliminar(l.id)
            }
            _submitting.value = false
            resultado
                .onSuccess {
                    done = true
                    onSuccess()
                }
                .onFailure {
                    _error.value = it.message ?: AppStrings.get(R.string.create_listing_error_generic)
                }
        }
    }

    /** Validación mínima antes de enviar. La pantalla usa la misma regla para habilitar el botón. */
    private fun formularioCompleto(action: ListingAction, form: ActionForm): Boolean = when (action) {
        ListingAction.APROBAR -> form.checks.all { it }
        ListingAction.RECHAZAR -> form.text.trim().length >= MIN_REJECT_CHARS
        ListingAction.DESACTIVAR -> form.text.isNotBlank()
        ListingAction.REACTIVAR -> true
        ListingAction.ELIMINAR -> form.text.isNotBlank() && form.acknowledged
    }
}