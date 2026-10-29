package com.equipo4.nearhome.ui.admin.listings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.equipo4.nearhome.R
import com.equipo4.nearhome.domain.model.ListingStatus
import com.equipo4.nearhome.domain.model.ListingType
import com.equipo4.nearhome.domain.model.PropertyType
import java.util.Locale

private val MxLocale: Locale = Locale.forLanguageTag("es-MX")

/** Formato único de precio: renta "$10,090/mes", venta "$4,850,000". */
@Composable
fun priceText(price: Double, type: ListingType): String = stringResource(
    if (type == ListingType.RENTA) R.string.listing_price_rent else R.string.listing_price_sale,
    formatAmount(price)
)

@Composable
fun ListingStatus.label(): String = stringResource(
    when (this) {
        ListingStatus.PENDIENTE -> R.string.listing_status_pending
        ListingStatus.ACTIVO -> R.string.listing_status_active
        ListingStatus.RECHAZADO -> R.string.listing_status_rejected
        ListingStatus.DESACTIVADO -> R.string.listing_status_deactivated
        ListingStatus.RENTADO_VENDIDO -> R.string.listing_status_closed
    }
)

/** Texto largo para el encabezado del detalle, ej. "Pendiente de revisión". */
@Composable
fun ListingStatus.headerLabel(): String =
    if (this == ListingStatus.PENDIENTE) stringResource(R.string.listing_status_pending_review) else label()

@Composable
fun ListingStatus.adjective(): String = stringResource(
    when (this) {
        ListingStatus.PENDIENTE -> R.string.listing_status_adj_pending
        ListingStatus.ACTIVO -> R.string.listing_status_adj_active
        ListingStatus.RECHAZADO -> R.string.listing_status_adj_rejected
        ListingStatus.DESACTIVADO -> R.string.listing_status_adj_deactivated
        ListingStatus.RENTADO_VENDIDO -> R.string.listing_status_adj_closed
    }
)

@Composable
fun ListingType.label(): String = stringResource(
    if (this == ListingType.RENTA) R.string.listing_mode_rent else R.string.listing_mode_sale
)

@Composable
fun ListingType.inLabel(): String = stringResource(
    if (this == ListingType.RENTA) R.string.listing_mode_in_rent else R.string.listing_mode_in_sale
)

@Composable
fun PropertyType.label(): String = stringResource(
    when (this) {
        PropertyType.DEPARTAMENTO -> R.string.listing_type_apartment
        PropertyType.CASA -> R.string.listing_type_house
        PropertyType.TERRENO -> R.string.listing_type_land
    }
)

@Composable
fun PropertyType.pluralLabel(): String = stringResource(
    when (this) {
        PropertyType.DEPARTAMENTO -> R.string.listing_type_plural_apartment
        PropertyType.CASA -> R.string.listing_type_plural_house
        PropertyType.TERRENO -> R.string.listing_type_plural_land
    }
)

@Composable
fun SortOrder.label(): String = stringResource(
    if (this == SortOrder.NEWEST_FIRST) R.string.listing_order_newest else R.string.listing_order_oldest
)

/** Texto de lo que se está mostrando, ej. "Rechazado, Venta" o "Todos los anuncios". */
@Composable
fun activeFilterLabels(filters: ListingFilters): List<String> =
    filters.statuses.sortedBy { it.ordinal }.map { it.label() } +
        filters.modalities.sortedBy { it.ordinal }.map { it.label() } +
        filters.types.sortedBy { it.ordinal }.map { it.label() }

/** "No encontramos anuncios rechazados de tipo casa en venta" */
@Composable
fun emptyListingsMessage(filters: ListingFilters, query: String): String {
    val parts = mutableListOf(stringResource(R.string.listing_word_listings))
    if (filters.statuses.isNotEmpty()) {
        parts += filters.statuses.sortedBy { it.ordinal }.map { it.adjective() }
            .joinToString(stringResource(R.string.listing_word_or))
    }
    if (filters.types.isNotEmpty()) {
        parts += stringResource(
            R.string.listing_of_type_format,
            filters.types.sortedBy { it.ordinal }.map { it.label().lowercase(MxLocale) }
                .joinToString(stringResource(R.string.listing_word_or))
        )
    }
    if (filters.modalities.size == 1) parts += filters.modalities.first().inLabel()
    if (query.isNotBlank()) parts += stringResource(R.string.listing_matching_format, query.trim())
    return stringResource(R.string.listings_empty_message, parts.joinToString(" "))
}

/** "Se mostrarán departamentos en renta que siguen pendientes de revisión" */
@Composable
fun filterSummaryNote(filters: ListingFilters): String {
    if (filters.selectedCount == 0) return stringResource(R.string.filters_note_all)
    val parts = mutableListOf<String>()
    parts += if (filters.types.isEmpty()) stringResource(R.string.listing_word_listings)
    else filters.types.sortedBy { it.ordinal }.map { it.pluralLabel() }
        .joinToString(stringResource(R.string.listing_word_and))
    if (filters.modalities.size == 1) parts += filters.modalities.first().inLabel()
    if (filters.statuses == setOf(ListingStatus.PENDIENTE)) {
        parts += stringResource(R.string.filters_note_still_pending)
    } else if (filters.statuses.isNotEmpty()) {
        parts += stringResource(
            R.string.filters_note_with_status,
            filters.statuses.sortedBy { it.ordinal }.map { it.label().lowercase(MxLocale) }
                .joinToString(stringResource(R.string.listing_word_or))
        )
    }
    return stringResource(R.string.filters_note_format, parts.joinToString(" "))
}
