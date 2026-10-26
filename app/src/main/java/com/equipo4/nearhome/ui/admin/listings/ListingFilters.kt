package com.equipo4.nearhome.ui.admin.listings

import com.equipo4.nearhome.domain.model.AdminListing
import com.equipo4.nearhome.domain.model.ListingAction
import com.equipo4.nearhome.domain.model.ListingStatus
import com.equipo4.nearhome.domain.model.ListingType
import com.equipo4.nearhome.domain.model.PropertyType
import java.text.Normalizer
import java.util.Locale

enum class SortOrder { NEWEST_FIRST, OLDEST_FIRST }

/**
 * Filtros aplicados. Un grupo vacío significa "sin filtrar por ese grupo".
 * El valor por defecto es la cola de revisión: solo pendientes, del más nuevo al más viejo.
 */
data class ListingFilters(
    val statuses: Set<ListingStatus> = setOf(ListingStatus.PENDIENTE),
    val modalities: Set<ListingType> = emptySet(),
    val types: Set<PropertyType> = emptySet(),
    val order: SortOrder = SortOrder.NEWEST_FIRST
) {
    /** El orden no cuenta como filtro. */
    val selectedCount: Int get() = statuses.size + modalities.size + types.size

    companion object {
        val Queue = ListingFilters()
    }
}

/** Minúsculas y sin acentos, para buscar sin importar mayúsculas ni tildes. */
fun normalizeText(text: String): String =
    Normalizer.normalize(text.trim().lowercase(Locale.ROOT), Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")

fun applyListingFilters(
    all: List<AdminListing>,
    query: String,
    filters: ListingFilters
): List<AdminListing> {
    val q = normalizeText(query)
    val filtered = all.filter { l ->
        (filters.statuses.isEmpty() || l.status in filters.statuses) &&
            (filters.modalities.isEmpty() || l.listingType in filters.modalities) &&
            (filters.types.isEmpty() || l.type in filters.types) &&
            (q.isEmpty() ||
                normalizeText(l.title).contains(q) ||
                normalizeText(l.id).contains(q) ||
                normalizeText(l.advertiser.name).contains(q))
    }
    return when (filters.order) {
        SortOrder.NEWEST_FIRST -> filtered.sortedByDescending { it.registeredAt }
        SortOrder.OLDEST_FIRST -> filtered.sortedBy { it.registeredAt }
    }
}

// ---------------- formulario de las pantallas de confirmación ----------------

const val MIN_REJECT_CHARS = 10

data class ActionForm(
    val checks: List<Boolean> = listOf(false, false, false),
    val text: String = "",
    val acknowledged: Boolean = false
)

fun isActionFormValid(action: ListingAction, form: ActionForm): Boolean =
    when (action) {
        ListingAction.APROBAR -> form.checks.all { it }
        ListingAction.RECHAZAR -> form.text.trim().length >= MIN_REJECT_CHARS
        ListingAction.DESACTIVAR -> form.text.isNotBlank()
        ListingAction.REACTIVAR -> true
        ListingAction.ELIMINAR -> form.text.isNotBlank() && form.acknowledged
    }
